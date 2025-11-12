package com.furryhub.petservices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.furryhub.petservices.exception.ResourceNotFoundException;
import com.furryhub.petservices.model.dto.CustomerProfileDTO;
import com.furryhub.petservices.model.entity.Customer;
import com.furryhub.petservices.model.entity.User;
import com.furryhub.petservices.repository.CustomerRepository;
import com.furryhub.petservices.repository.UserRepository;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    public void deleteCustomer(Long id) {
        // Fetch the customer from the database
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));

        // Delete associated user if required
        User user = customer.getUser();
        if (user != null) {
            userRepository.delete(user);
        }

        // Delete the customer
        customerRepository.delete(customer);
    }

    public Customer getCustomerByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return customerRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for user email: " + email));
    }

    public CustomerProfileDTO getCustomerProfile(String email) {
        Customer customer = getCustomerByEmail(email);
        return CustomerProfileDTO.builder()
                .id(customer.getId())
                .email(customer.getUser().getEmail())
                .firstName(customer.getUser().getFirstName())
                .lastName(customer.getUser().getLastName())
                .phoneNumber(customer.getPhoneNumber())
                .address(customer.getAddress())
                .build();
    }
}