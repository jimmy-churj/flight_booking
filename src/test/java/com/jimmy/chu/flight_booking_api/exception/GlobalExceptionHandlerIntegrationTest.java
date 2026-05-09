package com.jimmy.chu.flight_booking_api.exception;

import com.jimmy.chu.flight_booking_api.service.FlightService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightService flightService;

    @Test
    void unexpectedException_returns500() throws Exception {
        when(flightService.createFlight(any())).thenThrow(new RuntimeException("Simulated failure"));

        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"flightNumber\":\"AA123\",\"totalSeats\":100}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void unexpectedException_responseBodyMatchesErrorFormat() throws Exception {
        when(flightService.createFlight(any())).thenThrow(new RuntimeException("Simulated failure"));

        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"flightNumber\":\"AA123\",\"totalSeats\":100}"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void unsupportedHttpMethod_returns405MethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/flights")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void unknownRoute_returns404NotFound() throws Exception {
        mockMvc.perform(get("/unknown-route"))
                .andExpect(status().isNotFound());
    }
}
