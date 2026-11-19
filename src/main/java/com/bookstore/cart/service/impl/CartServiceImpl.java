package com.bookstore.cart.service.impl;

import com.bookstore.book.entity.Book;
import com.bookstore.book.repository.BookRepository;
import com.bookstore.cart.dto.CartItemRequestDto;
import com.bookstore.cart.dto.CartItemResponseDto;
import com.bookstore.cart.dto.CartResponseDto;
import com.bookstore.cart.dto.UpdateCartItemDto;
import com.bookstore.cart.entity.Cart;
import com.bookstore.cart.entity.CartItem;
import com.bookstore.cart.repository.CartItemRepository;
import com.bookstore.cart.repository.CartRepository;
import com.bookstore.cart.service.CartService;
import com.bookstore.common.exception.BadRequestException;
import com.bookstore.common.exception.InsufficientStockException;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.customer.entity.Customer;
import com.bookstore.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final BookRepository bookRepository;

    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           CustomerRepository customerRepository,
                           BookRepository bookRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.customerRepository = customerRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public CartResponseDto getCartByCustomerId(Long customerId) {
        Cart cart = getOrCreateCart(customerId);
        return mapEntityToResponseDto(cart);
    }

    @Override
    public CartResponseDto addItemToCart(Long customerId, CartItemRequestDto itemDto) {
        Cart cart = getOrCreateCart(customerId);

        Book book = bookRepository.findById(itemDto.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", itemDto.getBookId()));

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getBook().getId().equals(book.getId()))
                .findFirst();

        int targetQuantity = itemDto.getQuantity();
        if (existingItemOpt.isPresent()) {
            targetQuantity += existingItemOpt.get().getQuantity();
        }

        if (book.getStockQuantity() < targetQuantity) {
            throw new InsufficientStockException(book.getTitle(), book.getStockQuantity(), targetQuantity);
        }

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(targetQuantity);
            existingItem.setUnitPrice(book.getPrice());
        } else {
            CartItem newItem = new CartItem(cart, book, itemDto.getQuantity(), book.getPrice());
            cart.addItem(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return mapEntityToResponseDto(savedCart);
    }

    @Override
    public CartResponseDto updateCartItemQuantity(Long customerId, Long itemId, UpdateCartItemDto updateDto) {
        Cart cart = getOrCreateCart(customerId);

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        Book book = cartItem.getBook();
        if (book.getStockQuantity() < updateDto.getQuantity()) {
            throw new InsufficientStockException(book.getTitle(), book.getStockQuantity(), updateDto.getQuantity());
        }

        cartItem.setQuantity(updateDto.getQuantity());
        cartItem.setUnitPrice(book.getPrice());

        Cart savedCart = cartRepository.save(cart);
        return mapEntityToResponseDto(savedCart);
    }

    @Override
    public CartResponseDto removeItemFromCart(Long customerId, Long itemId) {
        Cart cart = getOrCreateCart(customerId);

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        cart.removeItem(cartItem);
        Cart savedCart = cartRepository.save(cart);
        return mapEntityToResponseDto(savedCart);
    }

    @Override
    public void clearCart(Long customerId) {
        Cart cart = getOrCreateCart(customerId);
        cart.clearItems();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Cart newCart = new Cart(customer);
                    return cartRepository.save(newCart);
                });
    }

    private CartResponseDto mapEntityToResponseDto(Cart cart) {
        List<CartItemResponseDto> itemDtos = cart.getItems().stream()
                .map(this::mapItemEntityToDto)
                .collect(Collectors.toList());

        return new CartResponseDto(
                cart.getId(),
                cart.getCustomer().getId(),
                cart.getCustomer().getEmail(),
                itemDtos,
                cart.getTotalAmount(),
                cart.getTotalItems()
        );
    }

    private CartItemResponseDto mapItemEntityToDto(CartItem item) {
        return new CartItemResponseDto(
                item.getId(),
                item.getBook().getId(),
                item.getBook().getTitle(),
                item.getBook().getIsbn(),
                item.getBook().getAuthor(),
                item.getBook().getCoverImageUrl(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}
