package com.jimmy.chu.flight_booking_api.service;

import com.jimmy.chu.flight_booking_api.dto.BookingResponse;
import com.jimmy.chu.flight_booking_api.dto.CreateBookingRequest;
import com.jimmy.chu.flight_booking_api.exception.NoSeatsAvailableException;
import com.jimmy.chu.flight_booking_api.model.Booking;
import com.jimmy.chu.flight_booking_api.model.Flight;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BookingService {

    private final FlightService flightService;
    private final ConcurrentHashMap<UUID, Booking> bookings = new ConcurrentHashMap<>();

    public BookingService(FlightService flightService) {
        this.flightService = flightService;
    }

    public BookingResponse createBooking(String flightNumber, CreateBookingRequest request) {
        Flight flight = flightService.getFlightOrThrow(flightNumber);

        if (!flight.tryClaim()) {
            throw new NoSeatsAvailableException(flightNumber);
        }

        Booking booking = new Booking(
                UUID.randomUUID(),
                flightNumber,
                request.passengerName(),
                request.passengerEmail(),
                Instant.now()
        );
        bookings.put(booking.bookingId(), booking);
        return toResponse(booking);
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.bookingId(),
                booking.flightNumber(),
                booking.passengerName(),
                booking.passengerEmail(),
                booking.bookedAt()
        );
    }
}
