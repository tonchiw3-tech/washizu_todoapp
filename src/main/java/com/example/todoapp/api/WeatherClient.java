package com.example.todoapp.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Component;

@Component
public class WeatherClient {
    private static final String URL = "https://api.open-meteo.com/v1/forecast";
    private final RestClient client;

    public WeatherClient() {
        HttpClient h = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        JdkClientHttpRequestFactory f = new JdkClientHttpRequestFactory(h);
        f.setReadTimeout(Duration.ofSeconds(5));
        client = RestClient.builder().requestFactory(f).build();
    }

    public WeatherResult fetchWeather(LocalDate from, LocalDate to) {
        try {
            LocalDate requestTo = from.plusDays(15).isBefore(to) ? from.plusDays(15) : to;
            String u = URL + "?latitude=35.68&longitude=139.76&daily=weather_code&timezone=Asia%2FTokyo&start_date="
                    + from + "&end_date=" + requestTo;
            Map<?, ?> root = client.get().uri(URI.create(u)).retrieve().body(Map.class);
            Map<?, ?> d = (Map<?, ?>) root.get("daily");
            List<?> dates = (List<?>) d.get("time"), codes = (List<?>) d.get("weather_code");
            Map<String, String> w = new LinkedHashMap<>();
            for (int i = 0; i < dates.size() && i < codes.size(); i++)
                w.put(String.valueOf(dates.get(i)), jp(((Number) codes.get(i)).intValue()));
            return new WeatherResult(w, false);
        } catch (RuntimeException e) {
            return new WeatherResult(Map.of(), true);
        }
    }

    private String jp(int c) {
        if (c == 0)
            return "\u6674";
        if (c <= 3)
            return "\u66c7";
        if (c <= 48)
            return "\u9727";
        if (c <= 67 || c >= 80 && c <= 82)
            return "\u96e8";
        if (c <= 77 || c >= 85 && c <= 86)
            return "\u96ea";
        return "\u96f7\u96e8";
    }

    public record WeatherResult(Map<String, String> weather, boolean unavailable) {
    }
}
