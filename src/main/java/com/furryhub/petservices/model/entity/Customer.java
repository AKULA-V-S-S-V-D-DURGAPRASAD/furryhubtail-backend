
package com.furryhub.petservices.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Customer {

    @Id
    private Long id; // Primary Key and Foreign Key to User.id

    private String address;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Manages the forward part of the relationship
    private List<Booking> bookings = new ArrayList<>();
    
    
   /* @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference
    private List<FurryDetail> furryDetail = new ArrayList<>();*/

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pet> pets;

    @NotBlank(message = "Phone number is required")
    @Column(nullable = false)
    private String phoneNumber;
     
    @OneToOne
    @MapsId
    @JoinColumn(name = "id") // MapsId uses the same column as @Id
    private User user;

    @Column(name = "location", columnDefinition = "geometry(Point,4326)")
    private Point location;

}