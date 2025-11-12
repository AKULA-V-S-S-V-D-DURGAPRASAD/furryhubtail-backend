package com.furryhub.petservices.service;

import com.furryhub.petservices.exception.ResourceNotFoundException;
import com.furryhub.petservices.model.dto.PetDTO;
import com.furryhub.petservices.model.entity.Customer;
import com.furryhub.petservices.model.entity.Pet;
import com.furryhub.petservices.repository.CustomerRepository;
import com.furryhub.petservices.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final ModelMapper modelMapper;
    private final CustomerRepository customerRepository;

    public List<PetDTO> getPetsByCustomer(Long customerId) {
        List<Pet> pets = petRepository.findByCustomerId(customerId);
        return pets.stream()
                .map(pet -> modelMapper.map(pet, PetDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public PetDTO createPet(String customerEmail, PetDTO petDTO){
        Pet save = modelMapper.map(petDTO, Pet.class);
        Customer customer = customerRepository.findByUser_Email(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerEmail));
        save.setCustomer(customer);
        save.setId(null); // Ensure ID is null for new entities
        petRepository.save(save);
        return modelMapper.map(save, PetDTO.class);
    }

    @Transactional
    public void deletePet(Principal principle ,Long petId) {
        Customer customer=customerRepository.findByUser_Email(principle.getName())
                .orElseThrow(()-> new ResourceNotFoundException("Customer not Found with Email"+ principle.getName()));

        Pet pet=petRepository.findById(petId)
                .orElseThrow(()->new ResourceNotFoundException("Pet not found with Id" + petId));

        if(!pet.getCustomer().getId().equals(customer.getId())){
           throw new SecurityException("you are not allowed to delete this pet");
        }

        petRepository.delete(pet);

    }

    @Transactional
    public PetDTO updatePet(String customerEmail, Long petId, PetDTO petDTO) {
        Customer customer = customerRepository.findByUser_Email(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerEmail));

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found with Id: " + petId));

        if (!pet.getCustomer().getId().equals(customer.getId())) {
            throw new SecurityException("You are not allowed to update this pet");
        }

        // Update pet fields
        pet.setName(petDTO.getName());
        pet.setAge(petDTO.getAge());
        pet.setBreed(petDTO.getBreed());
        pet.setGender(petDTO.getGender());
        pet.setWeight(petDTO.getWeight());
        pet.setColor(petDTO.getColor());

        petRepository.save(pet);
        return modelMapper.map(pet, PetDTO.class);
    }


}
