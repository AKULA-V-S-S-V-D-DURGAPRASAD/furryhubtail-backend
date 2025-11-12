package com.furryhub.petservices.repository;

import com.furryhub.petservices.model.dto.PetDTO;
import com.furryhub.petservices.model.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pet,Long> {

    Optional<Object> findAllById(Long id);

    void deleteById(PetDTO petDTO);

    List<Pet> findByCustomerId(Long customerId);
}