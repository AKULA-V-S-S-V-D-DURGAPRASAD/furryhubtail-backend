package com.furryhub.petservices.model.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDto {
    private Long userId;
    private List<CartItemDto> items;
    private BigDecimal total;
}