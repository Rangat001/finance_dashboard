package com.example.finance.finance.repository;

import com.example.finance.finance.entity.records;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface record_repository extends JpaRepository<records, Integer> {
    List<records> findTop10ByOrderByDateDescIdDesc();
    List<records> findByDateBetweenOrderByDateAsc(Date start, Date end);

    @Query("""
        SELECT r
        FROM records r
        WHERE (:start IS NULL OR r.date >= :start)
          AND (:end IS NULL OR r.date <= :end)
          AND (:category IS NULL OR r.category = :category)
          AND (:type IS NULL OR r.type = :type)
        ORDER BY r.date ASC, r.id ASC
    """)
    List<records> search(
            @Param("start") Date start,
            @Param("end") Date end,
            @Param("category") String category,
            @Param("type") records.Type type
    );
}
