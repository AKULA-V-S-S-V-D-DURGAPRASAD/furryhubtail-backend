package com.furryhub.petservices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.furryhub.petservices.model.entity.Customer;
import com.furryhub.petservices.model.entity.Booking;


@Service
public class SmSNotificationService {
	
	@Autowired
	TwilioService twilioService;

    public void cancelBookingByProviderNotification(Customer customer, Long bookingId, String providerPhoneNumber){


        String customerPhoneNumber= customer.getPhoneNumber();
        System.out.println(customerPhoneNumber);
        String customerName=customer.getUser().getFirstName()+" "+customer.getUser().getLastName();
        String message = String.format(
                "\nDear %s,\nYour booking with ID %d has been canceled by your FurryHub provider.\nWe apologize for the inconvenience.\nThank you for choosing FurryHub!",
                customerName, bookingId, providerPhoneNumber
        );

        twilioService.sendSms(customerPhoneNumber,message);
    }

    public void cancelBookingByCustomerNotification(Customer customer, Long bookingId){


        String customerPhoneNumber= customer.getPhoneNumber();
        System.out.println(customerPhoneNumber);
        String customerName=customer.getUser().getFirstName()+" "+customer.getUser().getLastName();
        String message = String.format(
                "\nDear %s,\nYour booking with ID %d has been canceled .\nThank you for choosing FurryHub!",
                customerName, bookingId
        );

        twilioService.sendSms(customerPhoneNumber,message);
    }

    public void BookingCompleteNotification(Customer customer,Long bookingId, String providerPhoneNumber){


        String customerPhoneNumber= customer.getPhoneNumber();
        System.out.println(customerPhoneNumber);
        String customerName=customer.getUser().getFirstName()+" "+customer.getUser().getLastName();
        String message = String.format(
                "\nDear %s,\nYour booking with ID %d has been completed by your FurryHub provider (%s).\nThank you for choosing FurryHub!",
                customerName, bookingId, providerPhoneNumber
        );

        twilioService.sendSms(customerPhoneNumber,message);
    }

    public void bookingConfirmationNotification(Customer customer, Long bookingId, String providerPhoneNumber, String otp){


        String customerPhoneNumber= customer.getPhoneNumber();
        System.out.println(customerPhoneNumber);
        String customerName=customer.getUser().getFirstName()+" "+customer.getUser().getLastName();
        String message = String.format(
                "Dear %s,\nYour FurryHub booking (ID: %d) has been CONFIRMED by your provider (%s).\n" +
                        "Your OTP for booking completion is: %s\n" +
                        "Please share this OTP only with your provider when the service is finished.\n\n" +
                        "Thank you for choosing FurryHub!",
                customerName, bookingId, providerPhoneNumber, otp
        );

        twilioService.sendSms(customerPhoneNumber,message);
    }

    public void sendBookingConfirmationSMS(Booking booking) {
        bookingConfirmationNotification(booking.getCustomer(), booking.getId(), booking.getProvider().getPhoneNumber(), booking.getOtp());
    }

    public void sendBookingCancellationSMS(Booking booking) {
        cancelBookingByProviderNotification(booking.getCustomer(), booking.getId(), booking.getProvider().getPhoneNumber());
    }

    public void sendBookingCompletionSMS(Booking booking) {
        BookingCompleteNotification(booking.getCustomer(), booking.getId(), booking.getProvider().getPhoneNumber());
    }

}
