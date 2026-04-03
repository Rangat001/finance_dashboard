package com.example.finance.finance.services;

import com.example.finance.finance.entity.records;
import com.example.finance.finance.repository.record_repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class RecordService {
    @Autowired
    record_repository record_repository;

    public List<records> search(Date start, Date end, String category, records.Type type) {
        return record_repository.search(start, end, normalizeCategory(category), type);
    }

    private String normalizeCategory(String category) {
        if (category == null) return null;
        String c = category.trim();
        return c.isEmpty() ? null : c;
    }
}
