package com.example.todoapp;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final TodoService todoService;

    public HomeController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "やること管理");
        return "index";
    }

    @GetMapping("/mcp")
    public String mcpTools() {
        return "mcp";
    }

    @GetMapping("/todos")
    public String todos(@RequestParam(name = "keyword", defaultValue = "") String keyword,
                        @RequestParam(name = "category", defaultValue = "") String category,
                        @RequestParam(name = "order", defaultValue = "asc") String order,
                        @RequestParam(name = "includeCompleted", defaultValue = "false") boolean includeCompleted,
                        @RequestParam(name = "page", defaultValue = "1") int page,
                        @RequestParam(name = "trash", defaultValue = "0") int trash,
                        Model model) {
        String sortOrder = "desc".equals(order) ? "desc" : "asc";
        boolean trashView = trash == 1;
        int totalPages = Math.max(1, ((trashView ? todoService.countTrashSearch(keyword, category) : todoService.countListSearch(keyword, category, !includeCompleted)) + 9) / 10);
        int currentPage = Math.max(1, Math.min(page, totalPages));
        model.addAttribute("todos", trashView ? todoService.searchForTrash(keyword, category, sortOrder, 10, (currentPage - 1) * 10) : todoService.searchForList(keyword, category, sortOrder,
                !includeCompleted, 10, (currentPage - 1) * 10));
        model.addAttribute("trash", trashView);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("order", sortOrder);
        model.addAttribute("includeCompleted", includeCompleted);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        return "todos";
    }

    @PostMapping("/todos")
    public String insertTodo(@ModelAttribute("todo") Todo todo,
                             RedirectAttributes redirectAttributes) {
        todoService.create(todo);
        redirectAttributes.addFlashAttribute("message", "登録しました");
        return "redirect:/todos";
    }

    @GetMapping("/todos/new")
    public String newTodo(Model model) {
        model.addAttribute("todo", new Todo());
        return "create";
    }

    @PostMapping("/todos/new")
    public String rewriteTodo(@ModelAttribute("todo") Todo todo) {
        return "create";
    }

    @PostMapping("/todos/confirm")
    public String confirmTodo(@Valid @ModelAttribute("todo") Todo todo,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "create";
        }
        return "create-confirm";
    }

    @GetMapping("/todos/{id}/edit")
    public String editTodo(@PathVariable Long id,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("message", "見つかりませんでした");
            return "redirect:/todos";
        }
        model.addAttribute("todo", todo);
        return "edit";
    }

    @PostMapping("/todos/{id}/confirm")
    public String confirmEditTodo(@PathVariable Long id,
                                  @Valid @ModelAttribute("todo") Todo todo,
                                  BindingResult bindingResult) {
        todo.setId(id);
        if (bindingResult.hasErrors()) {
            return "edit";
        }
        return "edit-confirm";
    }

    @PostMapping("/todos/{id}")
    public String updateTodo(@PathVariable Long id,
                             @ModelAttribute("todo") Todo todo,
                             RedirectAttributes redirectAttributes) {
        todo.setId(id);
        todoService.update(todo);
        redirectAttributes.addFlashAttribute("message", "保存しました");
        return "redirect:/todos";
    }

    @PostMapping("/todos/{id}/edit")
    public String rewriteEditTodo(@PathVariable Long id,
                                 @ModelAttribute("todo") Todo todo) {
        todo.setId(id);
        return "edit";
    }

    @PostMapping("/todos/{id}/pin")
    public String togglePinned(@PathVariable Long id,
                               @RequestParam(name = "keyword", defaultValue = "") String keyword,
                               @RequestParam(name = "category", defaultValue = "") String category,
                               @RequestParam(name = "order", defaultValue = "asc") String order,
                               @RequestParam(name = "includeCompleted", defaultValue = "false") boolean includeCompleted,
                               @RequestParam(name = "page", defaultValue = "1") int page) {
        Todo todo = todoService.findById(id);
        if (todo != null) todoService.togglePinned(id, !Boolean.TRUE.equals(todo.getPinned()));
        return "redirect:/todos?keyword=" + java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8)
                + "&category=" + java.net.URLEncoder.encode(category, java.nio.charset.StandardCharsets.UTF_8)
                + "&order=" + java.net.URLEncoder.encode(order, java.nio.charset.StandardCharsets.UTF_8)
                + "&includeCompleted=" + includeCompleted + "&page=" + page;
    }

    @GetMapping("/todos/{id}/delete")
    public String deleteTodo(@PathVariable Long id,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("message", "見つかりませんでした");
            return "redirect:/todos";
        }
        model.addAttribute("todo", todo);
        return "delete";
    }

    @PostMapping("/todos/{id}/delete")
    public String executeDeleteTodo(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        todoService.delete(id);
        redirectAttributes.addFlashAttribute("message", "削除しました");
        return "redirect:/todos";
    }

    @PostMapping("/todos/{id}/restore")
    public String restoreTodo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        todoService.restore(id);
        redirectAttributes.addFlashAttribute("message", "戻しました");
        return "redirect:/todos";
    }
}
