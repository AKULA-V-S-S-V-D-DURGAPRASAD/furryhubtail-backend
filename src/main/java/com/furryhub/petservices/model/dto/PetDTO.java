package com.furryhub.petservices.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PetDTO {
    private Long id;
    private String name;
    private Integer age;
    private String breed;
    private String gender;
    private Double weight;
    private String color;
}
