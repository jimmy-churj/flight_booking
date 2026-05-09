package com.jimmy.chu.flight_booking_api.controller;

import com.jimmy.chu.flight_booking_api.dto.CreateFlightRequest;
import com.jimmy.chu.flight_booking_api.dto.FlightResponse;
import com.jimmy.chu.flight_booking_api.service.FlightService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flights")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FlightResponse createFlight(@Valid @RequestBody CreateFlightRequest request) {
        return flightService.createFlight(request);
    }
}
