package com.furryhub.petservices.model.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {
    private Long packageId;
    private Integer qty;
}