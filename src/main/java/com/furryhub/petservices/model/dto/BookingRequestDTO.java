package com.furryhub.petservices.model.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequestDTO {
    private Long id;

    private Long providerId;
    private String providerName;
    private Double providerLatitude;
    private Double providerLongitude;

    private Long bookingId;
    private LocalDateTime requestedAt;
    private String status;     // PENDING / ACCEPTED / REJECTED / EXPIRED

}