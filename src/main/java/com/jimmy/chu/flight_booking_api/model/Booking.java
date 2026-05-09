package com.jimmy.chu.flight_booking_api.model;

import java.time.Instant;
import java.util.UUID;

public record Booking(
        UUID bookingId,
        String flightNumber,
        String passengerName,
        String passengerEmail,
        Instant bookedAt
) {}
