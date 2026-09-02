package com.example.todoapp.mcp;

import java.time.LocalDate;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import com.example.todoapp.Todo;
import com.example.todoapp.TodoService;
import com.example.todoapp.api.HolidayClient;
import com.example.todoapp.api.TodoDto;

@Component
public class TodoTools {

    private final TodoService todoService;
    private final HolidayClient holidayClient;

    public TodoTools(TodoService todoService, HolidayClient holidayClient) {
        this.todoService = todoService;
        this.holidayClient = holidayClient;
    }

    @McpTool(name = "list_todos", description = "やることの一覧を返す（期間・ジャンルで絞れる）")
    public List<TodoDto> listTodos(
            @McpToolParam(required = false, description = "キーワード") String keyword,
            @McpToolParam(required = false, description = "ジャンル") String category,
            @McpToolParam(required = false, description = "開始日（yyyy-MM-dd）") String from,
            @McpToolParam(required = false, description = "終了日（yyyy-MM-dd）") String to) {
        LocalDate fromDate = from == null ? null : LocalDate.parse(from);
        LocalDate toDate = to == null ? null : LocalDate.parse(to);

        return todoService.search(keyword, category, "asc", fromDate, toDate).stream()
                .map(TodoDto::from)
                .toList();
    }

    @McpTool(name = "get_todo", description = "やることを1件返す")
    public TodoDto getTodo(
            @McpToolParam(required = true, description = "やることのID") Long id) {
        Todo todo = todoService.findById(id);
        return todo == null ? null : TodoDto.from(todo);
    }
    @McpTool(name = "add_todo", description = "やることを1件足す")
    public TodoDto addTodo(@McpToolParam(required = true, description = "やること") String title,
            @McpToolParam(required = false, description = "メモ") String detail,
            @McpToolParam(required = true, description = "ジャンル") String category,
            @McpToolParam(required = true, description = "優先度") Integer priority,
            @McpToolParam(required = false, description = "期限（yyyy-MM-dd）") String dueDate) {
        validateCategory(category);
        Todo todo = new Todo();
        todo.setTitle(title); todo.setDetail(detail); todo.setCategory(category); todo.setPriority(priority);
        todo.setDueDate(dueDate == null ? null : LocalDate.parse(dueDate)); todo.setCompleted(false);
        todoService.create(todo);
        return TodoDto.from(todo);
    }

    @McpTool(name = "update_todo", description = "やることを1件直す（期限を変えるのもこれ）")
    public TodoDto updateTodo(@McpToolParam(required = true, description = "やることのID") Long id,
            @McpToolParam(required = false, description = "やること") String title,
            @McpToolParam(required = false, description = "メモ") String detail,
            @McpToolParam(required = false, description = "ジャンル") String category,
            @McpToolParam(required = false, description = "優先度") Integer priority,
            @McpToolParam(required = false, description = "期限（yyyy-MM-dd）") String dueDate) {
        Todo todo = todoService.findById(id); if (todo == null) return null;
        if (title != null) todo.setTitle(title); if (detail != null) todo.setDetail(detail);
        if (category != null) { validateCategory(category); todo.setCategory(category); }
        if (priority != null) todo.setPriority(priority);
        if (dueDate != null) todo.setDueDate(LocalDate.parse(dueDate));
        todoService.update(todo); return TodoDto.from(todo);
    }

    @McpTool(name = "complete_todo", description = "やることを完了にする")
    public TodoDto completeTodo(@McpToolParam(required = true, description = "やることのID") Long id) {
        Todo todo = todoService.findById(id); if (todo == null) return null;
        todo.setCompleted(true); todoService.update(todo); return TodoDto.from(todo);
    }

    @McpTool(name = "delete_todo", description = "やることを1件消す")
    public void deleteTodo(@McpToolParam(required = true, description = "やることのID") Long id) { todoService.delete(id); }

    @McpTool(name = "find_free_days", description = "期間の中で、期限のやることが無く、土日でも祝日でもない空いている日を返す")
    public List<LocalDate> findFreeDays(
            @McpToolParam(required = true, description = "開始日（yyyy-MM-dd）") String from,
            @McpToolParam(required = true, description = "終了日（yyyy-MM-dd）") String to) {
        LocalDate start = LocalDate.parse(from), end = LocalDate.parse(to);
        Set<LocalDate> due = new HashSet<>(todoService.search(null, null, "asc", start, end).stream()
                .map(Todo::getDueDate).filter(java.util.Objects::nonNull).toList());
        Set<LocalDate> holidays = holidayClient.fetchHolidays().holidays().keySet().stream()
                .map(LocalDate::parse).collect(java.util.stream.Collectors.toSet());
        return start.datesUntil(end.plusDays(1)).filter(d -> d.getDayOfWeek().getValue() < 6)
                .filter(d -> !due.contains(d)).filter(d -> !holidays.contains(d)).toList();
    }

    private void validateCategory(String category) {
        if (!Set.of("デザイン", "マーケティング", "プログラミング", "資格", "就職活動").contains(category))
            throw new IllegalArgumentException("ジャンルは指定された5種類のいずれかを指定してください");
    }
}
