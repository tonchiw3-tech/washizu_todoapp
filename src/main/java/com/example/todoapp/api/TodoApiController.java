package com.example.todoapp.api;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.net.URI;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.todoapp.TodoService;
import com.example.todoapp.Todo;

@RestController
public class TodoApiController {

    private final TodoService todoService;

    public TodoApiController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/api/todos")
    public List<TodoDto> todos(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "category", defaultValue = "") String category,
            @RequestParam(name = "order", defaultValue = "asc") String order,
            @RequestParam(name = "from", required = false) LocalDate from,
            @RequestParam(name = "to", required = false) LocalDate to) {
        String sortOrder = "desc".equals(order) ? "desc" : "asc";
        return todoService.search(keyword, category, sortOrder, from, to).stream()
                .map(TodoDto::from)
                .toList();
    }

    @GetMapping(value = "/api/todos.csv", produces = "text/csv")
    public ResponseEntity<byte[]> todosCsv(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "category", defaultValue = "") String category,
            @RequestParam(name = "order", defaultValue = "asc") String order,
            @RequestParam(name = "includeCompleted", defaultValue = "false") boolean includeCompleted) {
        String sortOrder = "desc".equals(order) ? "desc" : "asc";
        List<Todo> todos = todoService.searchForList(keyword, category, sortOrder,
                !includeCompleted, Integer.MAX_VALUE, 0);

        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("題名,ジャンル,優先度,期限,状態\r\n");
        for (Todo todo : todos) {
            String status = Boolean.TRUE.equals(todo.getCompleted()) ? "完了" : "未完了";
            if (Boolean.TRUE.equals(todo.getCompleted()) && todo.getCompletedAt() != null) {
                status += " " + todo.getCompletedAt().toLocalDate();
            }
            csv.append(csvRow(todo.getTitle(), todo.getCategory(),
                    todo.getPriority() == 1 ? "高" : todo.getPriority() == 2 ? "中" : "低",
                    todo.getDueDate() == null ? "" : todo.getDueDate().toString(), status));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDispositionFormData("attachment", "todos.csv");
        return new ResponseEntity<>(csv.toString().getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
    }

    private static String csvRow(String... values) {
        StringJoiner row = new StringJoiner(",");
        for (String value : values) {
            String safe = value == null ? "" : value;
            if (safe.startsWith("=") || safe.startsWith("+") || safe.startsWith("-") || safe.startsWith("@")) {
                safe = "'" + safe;
            }
            row.add("\"" + safe.replace("\"", "\"\"") + "\"");
        }
        return row + "\r\n";
    }

    @GetMapping("/api/todos/{id}")
    public ResponseEntity<?> todo(@PathVariable Long id) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            return notFound(id);
        }
        return ResponseEntity.ok(TodoDto.from(todo));
    }

    @PostMapping("/api/todos")
    public ResponseEntity<TodoDto> create(@Valid @RequestBody TodoRequest request) {
        Todo todo = request.toTodo();
        if (todo.getCompleted() == null) {
            todo.setCompleted(false);
        }
        todoService.create(todo);
        Todo created = todoService.findById(todo.getId());
        return ResponseEntity.created(URI.create("/api/todos/" + todo.getId()))
                .body(TodoDto.from(created));
    }

    @PutMapping("/api/todos/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody TodoRequest request) {
        if (todoService.findById(id) == null) {
            return notFound(id);
        }
        Todo todo = request.toTodo();
        todo.setId(id);
        todoService.update(todo);
        return ResponseEntity.ok(TodoDto.from(todoService.findById(id)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationError(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("about:blank"));
        problem.setTitle("Bad Request");
        problem.setDetail("入力に誤りがあります");
        problem.setInstance(URI.create(request.getRequestURI()));

        var errors = new ArrayList<Map<String, String>>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            errors.add(Map.of("field", error.getField(), "message", error.getDefaultMessage()));
        }
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problem);
    }

    @DeleteMapping("/api/todos/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (todoService.findById(id) == null) {
            return notFound(id);
        }
        todoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<ProblemDetail> notFound(Long id) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setType(URI.create("about:blank"));
        problem.setTitle("Todo not found");
        problem.setDetail("Todo with id " + id + " was not found.");
        problem.setInstance(URI.create("/api/todos/" + id));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }
}
