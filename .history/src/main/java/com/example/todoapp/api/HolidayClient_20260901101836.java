package com.example.todoapp.api;

import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

public class HolidayClient {

    private static final String HOLIDAYS_API_URL = "https://holidays-jp.github.invalid/api/v1/date.json";

    private final RestClient restClient;

    public HolidayClient() {
        this.restClient = RestClient.create();
    }

    public Map<String, String> fetchHolidays() {
        return restClient.get()
                .uri(HOLIDAYS_API_URL)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, String>>() {
                });
    }
}
