package com.jimmy.chu.flight_booking_api.service;

import com.jimmy.chu.flight_booking_api.dto.BookingResponse;
import com.jimmy.chu.flight_booking_api.dto.CreateBookingRequest;
import com.jimmy.chu.flight_booking_api.exception.FlightNotFoundException;
import com.jimmy.chu.flight_booking_api.exception.NoSeatsAvailableException;
import com.jimmy.chu.flight_booking_api.model.Flight;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private FlightService flightService;

    @InjectMocks
    private BookingService bookingService;

    private static final CreateBookingRequest VALID_REQUEST =
            new CreateBookingRequest("Jimmy Chu", "jimmy@example.com");

    @Test
    void createBooking_success_returnsPopulatedBookingResponse() {
        when(flightService.getFlightOrThrow("AA123")).thenReturn(new Flight("AA123", 10));

        BookingResponse response = bookingService.createBooking("AA123", VALID_REQUEST);

        assertThat(response.bookingId()).isNotNull();
        assertThat(response.flightNumber()).isEqualTo("AA123");
        assertThat(response.passengerName()).isEqualTo("Jimmy Chu");
        assertThat(response.passengerEmail()).isEqualTo("jimmy@example.com");
        assertThat(response.bookedAt()).isNotNull();
    }

    @Test
    void createBooking_success_decrementsFlightAvailableSeats() {
        Flight flight = new Flight("AA123", 10);
        when(flightService.getFlightOrThrow("AA123")).thenReturn(flight);

        bookingService.createBooking("AA123", VALID_REQUEST);

        assertThat(flight.getAvailableSeats()).isEqualTo(9);
    }

    @Test
    void createBooking_successfulBookings_eachHaveUniqueId() {
        Flight flight = new Flight("AA123", 10);
        when(flightService.getFlightOrThrow("AA123")).thenReturn(flight);

        BookingResponse b1 = bookingService.createBooking("AA123", new CreateBookingRequest("Alice", "alice@example.com"));
        BookingResponse b2 = bookingService.createBooking("AA123", new CreateBookingRequest("Bob", "bob@example.com"));

        assertThat(b1.bookingId()).isNotEqualTo(b2.bookingId());
    }

    @Test
    void createBooking_flightNotFound_propagatesFlightNotFoundException() {
        when(flightService.getFlightOrThrow("NOPE")).thenThrow(new FlightNotFoundException("NOPE"));

        assertThatThrownBy(() -> bookingService.createBooking("NOPE", VALID_REQUEST))
                .isInstanceOf(FlightNotFoundException.class);
    }

    @Test
    void createBooking_flightWithZeroSeats_throwsNoSeatsAvailableException() {
        when(flightService.getFlightOrThrow("FULL")).thenReturn(new Flight("FULL", 0));

        assertThatThrownBy(() -> bookingService.createBooking("FULL", VALID_REQUEST))
                .isInstanceOf(NoSeatsAvailableException.class);
    }

    @Test
    void createBooking_lastAvailableSeat_succeeds() {
        Flight flight = new Flight("LAST", 1);
        when(flightService.getFlightOrThrow("LAST")).thenReturn(flight);

        assertThatCode(() -> bookingService.createBooking("LAST", VALID_REQUEST))
                .doesNotThrowAnyException();
        assertThat(flight.getAvailableSeats()).isZero();
    }

    @Test
    void createBooking_afterLastSeatTaken_throwsNoSeatsAvailableException() {
        Flight flight = new Flight("LAST2", 1);
        when(flightService.getFlightOrThrow("LAST2")).thenReturn(flight);

        bookingService.createBooking("LAST2", new CreateBookingRequest("Alice", "alice@example.com"));

        assertThatThrownBy(() -> bookingService.createBooking("LAST2", new CreateBookingRequest("Bob", "bob@example.com")))
                .isInstanceOf(NoSeatsAvailableException.class);
    }

    @Test
    void createBooking_concurrent_preventsOverbooking() throws InterruptedException {
        int totalSeats = 5;
        int totalAttempts = 20;
        Flight flight = new Flight("CONC", totalSeats);
        when(flightService.getFlightOrThrow("CONC")).thenReturn(flight);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger rejectedCount = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(totalAttempts);
        ExecutorService executor = Executors.newFixedThreadPool(totalAttempts);

        for (int i = 0; i < totalAttempts; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    bookingService.createBooking("CONC", new CreateBookingRequest("P" + idx, "p" + idx + "@example.com"));
                    successCount.incrementAndGet();
                } catch (NoSeatsAvailableException e) {
                    rejectedCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(totalSeats);
        assertThat(rejectedCount.get()).isEqualTo(totalAttempts - totalSeats);
    }
}
