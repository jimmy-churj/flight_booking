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
    public int getAvailableSeats() { return availableSeats.get(); }

    /**
     * Atomically claims one seat via a compare-and-swap loop.
     * The counter never goes below zero — no decrement-then-restore needed.
     * Returns false immediately when no seats remain.
     */
    public boolean tryClaim() {
        int current;
        do {
            current = availableSeats.get();
            if (current <= 0) return false;
        } while (!availableSeats.compareAndSet(current, current - 1));
        return true;
    }
}
