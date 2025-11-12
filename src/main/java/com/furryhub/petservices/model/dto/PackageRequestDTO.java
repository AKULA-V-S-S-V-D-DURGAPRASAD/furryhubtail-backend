package com.furryhub.petservices.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Duration;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PackageRequestDTO {
	 private Long id;

	
/*@NotNull(message = "Provider email is required")
 private String providerEmail;*/

 @NotNull(message = "Package name is required")
 private String name;

 @NotNull(message = "Price is required")
 private BigDecimal price;

 @NotNull(message = "Duration is required")
 private Duration duration;

 @NotNull(message = "Type is required")
 private String type;

 private String description;

}