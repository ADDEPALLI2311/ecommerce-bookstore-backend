package com.bookstore.common.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String bookTitle, int availableStock, int requestedQuantity) {
        super(String.format("Insufficient stock for book '%s'. Available: %d, Requested: %d", bookTitle, availableStock, requestedQuantity));
    }
}
