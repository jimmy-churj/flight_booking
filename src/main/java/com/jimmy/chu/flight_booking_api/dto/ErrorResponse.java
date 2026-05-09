package com.jimmy.chu.flight_booking_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String message, Map<String, String> errors) {

    public ErrorResponse(String message) {
        this(message, null);
    }
}
