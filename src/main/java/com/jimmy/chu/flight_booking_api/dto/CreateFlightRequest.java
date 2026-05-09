package com.jimmy.chu.flight_booking_api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFlightRequest(
        @NotBlank @Size(max = 10) String flightNumber,
        @Min(1) int totalSeats
) {}
