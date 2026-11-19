package com.bookstore.customer.service;

import com.bookstore.common.PagedResponse;
import com.bookstore.common.exception.DuplicateResourceException;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.customer.dto.CustomerRequestDto;
import com.bookstore.customer.dto.CustomerResponseDto;
import com.bookstore.customer.entity.Customer;
import com.bookstore.customer.repository.CustomerRepository;
import com.bookstore.customer.service.impl.CustomerServiceImpl;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;
    private CustomerRequestDto requestDto;

    @BeforeEach
    void setUp() {
        customer = new Customer(1L, "john.doe@example.com", "John", "Doe", "+1-555-1234",
                "123 Main St", "Apt 4", "New York", "10001", "USA");

        requestDto = new CustomerRequestDto("john.doe@example.com", "John", "Doe", "+1-555-1234",
                "123 Main St", "Apt 4", "New York", "10001", "USA");
    }

    @Test
    @DisplayName("Should register customer successfully when email is unique")
    void shouldRegisterCustomerSuccessfully() {
        when(customerRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        CustomerResponseDto response = customerService.registerCustomer(requestDto);

        assertNotNull(response);
        assertEquals("john.doe@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email is already registered")
    void shouldThrowExceptionWhenEmailExists() {
        when(customerRepository.existsByEmail(requestDto.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> customerService.registerCustomer(requestDto));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should retrieve customer by ID successfully")
    void shouldGetCustomerById() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerResponseDto response = customerService.getCustomerById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John", response.getFirstName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when customer not found by ID")
    void shouldThrowExceptionWhenCustomerNotFoundById() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(99L));
    }

    @Test
    @DisplayName("Should retrieve customer by email successfully")
    void shouldGetCustomerByEmail() {
        when(customerRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(customer));

        CustomerResponseDto response = customerService.getCustomerByEmail("john.doe@example.com");

        assertNotNull(response);
        assertEquals("john.doe@example.com", response.getEmail());
    }

    @Test
    @DisplayName("Should return paginated customers")
    void shouldGetAllCustomersPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> page = new PageImpl<>(List.of(customer), pageable, 1);

        when(customerRepository.findAll(pageable)).thenReturn(page);

        PagedResponse<CustomerResponseDto> response = customerService.getAllCustomers(pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalElements());
    }

    @Test
    @DisplayName("Should delete customer successfully")
    void shouldDeleteCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        customerService.deleteCustomer(1L);

        verify(customerRepository).delete(customer);
    }
}
