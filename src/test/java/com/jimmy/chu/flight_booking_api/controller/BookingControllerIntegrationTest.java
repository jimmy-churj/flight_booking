package com.jimmy.chu.flight_booking_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String PASSENGER_NAME = "Jimmy Chu";
    private static final String PASSENGER_EMAIL = "jimmy@example.com";

    @Test
    void createBooking_validRequest_returns201Created() throws Exception {
        String fn = uniqueFlight();
        registerFlight(fn, 10);

        mockMvc.perform(post("/flights/{fn}/bookings", fn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody(PASSENGER_NAME, PASSENGER_EMAIL)))
                .andExpect(status().isCreated());
    }

    @Test
    void createBooking_validRequest_responseContainsAllFields() throws Exception {
        String fn = uniqueFlight();
        registerFlight(fn, 10);

        mockMvc.perform(post("/flights/{fn}/bookings", fn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody(PASSENGER_NAME, PASSENGER_EMAIL)))
                .andExpect(jsonPath("$.bookingId").isString())
                .andExpect(jsonPath("$.flightNumber").value(fn))
                .andExpect(jsonPath("$.passengerName").value(PASSENGER_NAME))
                .andExpect(jsonPath("$.passengerEmail").value(PASSENGER_EMAIL))
                .andExpect(jsonPath("$.bookedAt").isString());
    }

    @Test
    void createBooking_bookingIdIsValidUUID() throws Exception {
        String fn = uniqueFlight();
        registerFlight(fn, 10);

        String responseBody = mockMvc.perform(post("/flights/{fn}/bookings", fn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody(PASSENGER_NAME, PASSENGER_EMAIL)))
                .andReturn().getResponse().getContentAsString();

        String bookingId = objectMapper.readTree(responseBody).get("bookingId").asText();
        assertThatCode(() -> UUID.fromString(bookingId)).doesNotThrowAnyException();
    }

    @Test
    void createBooking_bookedAtIsPopulated() throws Exception {
        String fn = uniqueFlight();
        registerFlight(fn, 10);

        mockMvc.perform(post("/flights/{fn}/bookings", fn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody(PASSENGER_NAME, PASSENGER_EMAIL)))
                .andExpect(jsonPath("$.bookedAt").isNotEmpty());
    }

    @Test
    void createBooking_consecutiveBookings_haveUniqueBookingIds() throws Exception {
        String fn = uniqueFlight();
        registerFlight(fn, 10);

        String id1 = extractBookingId(fn, "Alice", "alice@example.com");
        String id2 = extractBookingId(fn, "Bob", "bob@example.com");

        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void createBooking_unknownFlightNumber_returns404NotFound() throws Exception {
        mockMvc.perform(post("/flights/UNKNOWN1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody(PASSENGER_NAME, PASSENGER_EMAIL)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBooking_unknownFlightNumber_responseContainsMessage() throws Exception {
        mockMvc.perform(post("/flights/UNKNOWN2/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody(PASSENGER_NAME, PASSENGER_EMAIL)))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void createBooking_flightWithNoAvailableSeats_returns409Conflict() throws Exception {
        String fn = uniqueFlight();
        registerFlight(fn, 1);
        bookFlight(fn, "Alice", "alice@example.com");

        mockMvc.perform(post("/flights/{fn}/bookings", fn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody("Bob", "bob@example.com")))
                .andExpect(status().isConflict());
    }

    @Test
    void createBooking_flightWithNoAvailableSeats_responseContainsMessage() throws Exception {
        String fn = uniqueFlight();
        registerFlight(fn, 1);
        bookFlight(fn, "Alice", "alice@example.com");

        mockMvc.perform(post("/flights/{fn}/bookings", fn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody("Bob", "bob@example.com")))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void createBooking_blankPassengerName_returns400BadRequest() throws Exception {
        String fn = uniqueFlight();
        registerFlight(fn, 10);

        mockMvc.perform(post("/flights/{fn}/bookings", fn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody("", PASSENGER_EMAIL)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_blankPassengerEmail_returns400BadRequest() throws Exception {
        String fn = uniqueFlight();
        registerFlight(fn, 10);

        mockMvc.perform(post("/flights/{fn}/bookings", fn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody(PASSENGER_NAME, "")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_invalidEmailFormat_returns400BadRequest() throws Exception {
        String fn = uniqueFlight();
        registerFlight(fn, 10);

        mockMvc.perform(post("/flights/{fn}/bookings", fn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody(PASSENGER_NAME, "not-an-email")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_emptyBody_returns400BadRequest() throws Exception {
        String fn = uniqueFlight();
        registerFlight(fn, 10);

        mockMvc.perform(post("/flights/{fn}/bookings", fn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_validationError_responseContainsMessage() throws Exception {
        String fn = uniqueFlight();
        registerFlight(fn, 10);

        mockMvc.perform(post("/flights/{fn}/bookings", fn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody("", PASSENGER_EMAIL)))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void createBooking_validationError_responseContainsFieldErrors() throws Exception {
        String fn = uniqueFlight();
        registerFlight(fn, 10);

        mockMvc.perform(post("/flights/{fn}/bookings", fn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody("", PASSENGER_EMAIL)))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.passengerName").isString());
    }

    @Test
    void createBooking_passengerNameTooLong_returns400BadRequest() throws Exception {
        String fn = uniqueFlight();
        registerFlight(fn, 10);

        mockMvc.perform(post("/flights/{fn}/bookings", fn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody("A".repeat(101), PASSENGER_EMAIL)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_passengerEmailTooLong_returns400BadRequest() throws Exception {
        String fn = uniqueFlight();
        registerFlight(fn, 10);
        String tooLongEmail = "a".repeat(244) + "@example.com"; // 256 chars

        mockMvc.perform(post("/flights/{fn}/bookings", fn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody(PASSENGER_NAME, tooLongEmail)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_malformedBody_returns400BadRequest() throws Exception {
        String fn = uniqueFlight();
        registerFlight(fn, 10);

        mockMvc.perform(post("/flights/{fn}/bookings", fn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void createBooking_multipleBookings_reduceAvailableSeatsAndPreventOverbooking() throws Exception {
        String fn = uniqueFlight();
        registerFlight(fn, 3);

        bookFlight(fn, "Alice", "alice@example.com");
        bookFlight(fn, "Bob", "bob@example.com");
        bookFlight(fn, "Charlie", "charlie@example.com");

        mockMvc.perform(post("/flights/{fn}/bookings", fn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody("Dave", "dave@example.com")))
                .andExpect(status().isConflict());
    }

    private void registerFlight(String flightNumber, int totalSeats) throws Exception {
        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("flightNumber", flightNumber, "totalSeats", totalSeats))))
                .andExpect(status().isCreated());
    }

    private void bookFlight(String flightNumber, String name, String email) throws Exception {
        mockMvc.perform(post("/flights/{fn}/bookings", flightNumber)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody(name, email)))
                .andExpect(status().isCreated());
    }

    private String extractBookingId(String flightNumber, String name, String email) throws Exception {
        String responseBody = mockMvc.perform(post("/flights/{fn}/bookings", flightNumber)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody(name, email)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("bookingId").asText();
    }

    private String bookingBody(String name, String email) throws Exception {
        return objectMapper.writeValueAsString(Map.of("passengerName", name, "passengerEmail", email));
    }

    private String uniqueFlight() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }
}
