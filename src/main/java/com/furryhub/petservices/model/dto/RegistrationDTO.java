
package com.furryhub.petservices.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationDTO {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    // Customer-specific fields
    private String address;
    private String phoneNumber;

    private Double latitude;
    private Double longitude;
    // Provider-specific fields
    private String specialization;
    private Integer experience;
    private String licenseNumber;
    private String petStoreName;
    private String petClinicLocation;
	private Double rating;
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getSpecialization() {
		return specialization;
	}
	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}
	public Integer getExperience() {
		return experience;
	}
	public void setExperience(Integer experience) {
		this.experience = experience;
	}
	public String getLicenseNumber() {
		return licenseNumber;
	}
	public void setLicenseNumber(String licenseNumber) {
		this.licenseNumber = licenseNumber;
	}
	public String getPetStoreName() {
		return petStoreName;
	}
	public void setPetStoreName(String petStoreName) {
		this.petStoreName = petStoreName;
	}
	public String getPetClinicLocation() {
		return petClinicLocation;
	}
	public void setPetClinicLocation(String petClinicLocation) {
		this.petClinicLocation = petClinicLocation;
	}
    
    // Getters and Setters
    // ...
}