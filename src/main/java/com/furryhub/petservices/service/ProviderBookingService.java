package com.furryhub.petservices.service;

import com.furryhub.petservices.model.dto.BookingDTO;
import com.furryhub.petservices.model.entity.*;
import com.furryhub.petservices.repository.*;
import com.furryhub.petservices.exception.ResourceNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class ProviderBookingService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private SmSNotificationService smSNotificationService;

    @Autowired
    BookingRequestRepository bookingRequestRepository;

    @Autowired
    private BookingNotificationService bookingNotificationService;

    @Transactional(readOnly = true)
    public List<BookingDTO> getProviderBookings(String providerEmail, Double latitude, Double longitude) {
        Provider provider = providerRepository.findByUser_Email(providerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with email: " + providerEmail));

        // Get confirmed/accepted bookings for this provider
        List<BookingDTO> confirmedBookings = bookingRepository.findBookingsByProviderId(provider.getId());
        confirmedBookings.forEach(booking -> booking.setRequestType("CONFIRMED"));

        // Get pending booking requests specifically sent to this provider
        List<BookingRequest> pendingRequests = bookingRequestRepository.findPendingRequestsByProviderEmail(providerEmail, RequestStatus.PENDING);
        List<BookingDTO> pendingRequestBookings = pendingRequests.stream()
                .map(request -> {
                    BookingDTO dto = modelMapper.map(request.getBooking(), BookingDTO.class);
                    dto.setRequestType("SPECIFIC");
                    return dto;
                })
                .toList();

        // Get additional pending bookings within radius (for discovery)
        List<BookingDTO> nearbyPendingBookings = new ArrayList<>();

        // Use provided location, or fall back to provider's stored location
        Double searchLatitude = latitude;
        Double searchLongitude = longitude;

        if (searchLatitude == null || searchLongitude == null) {
            // Use provider's stored location if available
            if (provider.getUser().getLatitude() != null && provider.getUser().getLongitude() != null) {
                searchLatitude = provider.getUser().getLatitude();
                searchLongitude = provider.getUser().getLongitude();
            }
        }

        if (searchLatitude != null && searchLongitude != null) {
            nearbyPendingBookings = bookingRepository.findPendingBookingsWithinRadius(searchLatitude, searchLongitude);
            nearbyPendingBookings.forEach(booking -> booking.setRequestType("DISCOVERY"));
        } else {
            // If no location available at all, show ALL pending bookings (no limit to ensure visibility)
            nearbyPendingBookings = bookingRepository.findAllPendingBookings();
            nearbyPendingBookings.forEach(booking -> booking.setRequestType("DISCOVERY"));
            System.out.println("DEBUG: Provider location null, showing " + nearbyPendingBookings.size() + " pending bookings");
        }

        // Filter out bookings that already have specific requests sent to this provider
        int beforeFilter = nearbyPendingBookings.size();
        nearbyPendingBookings = nearbyPendingBookings.stream()
                .filter(booking -> !bookingRequestRepository.existsByBooking_IdAndProvider_Id(booking.getId(), provider.getId()))
                .toList();
        int afterFilter = nearbyPendingBookings.size();
        System.out.println("DEBUG: Filtered " + (beforeFilter - afterFilter) + " bookings that already have requests to this provider");

        // Combine and return: confirmed bookings + pending requests to this provider + nearby pending bookings
        List<BookingDTO> allBookings = new ArrayList<>();
        allBookings.addAll(confirmedBookings);
        allBookings.addAll(pendingRequestBookings);
        allBookings.addAll(nearbyPendingBookings);

        System.out.println("DEBUG: Provider " + providerEmail + " total bookings: " +
                "confirmed=" + confirmedBookings.size() + ", " +
                "specific=" + pendingRequestBookings.size() + ", " +
                "discovery=" + nearbyPendingBookings.size() + ", " +
                "total=" + allBookings.size());

        return allBookings;
    }

    @Transactional
    public BookingDTO confirmBooking(Long bookingId, String providerEmail) {
        try {
            Provider provider = providerRepository.findByUser_Email(providerEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Provider not found with email: " + providerEmail));

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

            // Enhanced status validation with specific error messages
            if (booking.getStatus() != BookingStatus.PENDING) {
                String errorMessage = switch (booking.getStatus()) {
                    case CONFIRMED -> "Booking already accepted by another provider";
                    case COMPLETED -> "Booking has already been completed";
                    case CANCELLED -> "Booking has been cancelled";
                    default -> "Booking is not available for acceptance. Status: " + booking.getStatus();
                };
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
            }

        // Check if provider is within 10km of customer location
        if (booking.getLatitude() != null && booking.getLongitude() != null &&
            provider.getUser().getLatitude() != null && provider.getUser().getLongitude() != null) {

            double distance = calculateDistance(
                provider.getUser().getLatitude(), provider.getUser().getLongitude(),
                booking.getLatitude(), booking.getLongitude()
            );

            if (distance > 10.0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provider is too far from customer location. Distance: " + distance + " km");
            }
        }

            // Generate OTP for booking completion
            String otp = String.format("%06d", new Random().nextInt(999999));
            booking.setOtp(otp);

            booking.setProvider(provider);
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setUpdatedAt(LocalDateTime.now());

            Booking savedBooking = bookingRepository.save(booking);

            // Send SMS notification
            try {
                smSNotificationService.sendBookingConfirmationSMS(savedBooking);
            } catch (Exception e) {
                // Log error but don't fail the booking
                System.err.println("Failed to send SMS notification: " + e.getMessage());
            }

            // Send WebSocket notification
            try {
                bookingNotificationService.notifyBookingAccepted(modelMapper.map(savedBooking, BookingDTO.class));
            } catch (Exception e) {
                // Log error but don't fail the booking
                System.err.println("Failed to send WebSocket notification: " + e.getMessage());
            }

            return modelMapper.map(savedBooking, BookingDTO.class);

        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Booking was modified by another provider. Please refresh and try again.");
        }
    }

    @Transactional
    public BookingDTO cancelBooking(Long bookingId, String providerEmail) {
        Provider provider = providerRepository.findByUser_Email(providerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with email: " + providerEmail));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (!booking.getProvider().getId().equals(provider.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Provider not authorized for this booking");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking is not in CONFIRMED status");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        // Send SMS notification
        try {
            smSNotificationService.sendBookingCancellationSMS(savedBooking);
        } catch (Exception e) {
            System.err.println("Failed to send SMS notification: " + e.getMessage());
        }

        // Send WebSocket notification
        try {
            bookingNotificationService.notifyBookingCancelled(modelMapper.map(savedBooking, BookingDTO.class));
        } catch (Exception e) {
            System.err.println("Failed to send WebSocket notification: " + e.getMessage());
        }

        return modelMapper.map(savedBooking, BookingDTO.class);
    }

    @Transactional
    public BookingDTO completeBooking(Long bookingId, String providerEmail, String otp) {
        Provider provider = providerRepository.findByUser_Email(providerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with email: " + providerEmail));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (!booking.getProvider().getId().equals(provider.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Provider not authorized for this booking");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking is not in CONFIRMED status");
        }

        if (!booking.getOtp().equals(otp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        booking.setCompletedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        // Send SMS notification
        try {
            smSNotificationService.sendBookingCompletionSMS(savedBooking);
        } catch (Exception e) {
            System.err.println("Failed to send SMS notification: " + e.getMessage());
        }

        return modelMapper.map(savedBooking, BookingDTO.class);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        return distance;
    }
}
