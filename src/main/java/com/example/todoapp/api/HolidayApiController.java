package com.example.todoapp.api;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
public class HolidayApiController {

    private final HolidayClient holidayClient;

    public HolidayApiController() {
        this.holidayClient = new HolidayClient();
    }

    @GetMapping("/api/holidays")
    public ResponseEntity<Map<String, String>> holidays(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        HolidayClient.HolidayResult holidayResult = holidayClient.fetchHolidays();
        Map<String, String> holidays = holidayResult.holidays();

        if (from == null && to == null) {
            return response(holidayResult, holidays);
        }

        Map<String, String> filteredHolidays = holidays.entrySet().stream()
                .filter(entry -> {
                    LocalDate date = LocalDate.parse(entry.getKey());
                    return (from == null || !date.isBefore(from))
                            && (to == null || !date.isAfter(to));
                })
                .collect(TreeMap::new,
                        (result, entry) -> result.put(entry.getKey(), entry.getValue()),
                        TreeMap::putAll);
        return response(holidayResult, filteredHolidays);
    }

    private ResponseEntity<Map<String, String>> response(HolidayClient.HolidayResult result,
            Map<String, String> holidays) {
        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
        if (result.unavailable()) {
            response.header("X-Holidays-Unavailable", "true");
        }
        return response.body(holidays);
    }
}
