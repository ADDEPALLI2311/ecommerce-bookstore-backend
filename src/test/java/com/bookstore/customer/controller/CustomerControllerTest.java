package com.bookstore.customer.controller;

import com.bookstore.common.exception.GlobalExceptionHandler;
import com.bookstore.customer.dto.CustomerRequestDto;
import com.bookstore.customer.dto.CustomerResponseDto;
import com.bookstore.customer.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@Import(GlobalExceptionHandler.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @Test
    @DisplayName("POST /api/v1/customers - Should register customer with valid input")
    void shouldRegisterCustomerWhenValidInput() throws Exception {
        CustomerRequestDto requestDto = new CustomerRequestDto(
                "john.doe@example.com", "John", "Doe", "+1-555-1234",
                "123 Main St", "Apt 4", "New York", "10001", "USA");

        CustomerResponseDto responseDto = new CustomerResponseDto(
                1L, "john.doe@example.com", "John", "Doe", "+1-555-1234",
                "123 Main St", "Apt 4", "New York", "10001", "USA",
                LocalDateTime.now(), LocalDateTime.now());

        when(customerService.registerCustomer(any(CustomerRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("John"));
    }

    @Test
    @DisplayName("POST /api/v1/customers - Should return 400 when email format is invalid")
    void shouldReturnBadRequestWhenInvalidEmail() throws Exception {
        CustomerRequestDto invalidDto = new CustomerRequestDto(
                "not-an-email", "", "", null, null, null, null, null, null);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    @DisplayName("GET /api/v1/customers/{id} - Should return customer by ID")
    void shouldReturnCustomerById() throws Exception {
        CustomerResponseDto responseDto = new CustomerResponseDto(
                1L, "john.doe@example.com", "John", "Doe", "+1-555-1234",
                "123 Main St", "Apt 4", "New York", "10001", "USA",
                LocalDateTime.now(), LocalDateTime.now());

        when(customerService.getCustomerById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));
    }
}
