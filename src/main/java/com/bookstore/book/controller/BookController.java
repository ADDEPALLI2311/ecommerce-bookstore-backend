package com.bookstore.book.controller;

import com.bookstore.book.dto.BookRequestDto;
import com.bookstore.book.dto.BookResponseDto;
import com.bookstore.book.dto.UpdateStockDto;
import com.bookstore.book.service.BookService;
import com.bookstore.common.ApiResponse;
import com.bookstore.common.PagedResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookResponseDto>> createBook(@Valid @RequestBody BookRequestDto requestDto) {
        BookResponseDto createdBook = bookService.createBook(requestDto);
        return new ResponseEntity<>(
                ApiResponse.success("Book created successfully", createdBook),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponseDto>> getBookById(@PathVariable Long id) {
        BookResponseDto book = bookService.getBookById(id);
        return ResponseEntity.ok(ApiResponse.success(book));
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<ApiResponse<BookResponseDto>> getBookByIsbn(@PathVariable String isbn) {
        BookResponseDto book = bookService.getBookByIsbn(isbn);
        return ResponseEntity.ok(ApiResponse.success(book));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<BookResponseDto>>> getAllBooks(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<BookResponseDto> pagedBooks = bookService.getAllBooks(category, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(pagedBooks));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponseDto>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequestDto requestDto) {
        BookResponseDto updatedBook = bookService.updateBook(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success("Book updated successfully", updatedBook));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<BookResponseDto>> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockDto stockDto) {
        BookResponseDto updatedBook = bookService.updateStock(id, stockDto);
        return ResponseEntity.ok(ApiResponse.success("Book stock updated successfully", updatedBook));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
