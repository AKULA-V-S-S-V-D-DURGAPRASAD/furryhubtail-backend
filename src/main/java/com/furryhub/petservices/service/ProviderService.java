package com.furryhub.petservices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.furryhub.petservices.exception.ResourceNotFoundException;
import com.furryhub.petservices.model.dto.ProviderProfileDTO;
import com.furryhub.petservices.model.entity.Provider;
import com.furryhub.petservices.model.entity.User;
import com.furryhub.petservices.repository.ProviderRepository;
import com.furryhub.petservices.repository.UserRepository;

@Service
public class ProviderService {

	@Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private UserRepository userRepository;

    public void deleteCustomer(Long id) {
        // Fetch the customer from the database
        Provider customer = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));

        // Delete associated user if required
        User user = customer.getUser();
        if (user != null) {
            userRepository.delete(user);
        }

        // Delete the customer
        providerRepository.delete(customer);
    }

    public Provider getProviderByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return providerRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found for user email: " + email));
    }

    public ProviderProfileDTO getProviderProfile(String email) {
        Provider provider = getProviderByEmail(email);
        return ProviderProfileDTO.builder()
                .id(provider.getId())
                .email(provider.getUser().getEmail())
                .firstName(provider.getUser().getFirstName())
                .lastName(provider.getUser().getLastName())
                .phoneNumber(provider.getPhoneNumber())
                .specialization(provider.getSpecialization())
                .experience(provider.getExperience())
                .licenseNumber(provider.getLicenseNumber())
                .petStoreName(provider.getPetStoreName())
                .rating(provider.getRating())
                .petClinicLocation(provider.getPetClinicLocation())
                .fieldType(provider.getFieldType())
                .address(provider.getAddress())
                .city(provider.getCity())
                .houseVisit(provider.getHouseVisit())
                .businessContactNumber(provider.getBusinessContactNumber())
                .onlineService(provider.getOnlineService())
                .build();
    }

}
