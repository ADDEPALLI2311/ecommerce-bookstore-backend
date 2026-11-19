package com.bookstore.customer.service.impl;

import com.bookstore.common.PagedResponse;
import com.bookstore.common.exception.DuplicateResourceException;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.customer.dto.CustomerRequestDto;
import com.bookstore.customer.dto.CustomerResponseDto;
import com.bookstore.customer.entity.Customer;
import com.bookstore.customer.repository.CustomerRepository;
import com.bookstore.customer.service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public CustomerResponseDto registerCustomer(CustomerRequestDto requestDto) {
        if (customerRepository.existsByEmail(requestDto.getEmail())) {
            throw new DuplicateResourceException("Customer", "email", requestDto.getEmail());
        }

        Customer customer = new Customer();
        mapRequestDtoToEntity(requestDto, customer);
        Customer savedCustomer = customerRepository.save(customer);
        return mapEntityToResponseDto(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponseDto getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return mapEntityToResponseDto(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponseDto getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
        return mapEntityToResponseDto(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CustomerResponseDto> getAllCustomers(Pageable pageable) {
        Page<Customer> customerPage = customerRepository.findAll(pageable);
        List<CustomerResponseDto> content = customerPage.getContent().stream()
                .map(this::mapEntityToResponseDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                customerPage.getNumber(),
                customerPage.getSize(),
                customerPage.getTotalElements(),
                customerPage.getTotalPages(),
                customerPage.isLast()
        );
    }

    @Override
    public CustomerResponseDto updateCustomer(Long id, CustomerRequestDto requestDto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        if (customerRepository.existsByEmailAndIdNot(requestDto.getEmail(), id)) {
            throw new DuplicateResourceException("Customer", "email", requestDto.getEmail());
        }

        mapRequestDtoToEntity(requestDto, customer);
        Customer updatedCustomer = customerRepository.save(customer);
        return mapEntityToResponseDto(updatedCustomer);
    }

    @Override
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        customerRepository.delete(customer);
    }

    private void mapRequestDtoToEntity(CustomerRequestDto dto, Customer entity) {
        entity.setEmail(dto.getEmail());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setPhone(dto.getPhone());
        entity.setAddressLine1(dto.getAddressLine1());
        entity.setAddressLine2(dto.getAddressLine2());
        entity.setCity(dto.getCity());
        entity.setPostalCode(dto.getPostalCode());
        entity.setCountry(dto.getCountry());
    }

    private CustomerResponseDto mapEntityToResponseDto(Customer entity) {
        return new CustomerResponseDto(
                entity.getId(),
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPhone(),
                entity.getAddressLine1(),
                entity.getAddressLine2(),
                entity.getCity(),
                entity.getPostalCode(),
                entity.getCountry(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
