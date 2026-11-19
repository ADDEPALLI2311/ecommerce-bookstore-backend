package com.bookstore.book.controller;

import com.bookstore.book.dto.BookRequestDto;
import com.bookstore.book.dto.BookResponseDto;
import com.bookstore.book.service.BookService;
import com.bookstore.common.exception.GlobalExceptionHandler;
import com.bookstore.common.exception.ResourceNotFoundException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(GlobalExceptionHandler.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @Test
    @DisplayName("POST /api/v1/books - Should create book with valid input")
    void shouldCreateBookWhenValidInput() throws Exception {
        BookRequestDto requestDto = new BookRequestDto(
                "9780134685991", "Effective Java", "Joshua Bloch", "Technology",
                BigDecimal.valueOf(45.00), 20, "Guide to Java", null);

        BookResponseDto responseDto = new BookResponseDto(
                1L, "9780134685991", "Effective Java", "Joshua Bloch", "Technology",
                BigDecimal.valueOf(45.00), 20, "Guide to Java", null,
                LocalDateTime.now(), LocalDateTime.now());

        when(bookService.createBook(any(BookRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Effective Java"))
                .andExpect(jsonPath("$.data.isbn").value("9780134685991"));
    }

    @Test
    @DisplayName("POST /api/v1/books - Should return 400 Bad Request when validation fails")
    void shouldReturnBadRequestWhenInvalidInput() throws Exception {
        BookRequestDto invalidDto = new BookRequestDto(
                "", "", "", "",
                BigDecimal.valueOf(-5.00), -1, null, null);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.validationErrors.isbn").exists())
                .andExpect(jsonPath("$.validationErrors.title").exists())
                .andExpect(jsonPath("$.validationErrors.price").exists());
    }

    @Test
    @DisplayName("GET /api/v1/books/{id} - Should return book when found")
    void shouldReturnBookById() throws Exception {
        BookResponseDto responseDto = new BookResponseDto(
                1L, "9780134685991", "Effective Java", "Joshua Bloch", "Technology",
                BigDecimal.valueOf(45.00), 20, "Guide to Java", null,
                LocalDateTime.now(), LocalDateTime.now());

        when(bookService.getBookById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Effective Java"));
    }

    @Test
    @DisplayName("GET /api/v1/books/{id} - Should return 404 when book not found")
    void shouldReturnNotFoundWhenBookMissing() throws Exception {
        when(bookService.getBookById(99L))
                .thenThrow(new ResourceNotFoundException("Book", "id", 99L));

        mockMvc.perform(get("/api/v1/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /api/v1/books/{id} - Should return 204 No Content")
    void shouldDeleteBook() throws Exception {
        mockMvc.perform(delete("/api/v1/books/1"))
                .andExpect(status().isNoContent());
    }
}
