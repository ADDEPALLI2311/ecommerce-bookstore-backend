package com.bookstore.book.service;

import com.bookstore.book.dto.BookRequestDto;
import com.bookstore.book.dto.BookResponseDto;
import com.bookstore.book.dto.UpdateStockDto;
import com.bookstore.book.entity.Book;
import com.bookstore.book.repository.BookRepository;
import com.bookstore.book.service.impl.BookServiceImpl;
import com.bookstore.common.PagedResponse;
import com.bookstore.common.exception.DuplicateResourceException;
import com.bookstore.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book;
    private BookRequestDto requestDto;

    @BeforeEach
    void setUp() {
        book = new Book(1L, "9780134685991", "Effective Java", "Joshua Bloch", "Technology",
                BigDecimal.valueOf(45.00), 20, "Best practices guide", "https://example.com/cover.jpg");

        requestDto = new BookRequestDto("9780134685991", "Effective Java", "Joshua Bloch", "Technology",
                BigDecimal.valueOf(45.00), 20, "Best practices guide", "https://example.com/cover.jpg");
    }

    @Test
    @DisplayName("Should create book successfully when ISBN is unique")
    void shouldCreateBookSuccessfully() {
        when(bookRepository.existsByIsbn(requestDto.getIsbn())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book b = invocation.getArgument(0);
            b.setId(1L);
            return b;
        });

        BookResponseDto response = bookService.createBook(requestDto);

        assertNotNull(response);
        assertEquals("Effective Java", response.getTitle());
        assertEquals("9780134685991", response.getIsbn());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when creating book with existing ISBN")
    void shouldThrowExceptionWhenIsbnExists() {
        when(bookRepository.existsByIsbn(requestDto.getIsbn())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> bookService.createBook(requestDto));
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should retrieve book by ID successfully")
    void shouldGetBookById() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BookResponseDto response = bookService.getBookById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Effective Java", response.getTitle());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when book not found by ID")
    void shouldThrowExceptionWhenBookNotFoundById() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.getBookById(99L));
    }

    @Test
    @DisplayName("Should retrieve book by ISBN successfully")
    void shouldGetBookByIsbn() {
        when(bookRepository.findByIsbn("9780134685991")).thenReturn(Optional.of(book));

        BookResponseDto response = bookService.getBookByIsbn("9780134685991");

        assertNotNull(response);
        assertEquals("9780134685991", response.getIsbn());
    }

    @Test
    @DisplayName("Should return paginated books")
    void shouldGetAllBooksPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> page = new PageImpl<>(List.of(book), pageable, 1);

        when(bookRepository.searchBooks(eq("Technology"), eq("Java"), eq(pageable))).thenReturn(page);

        PagedResponse<BookResponseDto> response = bookService.getAllBooks("Technology", "Java", pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalElements());
    }

    @Test
    @DisplayName("Should update stock quantity successfully")
    void shouldUpdateStockQuantity() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookResponseDto response = bookService.updateStock(1L, new UpdateStockDto(35));

        assertNotNull(response);
        assertEquals(35, response.getStockQuantity());
    }

    @Test
    @DisplayName("Should delete book successfully")
    void shouldDeleteBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        bookService.deleteBook(1L);

        verify(bookRepository).delete(book);
    }
}
