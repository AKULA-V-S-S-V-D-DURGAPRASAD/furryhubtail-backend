
package com.furryhub.petservices.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.locationtech.jts.geom.Point;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "providers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Provider {
    @Id
    private Long id; // Primary Key and Foreign Key to User.id

    private String specialization;

    private Integer experience; // in years

    private String licenseNumber;

    private String petStoreName;

    private Double rating=0.0;

    private String petClinicLocation;

    @NotBlank(message = "Phone number is required")
    @Column(nullable = false)
    private String phoneNumber;

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Manages the forward part of the relationship
    private List<Booking> bookings = new ArrayList<>();



    @OneToOne
    @MapsId
    @JoinColumn(name = "id") // MapsId uses the same column as @Id
    private User user;
    
    private String fieldType;
    
    private String address;
    
    private String city;
    
    private String houseVisit;
    
    private String businessContactNumber;
    
    private String onlineService;

    @Column(name = "location", columnDefinition = "geometry(Point,4326)")
    private Point location;


}