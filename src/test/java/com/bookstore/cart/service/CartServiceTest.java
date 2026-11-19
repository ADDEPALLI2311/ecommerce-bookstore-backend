package com.bookstore.cart.service;

import com.bookstore.book.entity.Book;
import com.bookstore.book.repository.BookRepository;
import com.bookstore.cart.dto.CartItemRequestDto;
import com.bookstore.cart.dto.CartResponseDto;
import com.bookstore.cart.dto.UpdateCartItemDto;
import com.bookstore.cart.entity.Cart;
import com.bookstore.cart.entity.CartItem;
import com.bookstore.cart.repository.CartItemRepository;
import com.bookstore.cart.repository.CartRepository;
import com.bookstore.cart.service.impl.CartServiceImpl;
import com.bookstore.common.exception.InsufficientStockException;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.customer.entity.Customer;
import com.bookstore.customer.repository.CustomerRepository;
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
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Customer customer;
    private Book book;
    private Cart cart;

    @BeforeEach
    void setUp() {
        customer = new Customer(1L, "john.doe@example.com", "John", "Doe", null, null, null, null, null, null);
        book = new Book(1L, "9780134685991", "Effective Java", "Joshua Bloch", "Tech", BigDecimal.valueOf(50.00), 10, null, null);
        cart = new Cart(customer);
        cart.setId(1L);
    }

    @Test
    @DisplayName("Should retrieve cart for customer, creating one if not present")
    void shouldGetCartByCustomerId() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));

        CartResponseDto response = cartService.getCartByCustomerId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getCustomerId());
    }

    @Test
    @DisplayName("Should add new item to cart successfully")
    void shouldAddItemToCartSuccessfully() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        CartItemRequestDto request = new CartItemRequestDto(1L, 2);
        CartResponseDto response = cartService.addItemToCart(1L, request);

        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertEquals(2, response.getTotalItems());
        assertEquals(BigDecimal.valueOf(100.00), response.getTotalAmount());
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when adding item exceeding available stock")
    void shouldThrowExceptionWhenInsufficientStock() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        CartItemRequestDto request = new CartItemRequestDto(1L, 50); // Available is 10

        assertThrows(InsufficientStockException.class, () -> cartService.addItemToCart(1L, request));
    }

    @Test
    @DisplayName("Should update cart item quantity successfully")
    void shouldUpdateCartItemQuantity() {
        CartItem cartItem = new CartItem(cart, book, 2, book.getPrice());
        cartItem.setId(10L);
        cart.addItem(cartItem);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        CartResponseDto response = cartService.updateCartItemQuantity(1L, 10L, new UpdateCartItemDto(5));

        assertNotNull(response);
        assertEquals(5, response.getTotalItems());
        assertEquals(BigDecimal.valueOf(250.00), response.getTotalAmount());
    }

    @Test
    @DisplayName("Should remove item from cart successfully")
    void shouldRemoveItemFromCart() {
        CartItem cartItem = new CartItem(cart, book, 2, book.getPrice());
        cartItem.setId(10L);
        cart.addItem(cartItem);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        CartResponseDto response = cartService.removeItemFromCart(1L, 10L);

        assertNotNull(response);
        assertEquals(0, response.getItems().size());
    }

    @Test
    @DisplayName("Should clear all items from cart")
    void shouldClearCart() {
        CartItem cartItem = new CartItem(cart, book, 2, book.getPrice());
        cartItem.setId(10L);
        cart.addItem(cartItem);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));

        cartService.clearCart(1L);

        verify(cartRepository).save(cart);
        assertEquals(0, cart.getItems().size());
    }
}
