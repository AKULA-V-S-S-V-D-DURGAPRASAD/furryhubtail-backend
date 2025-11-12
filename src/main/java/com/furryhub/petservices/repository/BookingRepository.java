package com.furryhub.petservices.repository;

import com.furryhub.petservices.model.dto.BookingDTO;
import com.furryhub.petservices.model.entity.Booking;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
	List<Booking> findByCustomer_Id(Long customerId);

	Optional<Booking> findById(Long bookingId);
	@Query("SELECT b FROM Booking b JOIN FETCH b.customer WHERE b.id = :bookingId")
	Optional<Booking> findBookingWithCustomer(@Param("bookingId") Long bookingId);

	@Query("SELECT new com.furryhub.petservices.model.dto.BookingDTO(" +
		       "b.id, " +
		       "b.customer.id, " +
		       "b.aPackage.id, " +
		       "b.provider.id, " +
		       "b.provider.phoneNumber, " +
		       "b.bookingDate, " +
		       "b.status, " +
		       "b.totalPrice, " +
		       "b.createdAt, " +
		       "b.updatedAt, " +
		       "b.completedAt, " +
		       "b.latitude, " +
		       "b.longitude, " +
		       "'CUSTOMER') " +
		       "FROM Booking b " +
		       "WHERE b.customer.user.email = :email")
		List<BookingDTO> findBookingsByCustomerEmail(@Param("email") String email);

	@Query("SELECT new com.furryhub.petservices.model.dto.BookingDTO(" +
		       "b.id, " +
		       "b.customer.id, " +
		       "b.aPackage.id, " +
		       "b.provider.id, " +
		       "b.provider.phoneNumber, " +
		       "b.bookingDate, " +
		       "b.status, " +
		       "b.totalPrice, " +
		       "b.createdAt, " +
		       "b.updatedAt, " +
		       "b.completedAt, " +
		       "b.latitude, " +
		       "b.longitude, " +
		       "'CONFIRMED') " +
		       "FROM Booking b " +
		       "WHERE b.provider.id = :providerId")
		List<BookingDTO> findBookingsByProviderId(@Param("providerId") Long providerId);

	@Query("SELECT new com.furryhub.petservices.model.dto.BookingDTO(" +
		       "b.id, " +
		       "b.customer.id, " +
		       "b.aPackage.id, " +
		       "b.provider.id, " +
		       "b.provider.phoneNumber, " +
		       "b.bookingDate, " +
		       "b.status, " +
		       "b.totalPrice, " +
		       "b.createdAt, " +
		       "b.updatedAt, " +
		       "b.completedAt, " +
		       "b.latitude, " +
		       "b.longitude, " +
		       "'DISCOVERY') " +
		       "FROM Booking b " +
		       "WHERE b.status = 'PENDING' AND " +
		       "b.latitude IS NOT NULL AND b.longitude IS NOT NULL AND " +
		       "(6371 * acos(cos(radians(:latitude)) * cos(radians(b.latitude)) * cos(radians(b.longitude) - radians(:longitude)) + sin(radians(:latitude)) * sin(radians(b.latitude)))) <= 50")
		List<BookingDTO> findPendingBookingsWithinRadius(@Param("latitude") Double latitude, @Param("longitude") Double longitude);

	@Query("SELECT new com.furryhub.petservices.model.dto.BookingDTO(" +
		       "b.id, " +
		       "b.customer.id, " +
		       "b.aPackage.id, " +
		       "b.provider.id, " +
		       "b.provider.phoneNumber, " +
		       "b.bookingDate, " +
		       "b.status, " +
		       "b.totalPrice, " +
		       "b.createdAt, " +
		       "b.updatedAt, " +
		       "b.completedAt, " +
		       "b.latitude, " +
		       "b.longitude, " +
		       "'DISCOVERY') " +
		       "FROM Booking b " +
		       "WHERE b.status = 'PENDING'")
		List<BookingDTO> findAllPendingBookings();

   // Optional<BookingDTO> findBookingByCustomerEmail(String email);

}
