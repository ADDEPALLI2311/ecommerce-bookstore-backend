package com.bookstore.order.service;

import com.bookstore.book.entity.Book;
import com.bookstore.book.repository.BookRepository;
import com.bookstore.cart.entity.Cart;
import com.bookstore.cart.entity.CartItem;
import com.bookstore.cart.repository.CartRepository;
import com.bookstore.common.exception.BadRequestException;
import com.bookstore.common.exception.InsufficientStockException;
import com.bookstore.customer.entity.Customer;
import com.bookstore.customer.repository.CustomerRepository;
import com.bookstore.order.dto.CheckoutRequestDto;
import com.bookstore.order.dto.OrderResponseDto;
import com.bookstore.order.dto.UpdateOrderStatusDto;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderStatus;
import com.bookstore.order.entity.PaymentStatus;
import com.bookstore.order.repository.OrderRepository;
import com.bookstore.order.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Customer customer;
    private Book book;
    private Cart cart;

    @BeforeEach
    void setUp() {
        customer = new Customer(1L, "john.doe@example.com", "John", "Doe", null, null, null, null, null, null);
        book = new Book(1L, "9780134685991", "Effective Java", "Joshua Bloch", "Tech", BigDecimal.valueOf(40.00), 10, null, null);
        cart = new Cart(customer);
        cart.setId(1L);

        CartItem cartItem = new CartItem(cart, book, 2, book.getPrice());
        cartItem.setId(1L);
        cart.addItem(cartItem);
    }

    @Test
    @DisplayName("Should successfully checkout cart, deduct stock and place order")
    void shouldCheckoutSuccessfully() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(100L);
            return o;
        });

        CheckoutRequestDto request = new CheckoutRequestDto(1L, "123 Main St, New York, NY");
        OrderResponseDto response = orderService.checkout(request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(BigDecimal.valueOf(80.00), response.getTotalAmount());
        assertEquals(OrderStatus.PENDING, response.getOrderStatus());
        assertEquals(8, book.getStockQuantity()); // Decremented by 2
        assertEquals(0, cart.getItems().size()); // Cart cleared
        verify(bookRepository).save(book);
        verify(cartRepository).save(cart);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException if cart is empty at checkout")
    void shouldThrowExceptionWhenCartIsEmpty() {
        cart.clearItems();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));

        CheckoutRequestDto request = new CheckoutRequestDto(1L, "123 Main St");

        assertThrows(BadRequestException.class, () -> orderService.checkout(request));
    }

    @Test
    @DisplayName("Should throw InsufficientStockException if book stock is depleted before checkout")
    void shouldThrowExceptionWhenStockDepleted() {
        book.setStockQuantity(1); // Cart has 2
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        CheckoutRequestDto request = new CheckoutRequestDto(1L, "123 Main St");

        assertThrows(InsufficientStockException.class, () -> orderService.checkout(request));
    }

    @Test
    @DisplayName("Should update order status successfully")
    void shouldUpdateOrderStatus() {
        Order order = new Order("ORD-12345", customer, BigDecimal.valueOf(80.00), "123 Main St");
        order.setId(100L);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        UpdateOrderStatusDto updateDto = new UpdateOrderStatusDto(OrderStatus.SHIPPED, PaymentStatus.COMPLETED);
        OrderResponseDto response = orderService.updateOrderStatus(100L, updateDto);

        assertNotNull(response);
        assertEquals(OrderStatus.SHIPPED, response.getOrderStatus());
        assertEquals(PaymentStatus.COMPLETED, response.getPaymentStatus());
    }

    @Test
    @DisplayName("Should cancel order and restore book stock")
    void shouldCancelOrderAndRestoreStock() {
        Order order = new Order("ORD-12345", customer, BigDecimal.valueOf(80.00), "123 Main St");
        order.setId(100L);
        com.bookstore.order.entity.OrderItem orderItem = new com.bookstore.order.entity.OrderItem(
                order, book, book.getTitle(), book.getIsbn(), 2, book.getPrice(), BigDecimal.valueOf(80.00));
        order.addItem(orderItem);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        int initialStock = book.getStockQuantity();
        OrderResponseDto response = orderService.cancelOrder(100L);

        assertNotNull(response);
        assertEquals(OrderStatus.CANCELLED, response.getOrderStatus());
        assertEquals(initialStock + 2, book.getStockQuantity());
        verify(bookRepository).save(book);
    }
}
