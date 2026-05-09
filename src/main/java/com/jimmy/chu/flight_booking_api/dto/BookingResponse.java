package com.jimmy.chu.flight_booking_api.dto;

import java.time.Instant;
import java.util.UUID;

public record BookingResponse(
        UUID bookingId,
        String flightNumber,
        String passengerName,
        String passengerEmail,
        Instant bookedAt
) {}
