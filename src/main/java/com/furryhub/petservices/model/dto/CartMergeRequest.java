package com.furryhub.petservices.model.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartMergeRequest {
    private List<CartItemDto> items;
}