
package com.furryhub.petservices.service;

import com.furryhub.petservices.exception.InvalidTokenException;
import com.furryhub.petservices.exception.TokenExpiredException;
import com.furryhub.petservices.exception.UserNotFoundException;
import com.furryhub.petservices.exception.ValidationException;
import com.furryhub.petservices.model.dto.JwtResponse;
import com.furryhub.petservices.model.dto.LoginRequest;
import com.furryhub.petservices.model.dto.RegistrationDTO;
import com.furryhub.petservices.model.entity.*;
import com.furryhub.petservices.model.entity.User;
import com.furryhub.petservices.repository.CustomerRepository;
import com.furryhub.petservices.repository.ProviderRepository;
import com.furryhub.petservices.repository.UserRepository;
import com.furryhub.petservices.util.JwtTokenUtil;
import com.furryhub.petservices.exception.AuthenticationFailedException;
import com.furryhub.petservices.exception.DuplicateResourceException;

import jakarta.transaction.Transactional;


import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;


@Service
public class AuthService {
	
	private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

	@Value("${app.frontend.base-url}")
    private String frontendBaseUrl;
	
	@Value("${baseUrl}")
	private String baseUrl;
    
	@Autowired
	EmailNotificationService emailNotificationService;
	
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private final GeometryFactory geometryFactory;


    public AuthService(GeometryFactory geometryFactory) {
        // assign existing fields...
        this.geometryFactory = geometryFactory;

    }
    
    @Transactional
    public JwtResponse authenticate(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(), loginRequest.getPassword())
            );

            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
            String token = jwtTokenUtil.generateToken(user);

            return new JwtResponse(token, "Bearer", user.getRole().name(), user.getFirstName(), user.getLastName());
        } catch (BadCredentialsException e) {
            throw new AuthenticationFailedException("Invalid email or password.");
        } catch (DisabledException e) {
            throw new AuthenticationFailedException("Account is disabled.");
        } catch (Exception e) {
            throw new AuthenticationFailedException("Authentication failed.");
        }
    }

    @Transactional
    public void registerCustomer(RegistrationDTO registrationDTO) {

        if (registrationDTO == null) {
            throw new IllegalArgumentException("Registration data must not be null");
        }
        if (registrationDTO.getEmail() == null || registrationDTO.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        if (registrationDTO.getPassword() == null || registrationDTO.getPassword().trim().isEmpty()) {
            throw new ValidationException("Password is required");
        }
        if (registrationDTO.getFirstName() == null || registrationDTO.getFirstName().trim().isEmpty()) {
            throw new ValidationException("First name is required");
        }
        if (registrationDTO.getLastName() == null || registrationDTO.getLastName().trim().isEmpty()) {
            throw new ValidationException("Last name is required");
        }
        if (registrationDTO.getLatitude() == null) {
            throw new ValidationException("Latitude is required");
        }
        if (registrationDTO.getLongitude() == null) {
            throw new ValidationException("Longitude is required");
        }

        if (userRepository.findByEmail(registrationDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already in use");
        }

        try {
            
            User user = new User();
            String verificationToken = UUID.randomUUID().toString();
            user.setEmail(registrationDTO.getEmail());
            user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
            user.setFirstName(registrationDTO.getFirstName());
            user.setLastName(registrationDTO.getLastName());
            user.setRole(Role.CUSTOMER);
            user.setIsEmailVerified(false);
            user.setVerificationToken(verificationToken);
            user.setVerificationExpiry(LocalDateTime.now().plusDays(1));
            userRepository.save(user); 
            Customer customer = new Customer();
            customer.setUser(user);
            customer.setAddress(registrationDTO.getAddress());
            customer.setPhoneNumber(registrationDTO.getPhoneNumber());

            Point location=geometryFactory.createPoint(new Coordinate(registrationDTO.getLongitude(),registrationDTO.getLatitude()));
            location.setSRID(4326);
            customer.setLocation(location);

            customerRepository.save(customer);

            emailNotificationService.registerCustomerNotification(user, verificationToken);


        } catch (DataAccessException dae) {
            // Handle database exceptions
            logger.error("Database error occurred during customer registration", dae);
            throw new ServiceException("An error occurred while processing your registration. Please try again later.");
        } catch (MailException me) {
            // Handle email sending exceptions
            logger.error("Error sending verification email to {}", registrationDTO.getEmail(), me);
            throw new ServiceException("Failed to send verification email. Please check your email address.");
        } catch (Exception e) {
            // Catch any other exceptions
            logger.error("Unexpected error occurred during customer registration", e);
            throw new ServiceException("An unexpected error occurred. Please try again later.");
        }
    }
   
    
    private void validateRegistrationDTO(RegistrationDTO registrationDTO) {
        if (registrationDTO == null) {
            throw new ValidationException("Registration data must not be null");
        }
        if (registrationDTO.getEmail() == null || registrationDTO.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }

        if (registrationDTO.getPassword() == null || registrationDTO.getPassword().trim().isEmpty()) {
            throw new ValidationException("Password is required");
        }
        if (registrationDTO.getPassword().length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long");
        }
        if (registrationDTO.getFirstName() == null || registrationDTO.getFirstName().trim().isEmpty()) {
            throw new ValidationException("First name is required");
        }
        if (registrationDTO.getLastName() == null || registrationDTO.getLastName().trim().isEmpty()) {
            throw new ValidationException("Last name is required");
        }
       if (registrationDTO.getSpecialization() == null || registrationDTO.getSpecialization().trim().isEmpty()) {
            throw new ValidationException("Specialization is required");
        }
        if (registrationDTO.getExperience() == null || registrationDTO.getExperience() < 0) {
            throw new ValidationException("Experience must be a non-negative number");
        }
        if (registrationDTO.getLicenseNumber() == null || registrationDTO.getLicenseNumber().trim().isEmpty()) {
            throw new ValidationException("License number is required");
        }
        if (registrationDTO.getLatitude() == null) {
            throw new ValidationException("Latitude is required");
        }
        if (registrationDTO.getLongitude() == null) {
            throw new ValidationException("Longitude is required");
        }

    }

    @Transactional
    public void registerProvider(RegistrationDTO registrationDTO) {
        validateRegistrationDTO(registrationDTO);
        if (userRepository.findByEmail(registrationDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already in use");
        }

        try {
            // Create new User
            User user = new User();
            String verificationToken = UUID.randomUUID().toString();
            user.setEmail(registrationDTO.getEmail());
            user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
            user.setFirstName(registrationDTO.getFirstName());
            user.setLastName(registrationDTO.getLastName());
            user.setRole(Role.PROVIDER);
            user.setIsEmailVerified(false);
            user.setVerificationToken(verificationToken);
            user.setVerificationExpiry(LocalDateTime.now().plusDays(1));

            userRepository.save(user);
            logger.info("User saved with ID: {}", user.getId());

            // Create new Provider
            Provider provider = new Provider();
            provider.setUser(user);
            provider.setSpecialization(registrationDTO.getSpecialization());
            provider.setExperience(registrationDTO.getExperience());
            provider.setLicenseNumber(registrationDTO.getLicenseNumber());
            provider.setPetStoreName(registrationDTO.getPetStoreName());
            provider.setPetClinicLocation(registrationDTO.getPetClinicLocation());
            provider.setPhoneNumber(registrationDTO.getPhoneNumber());
            provider.setAddress(registrationDTO.getAddress());

            Point location=geometryFactory.createPoint(new Coordinate(registrationDTO.getLongitude(),registrationDTO.getLatitude()));
            location.setSRID(4326);
            provider.setLocation(location);

            providerRepository.save(provider);
            logger.info("Provider profile created for User ID: {}", user.getId());

            emailNotificationService.registerProviderNotification(user, verificationToken);


        } catch (DataAccessException dae) {
            // Handle database exceptions
            logger.error("Database error occurred during provider registration", dae);
            throw new ServiceException("An error occurred while processing your registration. Please try again later.");
        } catch (MailException me) {
            // Handle email sending exceptions
            logger.error("Error sending verification email to {}", registrationDTO.getEmail(), me);
            throw new ServiceException("Failed to send verification email. Please check your email address.");
        }catch (Exception e) {
            // Catch any other exceptions
            logger.error("Unexpected error occurred during provider registration", e);
            throw new ServiceException("An unexpected error occurred. Please try again later.");
        }
    }

    
    @Transactional
    public String verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
        		    .orElseThrow(()-> new InvalidTokenException("Invalid Token"));

        if (user.getVerificationExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Verification token has expired.");
        }
        user.setIsEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationExpiry(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return "Email verified successfully.";
        }

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found."));

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusDays(1));
        userRepository.save(user);
        emailNotificationService.resetPasswordNotification(user, resetToken);  
    }

	@Transactional
    public void resetPassword(String resetToken, String newPassword) {
        User user = userRepository.findByResetToken(resetToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid reset token."));
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Reset token has expired.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
		
	}
	