package com.furryhub.petservices.service;

import com.furryhub.petservices.model.dto.BookingDTO;
import com.furryhub.petservices.model.entity.Booking;
import com.furryhub.petservices.model.entity.BookingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class BookingNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Notify all providers about a new booking request
     */
    public void notifyNewBooking(BookingDTO booking) {
        messagingTemplate.convertAndSend("/topic/bookings/new", booking);
    }

    /**
     * Notify specific provider about booking updates
     */
    public void notifyProviderBookingUpdate(Long providerId, BookingDTO booking) {
        messagingTemplate.convertAndSendToUser(
            providerId.toString(),
            "/queue/bookings",
            booking
        );
    }

    /**
     * Notify customer about booking status changes
     */
    public void notifyCustomerBookingUpdate(Long customerId, BookingDTO booking) {
        messagingTemplate.convertAndSendToUser(
            customerId.toString(),
            "/queue/bookings",
            booking
        );
    }

    /**
     * Notify providers in a specific area about new bookings
     */
    public void notifyNearbyProviders(BookingDTO booking, Double latitude, Double longitude) {
        // Create a location-based notification
        LocationBasedBookingNotification notification = new LocationBasedBookingNotification(
            booking, latitude, longitude
        );
        messagingTemplate.convertAndSend("/topic/bookings/nearby", notification);
    }

    /**
     * Notify when a booking is accepted
     */
    public void notifyBookingAccepted(BookingDTO booking) {
        // Notify the customer
        notifyCustomerBookingUpdate(booking.getCustomerId(), booking);

        // Notify other providers that this booking is no longer available
        messagingTemplate.convertAndSend("/topic/bookings/confirmed/" + booking.getId(), booking);
    }

    /**
     * Notify when a booking is completed
     */
    public void notifyBookingCompleted(BookingDTO booking) {
        // Notify the customer
        notifyCustomerBookingUpdate(booking.getCustomerId(), booking);

        // Notify the provider
        if (booking.getProviderId() != null) {
            notifyProviderBookingUpdate(booking.getProviderId(), booking);
        }
    }

    /**
     * Notify when a booking is cancelled
     */
    public void notifyBookingCancelled(BookingDTO booking) {
        // Notify the customer
        notifyCustomerBookingUpdate(booking.getCustomerId(), booking);

        // Notify the provider if assigned
        if (booking.getProviderId() != null) {
            notifyProviderBookingUpdate(booking.getProviderId(), booking);
        }

        // Notify all providers that this booking might be available again
        messagingTemplate.convertAndSend("/topic/bookings/cancelled/" + booking.getId(), booking);
    }

    /**
     * Inner class for location-based notifications
     */
    public static class LocationBasedBookingNotification {
        private BookingDTO booking;
        private Double latitude;
        private Double longitude;

        public LocationBasedBookingNotification(BookingDTO booking, Double latitude, Double longitude) {
            this.booking = booking;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        // Getters
        public BookingDTO getBooking() { return booking; }
        public Double getLatitude() { return latitude; }
        public Double getLongitude() { return longitude; }
    }
}
