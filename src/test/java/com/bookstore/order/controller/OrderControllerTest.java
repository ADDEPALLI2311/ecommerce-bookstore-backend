package com.bookstore.order.controller;

import com.bookstore.common.exception.GlobalExceptionHandler;
import com.bookstore.order.dto.CheckoutRequestDto;
import com.bookstore.order.dto.OrderResponseDto;
import com.bookstore.order.dto.UpdateOrderStatusDto;
import com.bookstore.order.entity.OrderStatus;
import com.bookstore.order.entity.PaymentStatus;
import com.bookstore.order.service.OrderService;
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
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("POST /api/v1/orders/checkout - Should place order from cart")
    void shouldCheckoutCart() throws Exception {
        CheckoutRequestDto request = new CheckoutRequestDto(1L, "123 Main St, New York, NY");
        OrderResponseDto response = new OrderResponseDto(
                100L, "ORD-20240501-ABC123", 1L, "john.doe@example.com", "John Doe",
                OrderStatus.PENDING, PaymentStatus.PENDING, BigDecimal.valueOf(90.00),
                "123 Main St, New York, NY", new ArrayList<>(),
                LocalDateTime.now(), LocalDateTime.now());

        when(orderService.checkout(any(CheckoutRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-20240501-ABC123"))
                .andExpect(jsonPath("$.data.totalAmount").value(90.00));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} - Should get order by ID")
    void shouldGetOrderById() throws Exception {
        OrderResponseDto response = new OrderResponseDto(
                100L, "ORD-20240501-ABC123", 1L, "john.doe@example.com", "John Doe",
                OrderStatus.PENDING, PaymentStatus.PENDING, BigDecimal.valueOf(90.00),
                "123 Main St, New York, NY", new ArrayList<>(),
                LocalDateTime.now(), LocalDateTime.now());

        when(orderService.getOrderById(100L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/orders/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100));
    }

    @Test
    @DisplayName("PATCH /api/v1/orders/{orderId}/status - Should update order status")
    void shouldUpdateOrderStatus() throws Exception {
        UpdateOrderStatusDto updateDto = new UpdateOrderStatusDto(OrderStatus.SHIPPED, PaymentStatus.COMPLETED);
        OrderResponseDto response = new OrderResponseDto(
                100L, "ORD-20240501-ABC123", 1L, "john.doe@example.com", "John Doe",
                OrderStatus.SHIPPED, PaymentStatus.COMPLETED, BigDecimal.valueOf(90.00),
                "123 Main St, New York, NY", new ArrayList<>(),
                LocalDateTime.now(), LocalDateTime.now());

        when(orderService.updateOrderStatus(eq(100L), any(UpdateOrderStatusDto.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/orders/100/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderStatus").value("SHIPPED"));
    }

    @Test
    @DisplayName("POST /api/v1/orders/{orderId}/cancel - Should cancel order")
    void shouldCancelOrder() throws Exception {
        OrderResponseDto response = new OrderResponseDto(
                100L, "ORD-20240501-ABC123", 1L, "john.doe@example.com", "John Doe",
                OrderStatus.CANCELLED, PaymentStatus.REFUNDED, BigDecimal.valueOf(90.00),
                "123 Main St, New York, NY", new ArrayList<>(),
                LocalDateTime.now(), LocalDateTime.now());

        when(orderService.cancelOrder(100L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders/100/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderStatus").value("CANCELLED"));
    }
}
