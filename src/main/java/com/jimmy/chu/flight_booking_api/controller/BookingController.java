package com.jimmy.chu.flight_booking_api.controller;

import com.jimmy.chu.flight_booking_api.dto.BookingResponse;
import com.jimmy.chu.flight_booking_api.dto.CreateBookingRequest;
import com.jimmy.chu.flight_booking_api.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flights")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/{flightNumber}/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(
            @PathVariable String flightNumber,
            @Valid @RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(flightNumber, request);
    }
}
