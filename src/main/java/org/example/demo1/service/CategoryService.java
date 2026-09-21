package org.example.demo1.service;

import org.example.demo1.pojo.PageResult;

import java.util.List;
import java.util.Map;

public interface CategoryService {
    List<Map<String, Object>> listEnabled();

    PageResult<Map<String, Object>> adminPage(int page, int size, String name, Integer status);

    Long create(String name, Integer sortNo);

    void update(Long id, String name, Integer status, Integer sortNo);

    void delete(Long id);
}
