package com.example.todoapp.api;

import java.util.Map;
import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

public class HolidayClient {

    private static final String HOLIDAYS_API_URL = "https://holidays-jp.github.io/api/v1/date.json";

    private final RestClient restClient;

    public HolidayClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public HolidayResult fetchHolidays() {
        try {
            Map<String, String> holidays = restClient.get()
                    .uri(HOLIDAYS_API_URL)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, String>>() {
                    });
            return new HolidayResult(holidays == null ? Map.of() : holidays, false);
        } catch (RestClientException e) {
            return new HolidayResult(Map.of(), true);
        }
    }

    public record HolidayResult(Map<String, String> holidays, boolean unavailable) {
    }
}
