package com.jimmy.chu.flight_booking_api.exception;

public class FlightAlreadyExistsException extends RuntimeException {
    public FlightAlreadyExistsException(String flightNumber) {
        super("Flight already registered: " + flightNumber);
    }
}
