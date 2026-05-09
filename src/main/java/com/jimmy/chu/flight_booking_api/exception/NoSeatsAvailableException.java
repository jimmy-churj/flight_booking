package com.jimmy.chu.flight_booking_api.exception;

public class NoSeatsAvailableException extends RuntimeException {
    public NoSeatsAvailableException(String flightNumber) {
        super("No seats available on flight: " + flightNumber);
    }
}
