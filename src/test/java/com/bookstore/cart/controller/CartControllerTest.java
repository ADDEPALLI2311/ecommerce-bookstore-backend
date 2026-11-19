package com.bookstore.cart.controller;

import com.bookstore.cart.dto.CartItemRequestDto;
import com.bookstore.cart.dto.CartResponseDto;
import com.bookstore.cart.service.CartService;
import com.bookstore.common.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@Import(GlobalExceptionHandler.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    @Test
    @DisplayName("GET /api/v1/carts/customer/{customerId} - Should return cart")
    void shouldReturnCartByCustomerId() throws Exception {
        CartResponseDto cartResponse = new CartResponseDto(
                1L, 1L, "john.doe@example.com", new ArrayList<>(), BigDecimal.ZERO, 0);

        when(cartService.getCartByCustomerId(1L)).thenReturn(cartResponse);

        mockMvc.perform(get("/api/v1/carts/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.customerId").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/carts/customer/{customerId}/items - Should add item to cart")
    void shouldAddItemToCart() throws Exception {
        CartItemRequestDto request = new CartItemRequestDto(1L, 2);
        CartResponseDto cartResponse = new CartResponseDto(
                1L, 1L, "john.doe@example.com", new ArrayList<>(), BigDecimal.valueOf(90.00), 2);

        when(cartService.addItemToCart(eq(1L), any(CartItemRequestDto.class))).thenReturn(cartResponse);

        mockMvc.perform(post("/api/v1/carts/customer/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalItems").value(2));
    }

    @Test
    @DisplayName("DELETE /api/v1/carts/customer/{customerId}/clear - Should clear cart")
    void shouldClearCart() throws Exception {
        mockMvc.perform(delete("/api/v1/carts/customer/1/clear"))
                .andExpect(status().isNoContent());
    }
}
