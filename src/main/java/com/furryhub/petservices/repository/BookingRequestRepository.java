package com.furryhub.petservices.repository;

import com.furryhub.petservices.model.entity.BookingRequest;
import com.furryhub.petservices.model.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {
    boolean existsByBooking_IdAndProvider_Id(Long bookingId, Long providerId);
    List<BookingRequest> findByBookingId(Long bookingId);
    List<BookingRequest> findByBookingIdAndStatus(Long bookingId, RequestStatus status);
    Optional<BookingRequest> findByBooking_IdAndProvider_Id(Long bookingId, Long providerId);

    @Query("SELECT br FROM BookingRequest br JOIN FETCH br.booking WHERE br.provider.user.email = :providerEmail AND br.status = :status")
    List<BookingRequest> findPendingRequestsByProviderEmail(@Param("providerEmail") String providerEmail, @Param("status") RequestStatus status);
}
