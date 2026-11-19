package com.bookstore.order.service;

import com.bookstore.common.PagedResponse;
import com.bookstore.order.dto.CheckoutRequestDto;
import com.bookstore.order.dto.OrderResponseDto;
import com.bookstore.order.dto.UpdateOrderStatusDto;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponseDto checkout(CheckoutRequestDto requestDto);

    OrderResponseDto getOrderById(Long orderId);

    OrderResponseDto getOrderByOrderNumber(String orderNumber);

    PagedResponse<OrderResponseDto> getOrdersByCustomerId(Long customerId, Pageable pageable);

    OrderResponseDto updateOrderStatus(Long orderId, UpdateOrderStatusDto updateDto);

    OrderResponseDto cancelOrder(Long orderId);
}
