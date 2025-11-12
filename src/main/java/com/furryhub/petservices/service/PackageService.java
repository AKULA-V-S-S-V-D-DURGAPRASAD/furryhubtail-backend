
package com.furryhub.petservices.service;


import com.furryhub.petservices.model.dto.PackageRequestDTO;

import com.furryhub.petservices.model.entity.Package;
import com.furryhub.petservices.model.entity.Provider;
import com.furryhub.petservices.repository.PackageRepository;
import com.furryhub.petservices.repository.ProviderRepository;
import com.furryhub.petservices.exception.ResourceNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PackageService {

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private ModelMapper modelMapper;

    

     
    public PackageRequestDTO convertToDTO(Package booking) {
        return PackageRequestDTO.builder()
                .id(booking.getId())
                .price(booking.getPrice())
                .description(booking.getDescription())
                .duration(booking.getDuration())
                .name(booking.getName())
                .type(booking.getType())
                .build();
    }

    @Transactional(readOnly = true)
    public List<PackageRequestDTO> getAllPackages() {
        List<Package> packages = packageRepository.findAll();
        return packages.stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Package> findById(Long id) {
        return packageRepository.findById(id);
    }

    public BigDecimal getCurrentPrice(Package servicePackage) {
        return servicePackage.getPrice();
    }


    public boolean isAvailable(Package servicePackage, int qty) {
        Boolean avail = servicePackage.getAvailable();
        if (avail == null) {
            return true;
        } else {
            return avail;
        }
    }

    @Transactional
    public PackageRequestDTO addPackage(String name, String description, BigDecimal price, Duration duration) {
        Package newPackage = Package.builder()
                .name(name)
                .description(description)
                .price(price)
                .duration(duration)
                .available(true)
                .build();

        Package savedPackage = packageRepository.save(newPackage);
        return convertToDTO(savedPackage);
    }

    @Transactional
    public PackageRequestDTO addPackageWithType(String name, String description, BigDecimal price, Duration duration, String type) {
        Package newPackage = Package.builder()
                .name(name)
                .description(description)
                .price(price)
                .duration(duration)
                .type(type)
                .available(true)
                .build();

        Package savedPackage = packageRepository.save(newPackage);
        return convertToDTO(savedPackage);
    }

    @Transactional
    public PackageRequestDTO updatePackage(Long id, PackageRequestDTO dto) {
        Package existingPackage = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found with id: " + id));

        existingPackage.setName(dto.getName());
        existingPackage.setDescription(dto.getDescription());
        existingPackage.setPrice(dto.getPrice());
        existingPackage.setDuration(dto.getDuration());
        existingPackage.setType(dto.getType());

        Package updatedPackage = packageRepository.save(existingPackage);
        return convertToDTO(updatedPackage);
    }

    @Transactional
    public void deletePackage(Long id) {
        if (!packageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Package not found with id: " + id);
        }
        packageRepository.deleteById(id);
    }
}
