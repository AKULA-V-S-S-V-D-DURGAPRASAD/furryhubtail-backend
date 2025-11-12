package com.furryhub.petservices.model.dto;

import com.furryhub.petservices.model.entity.BookingStatus;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {
    private Long id;
    private Long customerId;
    private Long packageId;
    private Long providerId;
    private String providerPhoneNumber;
    private LocalDateTime bookingDate;
    private BookingStatus status;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    private Double latitude;  // nullable
    private Double longitude; // nullable
    private String requestType; // CONFIRMED, SPECIFIC, DISCOVERY
}
