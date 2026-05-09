package com.jimmy.chu.flight_booking_api.exception;

public class FlightNotFoundException extends RuntimeException {
    public FlightNotFoundException(String flightNumber) {
        super("Flight not found: " + flightNumber);
    }
}
