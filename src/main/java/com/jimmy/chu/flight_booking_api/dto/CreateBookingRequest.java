package com.jimmy.chu.flight_booking_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBookingRequest(
        @NotBlank @Size(max = 100) String passengerName,
        @NotBlank @Email @Size(max = 255) String passengerEmail
) {}
