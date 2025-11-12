package com.furryhub.petservices.controller;

import com.furryhub.petservices.model.dto.PackageRequestDTO;
import com.furryhub.petservices.service.PackageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
public class PackageController {

    @Autowired
    private PackageService packageService;

    @GetMapping("/all")
    public ResponseEntity<List<PackageRequestDTO>> getAllPackages() {
        List<PackageRequestDTO> packages = packageService.getAllPackages();
        return ResponseEntity.ok(packages);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PackageRequestDTO> getPackageById(@PathVariable Long id) {
        PackageRequestDTO packageDTO = packageService.findById(id)
                .map(packageService::convertToDTO)
                .orElseThrow(() -> new com.furryhub.petservices.exception.ResourceNotFoundException("Package not found with id: " + id));
        return ResponseEntity.ok(packageDTO);
    }

    @PostMapping
    public ResponseEntity<PackageRequestDTO> createPackage(@Valid @RequestBody PackageRequestDTO packageRequestDTO) {
        PackageRequestDTO createdPackage = packageService.addPackageWithType(
                packageRequestDTO.getName(),
                packageRequestDTO.getDescription(),
                packageRequestDTO.getPrice(),
                packageRequestDTO.getDuration(),
                packageRequestDTO.getType()
        );
        return ResponseEntity.status(201).body(createdPackage);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PackageRequestDTO> updatePackage(@PathVariable Long id, @Valid @RequestBody PackageRequestDTO packageRequestDTO) {
        PackageRequestDTO updatedPackage = packageService.updatePackage(id, packageRequestDTO);
        return ResponseEntity.ok(updatedPackage);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable Long id) {
        packageService.deletePackage(id);
        return ResponseEntity.noContent().build();
    }

}
