package com.example.todoapp.api;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HolidayApiController {

    private final HolidayClient holidayClient;

    public HolidayApiController() {
        this.holidayClient = new HolidayClient();
    }

    @GetMapping("/api/holidays")
    public Map<String, String> holidays(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        Map<String, String> holidays = holidayClient.fetchHolidays();

        if (from == null && to == null) {
            return holidays;
        }

        return holidays.entrySet().stream()
                .filter(entry -> {
                    LocalDate date = LocalDate.parse(entry.getKey());
                    return (from == null || !date.isBefore(from))
                            && (to == null || !date.isAfter(to));
                })
                .collect(TreeMap::new,
                        (result, entry) -> result.put(entry.getKey(), entry.getValue()),
                        TreeMap::putAll);
    }
}
