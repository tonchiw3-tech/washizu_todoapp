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
                           @RequestParam(required = false) Integer day,
                           @RequestParam(required = false) String from,
                           @RequestParam(required = false, defaultValue = "month") String view,
                           Model model) {
        LocalDate today = LocalDate.now();

        List<LocalDate> calendarDays = new ArrayList<>();
        boolean weekView = "week".equals(view);
        LocalDate targetDate;
        YearMonth targetMonth;

        if (weekView && from != null && !from.isBlank()) {
            targetDate = LocalDate.parse(from);
            targetMonth = YearMonth.from(targetDate);
        } else {
            targetMonth = YearMonth.of(
                    year != null ? year : today.getYear(),
                    month != null ? month : today.getMonthValue());
            int targetDay = day != null ? Math.max(1, Math.min(day, targetMonth.lengthOfMonth())) : 1;
            targetDate = targetMonth.atDay(targetDay);
        }
        LocalDate firstDay;
        LocalDate lastDay;

        if (weekView) {
            firstDay = targetDate.minusDays(targetDate.getDayOfWeek().getValue() % 7);
            lastDay = firstDay.plusDays(6);
            for (int i = 0; i < 7; i++) {
                calendarDays.add(firstDay.plusDays(i));
            }
        } else {
            firstDay = targetMonth.atDay(1);
            lastDay = targetMonth.atEndOfMonth();
            for (int i = 0; i < firstDay.getDayOfWeek().getValue() % 7; i++) {
                calendarDays.add(null);
            }
            for (int date = 1; date <= targetMonth.lengthOfMonth(); date++) {
                calendarDays.add(targetMonth.atDay(date));
            }
            while (calendarDays.size() % 7 != 0) {
                calendarDays.add(null);
            }
        }

        LocalDate previousWeek = firstDay.minusWeeks(1);
        LocalDate nextWeek = firstDay.plusWeeks(1);

        model.addAttribute("displayYear", targetMonth.getYear());
        model.addAttribute("displayMonth", targetMonth.getMonthValue());
        model.addAttribute("displayDay", targetDate.getDayOfMonth());
        model.addAttribute("weekView", weekView);
        model.addAttribute("firstDay", firstDay);
        model.addAttribute("lastDay", lastDay);
        List<List<LocalDate>> calendarWeeks = new ArrayList<>();
        for (int i = 0; i < calendarDays.size(); i += 7) {
            calendarWeeks.add(calendarDays.subList(i, i + 7));
        }
        model.addAttribute("calendarWeeks", calendarWeeks);
        model.addAttribute("previousMonth", targetMonth.minusMonths(1));
        model.addAttribute("nextMonth", targetMonth.plusMonths(1));
        model.addAttribute("previousWeek", previousWeek);
        model.addAttribute("nextWeek", nextWeek);
        return "calendar";
    }
}
