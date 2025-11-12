package com.furryhub.petservices.service;

import com.furryhub.petservices.exception.ResourceNotFoundException;
import com.furryhub.petservices.model.dto.BookingDTO;
import com.furryhub.petservices.model.dto.BookingRequestDTO;
import com.furryhub.petservices.model.entity.*;
import com.furryhub.petservices.model.entity.Package;
import com.furryhub.petservices.repository.*;
import org.locationtech.jts.geom.Point;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hibernate.spatial.SpatialFunction.distance;

@Service
public class CustomerBookingService {

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
    private BookingRequestRepository bookingRequestRepository;

    @Transactional
    public BookingDTO createBooking(String customerEmail, Long packageId) {
        Booking booking = new Booking();

        Customer customer = customerRepository.findByUser_Email(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Package aPackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found"));

        double lat = 17.3850; // Default Hyderabad latitude
        double lon = 78.4867; // Default Hyderabad longitude

        // Use customer's location if available, otherwise use default
        if (customer.getLocation() != null) {
            lat = customer.getLocation().getY(); // latitude
            lon = customer.getLocation().getX(); // longitude
        }

        booking.setCustomer(customer);
        booking.setAPackage(aPackage);
        booking.setProvider(null); // provider assigned after one accepts
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(aPackage.getPrice());
        booking.setLatitude(lat);
        booking.setLongitude(lon);

        booking = bookingRepository.save(booking);

        final double SEARCH_METERS = 5000.0; // 5 km
        final int TOP_N = 5;

        List<Object[]> rows = providerRepository.findNearbyProvidersWithDistance(lat, lon, SEARCH_METERS, TOP_N);

        List<BookingRequest> requests = new ArrayList<>();


        LocalDateTime now = LocalDateTime.now();
        for (Object[] row : rows) {
            Number providerIdNum = (Number) row[0];
            long providerId = providerIdNum.longValue();
            double distance = ((Number) row[1]).doubleValue();

            Provider provider = providerRepository.findById(providerId).orElse(null);
            if (provider == null) continue;

            BookingRequest req = BookingRequest.builder()
                    .booking(booking)
                    .provider(provider)
                    .requestedAt(now)
                    .status(RequestStatus.PENDING)
                    .build();

            requests.add(req);
        }

        if (!requests.isEmpty()) {
            bookingRequestRepository.saveAll(requests);
        }


        BookingDTO bookingDTO = modelMapper.map(booking, BookingDTO.class);
        return bookingDTO;

    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsByCustomerEmail(String email) {
        List<BookingDTO> bookings = bookingRepository.findBookingsByCustomerEmail(email);
        return bookings; // Return empty list instead of throwing exception
    }

    @Transactional
    public BookingDTO cancelBooking(Long bookingId,String customerEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        // verify customer owns this booking
        if (!booking.getCustomer().getUser().getEmail().equals(customerEmail)) {
            throw new IllegalStateException("You are not authorized to cancel this booking.");
        }

        // disallow if completed or already cancelled
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("This booking is already cancelled.");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("You cannot cancel a completed booking.");
        }

        // allow only within 10 minutes for PENDING or CONFIRMED
        Duration diff = Duration.between(booking.getBookingDate(), LocalDateTime.now());
        if (diff.toMinutes() > 10) {
            throw new IllegalStateException("You can only cancel within 10 minutes of booking creation.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());
        smSNotificationService.cancelBookingByCustomerNotification(booking.getCustomer(),bookingId);
        Booking updatedBooking = bookingRepository.save(booking);

        return modelMapper.map(updatedBooking, BookingDTO.class);

    }
}