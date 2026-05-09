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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FlightControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createFlight_validRequest_returns201Created() throws Exception {
        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(flightBody(uniqueFlight(), 180)))
                .andExpect(status().isCreated());
    }

    @Test
    void createFlight_validRequest_responseContainsAllFields() throws Exception {
        String fn = uniqueFlight();
        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(flightBody(fn, 180)))
                .andExpect(jsonPath("$.flightNumber").value(fn))
                .andExpect(jsonPath("$.totalSeats").value(180))
                .andExpect(jsonPath("$.availableSeats").value(180));
    }

    @Test
    void createFlight_availableSeatsMatchesTotalSeats() throws Exception {
        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(flightBody(uniqueFlight(), 50)))
                .andExpect(jsonPath("$.availableSeats").value(50));
    }

    @Test
    void createFlight_duplicateFlightNumber_returns409Conflict() throws Exception {
        String fn = uniqueFlight();
        String body = flightBody(fn, 100);

        mockMvc.perform(post("/flights").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/flights").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void createFlight_duplicateFlightNumber_responseContainsMessage() throws Exception {
        String fn = uniqueFlight();
        String body = flightBody(fn, 100);

        mockMvc.perform(post("/flights").contentType(MediaType.APPLICATION_JSON).content(body));

        mockMvc.perform(post("/flights").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void createFlight_blankFlightNumber_returns400BadRequest() throws Exception {
        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(flightBody("", 100)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFlight_missingFlightNumber_returns400BadRequest() throws Exception {
        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("totalSeats", 100))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFlight_flightNumberExceedsMaxLength_returns400BadRequest() throws Exception {
        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(flightBody("TOOLONGFLIGHT", 100)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFlight_zeroTotalSeats_returns400BadRequest() throws Exception {
        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(flightBody(uniqueFlight(), 0)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFlight_negativeTotalSeats_returns400BadRequest() throws Exception {
        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(flightBody(uniqueFlight(), -1)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFlight_emptyBody_returns400BadRequest() throws Exception {
        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFlight_validationError_responseContainsMessage() throws Exception {
        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(flightBody("", 100)))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void createFlight_validationError_responseContainsFieldErrors() throws Exception {
        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(flightBody("", 100)))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.flightNumber").isString());
    }

    @Test
    void createFlight_malformedBody_returns400BadRequest() throws Exception {
        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isString());
    }

    private String flightBody(String flightNumber, int totalSeats) throws Exception {
        return objectMapper.writeValueAsString(Map.of("flightNumber", flightNumber, "totalSeats", totalSeats));
    }

    private String uniqueFlight() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }
}
