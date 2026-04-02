package com.example.finance.finance.repository;

import com.example.finance.finance.entity.records;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface record_repository extends JpaRepository<records, Integer> {
    List<records> findTop10ByOrderByDateDescIdDesc();
    List<records> findByDateBetweenOrderByDateAsc(Date start, Date end);
}
