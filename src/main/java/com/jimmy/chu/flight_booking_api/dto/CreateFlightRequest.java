package com.jimmy.chu.flight_booking_api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateFlightRequest(
        @NotBlank @Pattern(regexp = "^[A-Z0-9]{1,10}$", message = "must be 1 to 10 uppercase letters or digits") String flightNumber,
        @Min(1) int totalSeats
) {}
