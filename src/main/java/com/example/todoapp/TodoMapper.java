package com.example.todoapp;

import java.util.List;
import java.time.LocalDate;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoMapper {

    List<Todo> search(@Param("keyword") String keyword,
                      @Param("category") String category,
                      @Param("order") String order,
                      @Param("from") LocalDate from,
                      @Param("to") LocalDate to,
                      @Param("completedOnly") Boolean completedOnly,
                      @Param("deletedOnly") Boolean deletedOnly,
                      @Param("limit") int limit,
                      @Param("offset") int offset);

    int countSearch(@Param("keyword") String keyword,
                    @Param("category") String category,
                    @Param("from") LocalDate from,
                    @Param("to") LocalDate to,
                    @Param("completedOnly") Boolean completedOnly,
                    @Param("deletedOnly") Boolean deletedOnly);

    Todo findById(Long id);

    Todo findDeletedById(Long id);

    void insert(Todo todo);

    void update(Todo todo);

    void deleteById(Long id);

    void restoreById(Long id);
}
