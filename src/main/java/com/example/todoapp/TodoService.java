package com.example.todoapp;

import java.util.List;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TodoService {

    private final TodoMapper todoMapper;

    public TodoService(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    public List<Todo> search(String keyword, String category, String order,
                              LocalDate from, LocalDate to) {
        return todoMapper.search(keyword, category, order, from, to, null, Integer.MAX_VALUE, 0);
    }

    public List<Todo> search(String keyword, String category, String order,
                             int limit, int offset) {
        return search(keyword, category, order, null, null, limit, offset);
    }

    public List<Todo> search(String keyword, String category, String order,
                             LocalDate from, LocalDate to, int limit, int offset) {
        return todoMapper.search(keyword, category, order, from, to, null, limit, offset);
    }

    public int countSearch(String keyword, String category) {
        return todoMapper.countSearch(keyword, category, null, null, null);
    }

    public int countSearch(String keyword, String category, LocalDate from, LocalDate to) {
        return todoMapper.countSearch(keyword, category, from, to, null);
    }

    public List<Todo> search(String keyword, String category, String order) {
        return search(keyword, category, order, null, null);
    }

    public List<Todo> searchForList(String keyword, String category, String order,
                                    Boolean completedOnly, int limit, int offset) {
        return todoMapper.search(keyword, category, order, null, null, completedOnly, limit, offset);
    }

    public int countListSearch(String keyword, String category, Boolean completedOnly) {
        return todoMapper.countSearch(keyword, category, null, null, completedOnly);
    }

    public Todo findById(Long id) {
        return todoMapper.findById(id);
    }

    public void create(Todo todo) {
        todoMapper.insert(todo);
        log.info("Todo 登録: id={}", todo.getId());
    }

    public void update(Todo todo) {
        todoMapper.update(todo);
        log.info("Todo 編集: id={}", todo.getId());
    }

    public void delete(Long id) {
        todoMapper.deleteById(id);
        log.info("Todo 削除: id={}", id);
    }
}
