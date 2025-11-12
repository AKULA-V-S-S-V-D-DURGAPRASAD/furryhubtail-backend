
package com.furryhub.petservices.controller;

import com.furryhub.petservices.model.dto.JwtResponse;
import com.furryhub.petservices.model.dto.LoginRequest;
import com.furryhub.petservices.model.dto.RegistrationDTO;
import com.furryhub.petservices.model.dto.ResetPasswordDTO;
import com.furryhub.petservices.service.AuthService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.authenticate(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/register/customer")
    public ResponseEntity<String> registerCustomer(@Valid @RequestBody RegistrationDTO registrationDTO) {
        authService.registerCustomer(registrationDTO);
        return ResponseEntity.ok("Customer registered successfully");
    }

    @PostMapping("/register/provider")
    public ResponseEntity<String> registerProvider(@Valid @RequestBody RegistrationDTO registrationDTO) {
        authService.registerProvider(registrationDTO);
        return ResponseEntity.ok("Provider registered successfully");
    }
    
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        String result = authService.verifyEmail(token);
        return ResponseEntity.ok(result);
    }
     
     
     @PostMapping("/forgot-password")
     public ResponseEntity<String> forgotPassword(@RequestParam("email") @NotBlank @Email String email) {
         authService.forgotPassword(email);
         return ResponseEntity.ok("Password reset instructions have been sent to your email.");
     }
     
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO) {
        authService.resetPassword(resetPasswordDTO.getToken(), resetPasswordDTO.getNewPassword());
        return ResponseEntity.ok("Password changed successfully.");
    }    
}