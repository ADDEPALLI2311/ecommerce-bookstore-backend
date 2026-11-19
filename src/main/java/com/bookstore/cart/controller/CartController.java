package com.bookstore.cart.controller;

import com.bookstore.cart.dto.CartItemRequestDto;
import com.bookstore.cart.dto.CartResponseDto;
import com.bookstore.cart.dto.UpdateCartItemDto;
import com.bookstore.cart.service.CartService;
import com.bookstore.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<CartResponseDto>> getCartByCustomerId(@PathVariable Long customerId) {
        CartResponseDto cart = cartService.getCartByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/customer/{customerId}/items")
    public ResponseEntity<ApiResponse<CartResponseDto>> addItemToCart(
            @PathVariable Long customerId,
            @Valid @RequestBody CartItemRequestDto itemDto) {
        CartResponseDto cart = cartService.addItemToCart(customerId, itemDto);
        return new ResponseEntity<>(
                ApiResponse.success("Item added to cart successfully", cart),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/customer/{customerId}/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponseDto>> updateCartItemQuantity(
            @PathVariable Long customerId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemDto updateDto) {
        CartResponseDto cart = cartService.updateCartItemQuantity(customerId, itemId, updateDto);
        return ResponseEntity.ok(ApiResponse.success("Cart item quantity updated successfully", cart));
    }

    @DeleteMapping("/customer/{customerId}/items/{itemId}")
    public ResponseEntity<Void> removeItemFromCart(
            @PathVariable Long customerId,
            @PathVariable Long itemId) {
        cartService.removeItemFromCart(customerId, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/customer/{customerId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable Long customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build();
    }
}
