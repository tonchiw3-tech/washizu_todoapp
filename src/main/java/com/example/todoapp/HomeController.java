package com.example.todoapp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final TodoMapper todoMapper;

    public HomeController(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "やること管理");
        return "index";
    }

    @GetMapping("/todos")
    public String todos(Model model) {
        model.addAttribute("todos", todoMapper.findAll());
        return "todos";
    }

    @PostMapping("/todos")
    public String insertTodo(@ModelAttribute("todo") Todo todo,
                             RedirectAttributes redirectAttributes) {
        todoMapper.insert(todo);
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
    public String confirmTodo(@ModelAttribute("todo") Todo todo) {
        return "create-confirm";
    }

    @GetMapping("/todos/{id}/edit")
    public String editTodo(@PathVariable Long id,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        Todo todo = todoMapper.findById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("message", "見つかりませんでした");
            return "redirect:/todos";
        }
        model.addAttribute("todo", todo);
        return "edit";
    }

    @PostMapping("/todos/{id}/confirm")
    public String confirmEditTodo(@PathVariable Long id,
                                 @ModelAttribute("todo") Todo todo) {
        todo.setId(id);
        return "edit-confirm";
    }

    @PostMapping("/todos/{id}")
    public String updateTodo(@PathVariable Long id,
                             @ModelAttribute("todo") Todo todo,
                             RedirectAttributes redirectAttributes) {
        todo.setId(id);
        todoMapper.update(todo);
        redirectAttributes.addFlashAttribute("message", "保存しました");
        return "redirect:/todos";
    }

    @PostMapping("/todos/{id}/edit")
    public String rewriteEditTodo(@PathVariable Long id,
                                 @ModelAttribute("todo") Todo todo) {
        todo.setId(id);
        return "edit";
    }

    @GetMapping("/todos/{id}/delete")
    public String deleteTodo(@PathVariable Long id,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        Todo todo = todoMapper.findById(id);
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
        todoMapper.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "削除しました");
        return "redirect:/todos";
    }
}
