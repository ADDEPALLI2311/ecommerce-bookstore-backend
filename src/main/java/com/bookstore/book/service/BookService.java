package com.bookstore.book.service;

import com.bookstore.book.dto.BookRequestDto;
import com.bookstore.book.dto.BookResponseDto;
import com.bookstore.book.dto.UpdateStockDto;
import com.bookstore.common.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface BookService {

    BookResponseDto createBook(BookRequestDto requestDto);

    BookResponseDto getBookById(Long id);

    BookResponseDto getBookByIsbn(String isbn);

    PagedResponse<BookResponseDto> getAllBooks(String category, String keyword, Pageable pageable);

    BookResponseDto updateBook(Long id, BookRequestDto requestDto);

    BookResponseDto updateStock(Long id, UpdateStockDto stockDto);

    void deleteBook(Long id);
}
