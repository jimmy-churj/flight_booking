package com.jimmy.chu.flight_booking_api.service;

import com.jimmy.chu.flight_booking_api.dto.CreateFlightRequest;
import com.jimmy.chu.flight_booking_api.dto.FlightResponse;
import com.jimmy.chu.flight_booking_api.exception.FlightAlreadyExistsException;
import com.jimmy.chu.flight_booking_api.exception.FlightNotFoundException;
import com.jimmy.chu.flight_booking_api.model.Flight;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class FlightService {

    private final ConcurrentHashMap<String, Flight> flights = new ConcurrentHashMap<>();

    public FlightResponse createFlight(CreateFlightRequest request) {
        Flight newFlight = new Flight(request.flightNumber(), request.totalSeats());
        Flight existing = flights.putIfAbsent(request.flightNumber(), newFlight);
        if (existing != null) {
            throw new FlightAlreadyExistsException(request.flightNumber());
        }
        return toResponse(newFlight);
    }

    public Flight getFlightOrThrow(String flightNumber) {
        Flight flight = flights.get(flightNumber);
        if (flight == null) throw new FlightNotFoundException(flightNumber);
        return flight;
    }

    private FlightResponse toResponse(Flight flight) {
        return new FlightResponse(flight.getFlightNumber(), flight.getTotalSeats(), flight.getAvailableSeats());
    }
}
