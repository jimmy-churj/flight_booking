package com.jimmy.chu.flight_booking_api.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Flight {

    private final String flightNumber;
    private final int totalSeats;
    private final AtomicInteger availableSeats;

    public Flight(String flightNumber, int totalSeats) {
        this.flightNumber = flightNumber;
        this.totalSeats = totalSeats;
        this.availableSeats = new AtomicInteger(totalSeats);
    }

    public String getFlightNumber() { return flightNumber; }
    public int getTotalSeats() { return totalSeats; }
    public AtomicInteger getAvailableSeats() { return availableSeats; }
}
