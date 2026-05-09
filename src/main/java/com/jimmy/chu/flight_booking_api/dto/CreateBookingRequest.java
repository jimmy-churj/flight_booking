package com.jimmy.chu.flight_booking_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateBookingRequest(
        @NotBlank String passengerName,
        @NotBlank @Email String passengerEmail
) {}
