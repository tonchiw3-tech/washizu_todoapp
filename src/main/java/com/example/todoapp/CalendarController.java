package com.example.todoapp;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CalendarController {

    @GetMapping("/calendar")
    public String calendar(@RequestParam(required = false) Integer year,
                           @RequestParam(required = false) Integer month,
                           Model model) {
        LocalDate today = LocalDate.now();
        YearMonth targetMonth = YearMonth.of(
                year != null ? year : today.getYear(),
                month != null ? month : today.getMonthValue());

        LocalDate firstDay = targetMonth.atDay(1);
        LocalDate lastDay = targetMonth.atEndOfMonth();
        List<LocalDate> calendarDays = new ArrayList<>();

        for (int i = 0; i < firstDay.getDayOfWeek().getValue() % 7; i++) {
            calendarDays.add(null);
        }
        for (int day = 1; day <= targetMonth.lengthOfMonth(); day++) {
            calendarDays.add(targetMonth.atDay(day));
        }
        while (calendarDays.size() % 7 != 0) {
            calendarDays.add(null);
        }

        model.addAttribute("displayYear", targetMonth.getYear());
        model.addAttribute("displayMonth", targetMonth.getMonthValue());
        model.addAttribute("firstDay", firstDay);
        model.addAttribute("lastDay", lastDay);
        List<List<LocalDate>> calendarWeeks = new ArrayList<>();
        for (int i = 0; i < calendarDays.size(); i += 7) {
            calendarWeeks.add(calendarDays.subList(i, i + 7));
        }
        model.addAttribute("calendarWeeks", calendarWeeks);
        model.addAttribute("previousMonth", targetMonth.minusMonths(1));
        model.addAttribute("nextMonth", targetMonth.plusMonths(1));
        return "calendar";
    }
}
