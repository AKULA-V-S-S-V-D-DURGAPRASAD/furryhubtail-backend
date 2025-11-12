package com.furryhub.petservices.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderProfileDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String specialization;
    private Integer experience;
    private String licenseNumber;
    private String petStoreName;
    private Double rating;
    private String petClinicLocation;
    private String fieldType;
    private String address;
    private String city;
    private String houseVisit;
    private String businessContactNumber;
    private String onlineService;
}
