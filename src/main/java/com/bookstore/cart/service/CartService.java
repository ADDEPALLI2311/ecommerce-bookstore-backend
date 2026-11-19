package com.bookstore.cart.service;

import com.bookstore.cart.dto.CartItemRequestDto;
import com.bookstore.cart.dto.CartResponseDto;
import com.bookstore.cart.dto.UpdateCartItemDto;

public interface CartService {

    CartResponseDto getCartByCustomerId(Long customerId);

    CartResponseDto addItemToCart(Long customerId, CartItemRequestDto itemDto);

    CartResponseDto updateCartItemQuantity(Long customerId, Long itemId, UpdateCartItemDto updateDto);

    CartResponseDto removeItemFromCart(Long customerId, Long itemId);

    void clearCart(Long customerId);
}
