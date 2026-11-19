package com.bookstore.order.dto;

import com.bookstore.order.entity.OrderStatus;
import com.bookstore.order.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateOrderStatusDto {

    @NotNull(message = "Order status is required")
    private OrderStatus orderStatus;

    private PaymentStatus paymentStatus;

    public UpdateOrderStatusDto() {
    }

    public UpdateOrderStatusDto(OrderStatus orderStatus, PaymentStatus paymentStatus) {
        this.orderStatus = orderStatus;
        this.paymentStatus = paymentStatus;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
