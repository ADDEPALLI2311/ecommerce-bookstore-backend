package com.bookstore.customer.service;

import com.bookstore.common.PagedResponse;
import com.bookstore.customer.dto.CustomerRequestDto;
import com.bookstore.customer.dto.CustomerResponseDto;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    CustomerResponseDto registerCustomer(CustomerRequestDto requestDto);

    CustomerResponseDto getCustomerById(Long id);

    CustomerResponseDto getCustomerByEmail(String email);

    PagedResponse<CustomerResponseDto> getAllCustomers(Pageable pageable);

    CustomerResponseDto updateCustomer(Long id, CustomerRequestDto requestDto);

    void deleteCustomer(Long id);
}
