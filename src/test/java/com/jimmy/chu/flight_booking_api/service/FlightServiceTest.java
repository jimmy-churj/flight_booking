package com.jimmy.chu.flight_booking_api.service;

import com.jimmy.chu.flight_booking_api.dto.CreateFlightRequest;
import com.jimmy.chu.flight_booking_api.dto.FlightResponse;
import com.jimmy.chu.flight_booking_api.exception.FlightAlreadyExistsException;
import com.jimmy.chu.flight_booking_api.exception.FlightNotFoundException;
import com.jimmy.chu.flight_booking_api.model.Flight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class FlightServiceTest {

    private FlightService flightService;

    @BeforeEach
    void setUp() {
        flightService = new FlightService();
    }

    @Test
    void createFlight_success_returnsCorrectFlightResponse() {
        FlightResponse response = flightService.createFlight(new CreateFlightRequest("AA123", 180));

        assertThat(response.flightNumber()).isEqualTo("AA123");
        assertThat(response.totalSeats()).isEqualTo(180);
        assertThat(response.availableSeats()).isEqualTo(180);
    }

    @Test
    void createFlight_availableSeatsInitiallyEqualssTotalSeats() {
        FlightResponse response = flightService.createFlight(new CreateFlightRequest("BB456", 50));

        assertThat(response.availableSeats()).isEqualTo(response.totalSeats());
    }

    @Test
    void createFlight_duplicateFlightNumber_throwsFlightAlreadyExistsException() {
        flightService.createFlight(new CreateFlightRequest("CC789", 100));

        assertThatThrownBy(() -> flightService.createFlight(new CreateFlightRequest("CC789", 200)))
                .isInstanceOf(FlightAlreadyExistsException.class);
    }

    @Test
    void createFlight_exceptionMessage_containsFlightNumber() {
        flightService.createFlight(new CreateFlightRequest("DD000", 100));

        assertThatThrownBy(() -> flightService.createFlight(new CreateFlightRequest("DD000", 50)))
                .hasMessageContaining("DD000");
    }

    @Test
    void getFlightOrThrow_existingFlight_returnsFlight() {
        flightService.createFlight(new CreateFlightRequest("EE111", 75));

        Flight flight = flightService.getFlightOrThrow("EE111");

        assertThat(flight.getFlightNumber()).isEqualTo("EE111");
        assertThat(flight.getTotalSeats()).isEqualTo(75);
        assertThat(flight.getAvailableSeats().get()).isEqualTo(75);
    }

    @Test
    void getFlightOrThrow_nonExistentFlight_throwsFlightNotFoundException() {
        assertThatThrownBy(() -> flightService.getFlightOrThrow("UNKNOWN"))
                .isInstanceOf(FlightNotFoundException.class);
    }

    @Test
    void getFlightOrThrow_exceptionMessage_containsFlightNumber() {
        assertThatThrownBy(() -> flightService.getFlightOrThrow("GHOST1"))
                .hasMessageContaining("GHOST1");
    }
}
