package com.furryhub.petservices.controller;

import com.furryhub.petservices.exception.ResourceNotFoundException;
import com.furryhub.petservices.model.dto.BookingDTO;
import com.furryhub.petservices.model.dto.CustomerProfileDTO;
import com.furryhub.petservices.model.dto.PetDTO;
import com.furryhub.petservices.model.entity.Customer;
import com.furryhub.petservices.service.CustomerBookingService;
import com.furryhub.petservices.service.ProviderBookingService;
import com.furryhub.petservices.service.CustomerService;

import com.furryhub.petservices.service.PetService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerController{
	
	  @Autowired
	  private CustomerBookingService customerBookingService;
       
	  @Autowired
	    private CustomerService customerService;

	  @Autowired
	  private PetService petService;
	  
	  @PostMapping("/create")
	  public ResponseEntity<BookingDTO> createBooking(
              Principal principal, @RequestParam Long packageId)
	  {
              if (principal == null) return ResponseEntity.status(401).build();
              String customerEmail=principal.getName();
	          BookingDTO bookingDTO = customerBookingService.createBooking(customerEmail, packageId);
	          return ResponseEntity.ok(bookingDTO);	 
	  }

    @PostMapping("/cancel/{bookingId}")
      public ResponseEntity<BookingDTO> cancelBooking(@PathVariable Long bookingId,Principal principal){
          if(principal==null) return ResponseEntity.status(401).build();
          String CustomerEmail=principal.getName();
          BookingDTO bookingDTO=customerBookingService.cancelBooking(bookingId,CustomerEmail);
          return ResponseEntity.ok(bookingDTO);
    }

	  @GetMapping("/by-customer-email")
	    public ResponseEntity<List<BookingDTO>> getBookingsByCustomerEmail(
	            @RequestParam String customerEmail
	    ) {
	        	List<BookingDTO> bookings = customerBookingService.getBookingsByCustomerEmail(customerEmail);
	            return ResponseEntity.ok(bookings);
	     
	    }

		@PostMapping("/addPet")
		public ResponseEntity<PetDTO> addPet(Principal principal, @RequestBody PetDTO petDTO){
			if (principal == null) return ResponseEntity.status(401).build();
		  PetDTO petdto= petService.createPet(principal.getName(),petDTO);
		  return ResponseEntity.ok(petdto);
		}

		@DeleteMapping("/deletePet/{petId}")
		public ResponseEntity<Void>deletePet(Principal principal,@PathVariable Long petId){
			if (principal == null) return ResponseEntity.status(401).build();
		  petService.deletePet(principal,petId);
		  return ResponseEntity.noContent().build();
		}

		@PutMapping("/updatePet/{petId}")
		public ResponseEntity<PetDTO> updatePet(Principal principal, @PathVariable Long petId, @RequestBody PetDTO petDTO){
			if (principal == null) return ResponseEntity.status(401).build();
			PetDTO updatedPet = petService.updatePet(principal.getName(), petId, petDTO);
			return ResponseEntity.ok(updatedPet);
		}

	@GetMapping("/{customerId}/pets")
	public ResponseEntity<List<PetDTO>> getCustomerPets(@PathVariable Long customerId) {
		List<PetDTO> pets = petService.getPetsByCustomer(customerId);
		return ResponseEntity.ok(pets);
	}

	@GetMapping("/profile")
	public ResponseEntity<CustomerProfileDTO> getCustomerProfile(Principal principal) {
		if (principal == null) return ResponseEntity.status(401).build();
		String customerEmail = principal.getName();
		CustomerProfileDTO customerProfile = customerService.getCustomerProfile(customerEmail);
		return ResponseEntity.ok(customerProfile);
	}

	@GetMapping("/address")
	public ResponseEntity<Map<String, String>> getCustomerAddress(Principal principal) {
		if (principal == null) return ResponseEntity.status(401).build();
		String customerEmail = principal.getName();
		Customer customer = customerService.getCustomerByEmail(customerEmail);
		Map<String, String> addressResponse = new HashMap<>();
		addressResponse.put("address", customer.getAddress() != null ? customer.getAddress() : "");
		return ResponseEntity.ok(addressResponse);
	}
	  
	  @DeleteMapping("/{id}")
	    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
	        try {
	            customerService.deleteCustomer(id);
	            return ResponseEntity.noContent().build(); // 204 No Content on successful deletion
	        } catch (ResourceNotFoundException e) {
	            return ResponseEntity.notFound().build(); // 404 Not Found if customer does not exist
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error for unexpected errors
	        }
	    }
}