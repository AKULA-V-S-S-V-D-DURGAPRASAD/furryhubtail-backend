package com.furryhub.petservices.controller;
import com.furryhub.petservices.exception.ResourceNotFoundException;
import com.furryhub.petservices.model.dto.BookingDTO;
import com.furryhub.petservices.model.dto.CompleteRequestDTO;
import com.furryhub.petservices.model.dto.PackageRequestDTO;
import com.furryhub.petservices.model.dto.ProviderProfileDTO;
import com.furryhub.petservices.model.entity.Booking;
import com.furryhub.petservices.model.entity.Customer;
import com.furryhub.petservices.repository.BookingRepository;
import com.furryhub.petservices.service.ProviderBookingService;
import com.furryhub.petservices.service.PackageService;
import com.furryhub.petservices.service.ProviderService;
import com.furryhub.petservices.service.SmSNotificationService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/provider")
@Slf4j
public class ProviderController{	   
	    @Autowired
	    private PackageService packageService;
	    @Autowired
		private BookingRepository bookingRepository;
		@Autowired
		 private ProviderBookingService providerBookingService;
		@Autowired
		 private ProviderService providerService;
		
		@Autowired
		private SmSNotificationService smsServiceNotification;


	@GetMapping("/bookings")
	public ResponseEntity<List<BookingDTO>> getProviderBookings(
			Principal principal,
			@RequestParam(required = false) Double latitude,
			@RequestParam(required = false) Double longitude
	) {
		if (principal == null) return ResponseEntity.status(401).build();
		String providerEmail = principal.getName();
		List<BookingDTO> bookings = providerBookingService.getProviderBookings(providerEmail, latitude, longitude);
		return ResponseEntity.ok(bookings);
	}

	@GetMapping("/profile")
	public ResponseEntity<ProviderProfileDTO> getProviderProfile(Principal principal) {
		if (principal == null) return ResponseEntity.status(401).build();
		String providerEmail = principal.getName();
		ProviderProfileDTO providerProfile = providerService.getProviderProfile(providerEmail);
		return ResponseEntity.ok(providerProfile);
	}


	@PostMapping("/add")
    public ResponseEntity<PackageRequestDTO> addPackage(@Valid @RequestBody PackageRequestDTO packageRequestDTO) {
		PackageRequestDTO aPackage = packageService.addPackage(
	            packageRequestDTO.getName(),
	            packageRequestDTO.getDescription(),
	            packageRequestDTO.getPrice(),
	            packageRequestDTO.getDuration()
	        );
	        return ResponseEntity.ok(aPackage);
	    }

	@GetMapping("/all")
	public ResponseEntity<List<PackageRequestDTO>> getAllPackages() {
		return ResponseEntity.ok(packageService.getAllPackages());
	}
	
	
	  @PutMapping("/{bookingId}/confirm")
	  public ResponseEntity<?> confirmBooking(
	          @PathVariable Long bookingId,
	          Principal principal
	  ) {
	      try {
	          if (principal == null) return ResponseEntity.status(401).build();
	          String providerEmail = principal.getName();
	          BookingDTO bookingDTO = providerBookingService.confirmBooking(bookingId, providerEmail);
	          return ResponseEntity.ok(bookingDTO);
	      } catch (ResponseStatusException e) {
	          // Return structured error response for better frontend handling
	          Map<String, Object> error = new HashMap<>();
	          error.put("error", e.getReason());
	          error.put("status", e.getStatusCode().value());
	          error.put("bookingId", bookingId);
	          return ResponseEntity.status(e.getStatusCode()).body(error);
	      } catch (ResourceNotFoundException e) {
	          Map<String, Object> error = new HashMap<>();
	          error.put("error", "Booking not found");
	          error.put("status", 404);
	          error.put("bookingId", bookingId);
	          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	      } catch (Exception e) {
	          e.printStackTrace(); // Log the exception for debugging
	          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while confirming the booking.");
	      }
	  }

    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(
            @PathVariable Long bookingId,
            Principal principal
    ) {
        try {
            if (principal == null) return ResponseEntity.status(401).build();
            String providerEmail=principal.getName();
            BookingDTO bookingDTO = providerBookingService.cancelBooking(bookingId, providerEmail);
            Booking booking = bookingRepository.findBookingWithCustomer(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
            return ResponseEntity.ok(bookingDTO);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null); // Handle appropriately
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null); // Handle appropriately
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while cancelling the booking.");
        }
    }

    @PutMapping("/{bookingId}/complete")
    public ResponseEntity<BookingDTO> completeBooking(@PathVariable Long bookingId,
                                                      Principal principal,
                                                      @RequestBody CompleteRequestDTO completeRequestDTO){
        try {
            if (principal == null) return ResponseEntity.status(401).build();
            String providerEmail=principal.getName();
            BookingDTO bookingDTO = providerBookingService.completeBooking(bookingId, providerEmail, completeRequestDTO.getOtp());
            Booking booking = bookingRepository.findBookingWithCustomer(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
            return ResponseEntity.ok(bookingDTO);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null); // Handle appropriately
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null); // Handle appropriately
        }

    }
	  
	  
	  @DeleteMapping("/{id}")
	    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
	        try {
	        	providerService.deleteCustomer(id);
	            return ResponseEntity.noContent().build(); // 204 No Content on successful deletion
	        } catch (ResourceNotFoundException e) {
	            return ResponseEntity.notFound().build(); // 404 Not Found if customer does not exist
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error for unexpected errors
	        }
	    }

	
}