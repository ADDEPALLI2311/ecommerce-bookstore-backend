package com.bookstore.order.service.impl;

import com.bookstore.book.entity.Book;
import com.bookstore.book.repository.BookRepository;
import com.bookstore.cart.entity.Cart;
import com.bookstore.cart.entity.CartItem;
import com.bookstore.cart.repository.CartRepository;
import com.bookstore.common.PagedResponse;
import com.bookstore.common.exception.BadRequestException;
import com.bookstore.common.exception.InsufficientStockException;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.customer.entity.Customer;
import com.bookstore.customer.repository.CustomerRepository;
import com.bookstore.order.dto.CheckoutRequestDto;
import com.bookstore.order.dto.OrderItemResponseDto;
import com.bookstore.order.dto.OrderResponseDto;
import com.bookstore.order.dto.UpdateOrderStatusDto;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderItem;
import com.bookstore.order.entity.OrderStatus;
import com.bookstore.order.entity.PaymentStatus;
import com.bookstore.order.repository.OrderRepository;
import com.bookstore.order.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final CartRepository cartRepository;
    private final BookRepository bookRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CustomerRepository customerRepository,
                            CartRepository cartRepository,
                            BookRepository bookRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.cartRepository = cartRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public OrderResponseDto checkout(CheckoutRequestDto requestDto) {
        Customer customer = customerRepository.findById(requestDto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", requestDto.getCustomerId()));

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new BadRequestException("Shopping cart is empty. Please add items before checkout."));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Shopping cart is empty. Please add items before checkout.");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        String orderNumber = generateOrderNumber();

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setCustomer(customer);
        order.setShippingAddress(requestDto.getShippingAddress());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);

        for (CartItem cartItem : cart.getItems()) {
            Book book = bookRepository.findById(cartItem.getBook().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book", "id", cartItem.getBook().getId()));

            if (book.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException(book.getTitle(), book.getStockQuantity(), cartItem.getQuantity());
            }

            // Deduct inventory
            book.setStockQuantity(book.getStockQuantity() - cartItem.getQuantity());
            bookRepository.save(book);

            BigDecimal subtotal = book.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = new OrderItem(
                    order,
                    book,
                    book.getTitle(),
                    book.getIsbn(),
                    cartItem.getQuantity(),
                    book.getPrice(),
                    subtotal
            );
            order.addItem(orderItem);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // Clear cart after successful checkout
        cart.clearItems();
        cartRepository.save(cart);

        return mapEntityToResponseDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return mapEntityToResponseDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        return mapEntityToResponseDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponseDto> getOrdersByCustomerId(Long customerId, Pageable pageable) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }

        Page<Order> orderPage = orderRepository.findByCustomerId(customerId, pageable);
        List<OrderResponseDto> content = orderPage.getContent().stream()
                .map(this::mapEntityToResponseDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isLast()
        );
    }

    @Override
    public OrderResponseDto updateOrderStatus(Long orderId, UpdateOrderStatusDto updateDto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot update status of a cancelled order.");
        }

        order.setOrderStatus(updateDto.getOrderStatus());
        if (updateDto.getPaymentStatus() != null) {
            order.setPaymentStatus(updateDto.getPaymentStatus());
        }

        Order updatedOrder = orderRepository.save(order);
        return mapEntityToResponseDto(updatedOrder);
    }

    @Override
    public OrderResponseDto cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is already cancelled.");
        }

        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Delivered orders cannot be cancelled.");
        }

        // Restore book stock
        for (OrderItem item : order.getItems()) {
            Book book = item.getBook();
            book.setStockQuantity(book.getStockQuantity() + item.getQuantity());
            bookRepository.save(book);
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        Order updatedOrder = orderRepository.save(order);
        return mapEntityToResponseDto(updatedOrder);
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ORD-" + timestamp + "-" + randomPart;
    }

    private OrderResponseDto mapEntityToResponseDto(Order order) {
        List<OrderItemResponseDto> itemDtos = order.getItems().stream()
                .map(this::mapItemEntityToDto)
                .collect(Collectors.toList());

        String customerName = order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName();

        return new OrderResponseDto(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomer().getId(),
                order.getCustomer().getEmail(),
                customerName,
                order.getOrderStatus(),
                order.getPaymentStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                itemDtos,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderItemResponseDto mapItemEntityToDto(OrderItem item) {
        return new OrderItemResponseDto(
                item.getId(),
                item.getBook().getId(),
                item.getBookTitle(),
                item.getBookIsbn(),
                item.getQuantity(),
                item.getPricePerUnit(),
                item.getSubtotal()
        );
    }
}
