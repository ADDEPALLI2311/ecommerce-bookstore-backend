package com.bookstore.book.service.impl;

import com.bookstore.book.dto.BookRequestDto;
import com.bookstore.book.dto.BookResponseDto;
import com.bookstore.book.dto.UpdateStockDto;
import com.bookstore.book.entity.Book;
import com.bookstore.book.repository.BookRepository;
import com.bookstore.book.service.BookService;
import com.bookstore.common.PagedResponse;
import com.bookstore.common.exception.DuplicateResourceException;
import com.bookstore.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public BookResponseDto createBook(BookRequestDto requestDto) {
        if (bookRepository.existsByIsbn(requestDto.getIsbn())) {
            throw new DuplicateResourceException("Book", "ISBN", requestDto.getIsbn());
        }

        Book book = new Book();
        mapRequestDtoToEntity(requestDto, book);
        Book savedBook = bookRepository.save(book);
        return mapEntityToResponseDto(savedBook);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponseDto getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        return mapEntityToResponseDto(book);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponseDto getBookByIsbn(String isbn) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "isbn", isbn));
        return mapEntityToResponseDto(book);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookResponseDto> getAllBooks(String category, String keyword, Pageable pageable) {
        Page<Book> bookPage = bookRepository.searchBooks(category, keyword, pageable);
        List<BookResponseDto> content = bookPage.getContent().stream()
                .map(this::mapEntityToResponseDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                bookPage.getNumber(),
                bookPage.getSize(),
                bookPage.getTotalElements(),
                bookPage.getTotalPages(),
                bookPage.isLast()
        );
    }

    @Override
    public BookResponseDto updateBook(Long id, BookRequestDto requestDto) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));

        if (bookRepository.existsByIsbnAndIdNot(requestDto.getIsbn(), id)) {
            throw new DuplicateResourceException("Book", "ISBN", requestDto.getIsbn());
        }

        mapRequestDtoToEntity(requestDto, book);
        Book updatedBook = bookRepository.save(book);
        return mapEntityToResponseDto(updatedBook);
    }

    @Override
    public BookResponseDto updateStock(Long id, UpdateStockDto stockDto) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));

        book.setStockQuantity(stockDto.getStockQuantity());
        Book updatedBook = bookRepository.save(book);
        return mapEntityToResponseDto(updatedBook);
    }

    @Override
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        bookRepository.delete(book);
    }

    private void mapRequestDtoToEntity(BookRequestDto dto, Book entity) {
        entity.setIsbn(dto.getIsbn());
        entity.setTitle(dto.getTitle());
        entity.setAuthor(dto.getAuthor());
        entity.setCategory(dto.getCategory());
        entity.setPrice(dto.getPrice());
        entity.setStockQuantity(dto.getStockQuantity());
        entity.setDescription(dto.getDescription());
        entity.setCoverImageUrl(dto.getCoverImageUrl());
    }

    private BookResponseDto mapEntityToResponseDto(Book entity) {
        return new BookResponseDto(
                entity.getId(),
                entity.getIsbn(),
                entity.getTitle(),
                entity.getAuthor(),
                entity.getCategory(),
                entity.getPrice(),
                entity.getStockQuantity(),
                entity.getDescription(),
                entity.getCoverImageUrl(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
