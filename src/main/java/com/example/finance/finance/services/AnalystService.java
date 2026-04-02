package com.example.finance.finance.services;

import com.example.finance.finance.dto.AnalystDashboardDto;
import com.example.finance.finance.entity.records;
import com.example.finance.finance.repository.record_repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalystService {

    @Autowired
    private record_repository record_repository;

    public AnalystDashboardDto getDashboard(String trendType) {
        List<records> all = record_repository.findAll();

        double totalIncome = all.stream()
                .filter(r -> r.getType() == records.Type.INCOME)
                .mapToDouble(r -> r.getAmount() == null ? 0.0 : r.getAmount())
                .sum();

        double totalExpenses = all.stream()
                .filter(r -> r.getType() == records.Type.EXPENSE)
                .mapToDouble(r -> r.getAmount() == null ? 0.0 : r.getAmount())
                .sum();

        double netBalance = totalIncome - totalExpenses;

        Map<String, Double> grouped = all.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCategory() + "|" + r.getType().name(),
                        Collectors.summingDouble(r -> r.getAmount() == null ? 0.0 : r.getAmount())
                ));

        List<AnalystDashboardDto.CategoryTotalDto> categoryTotals = grouped.entrySet().stream()
                .map(e -> {
                    String[] parts = e.getKey().split("\\|");
                    return new AnalystDashboardDto.CategoryTotalDto(
                            parts[0],
                            records.Type.valueOf(parts[1]),
                            e.getValue()
                    );
                })
                .sorted(Comparator.comparing(AnalystDashboardDto.CategoryTotalDto::getCategory))
                .toList();

        List<AnalystDashboardDto.RecentActivityDto> recentActivity =
                record_repository.findTop10ByOrderByDateDescIdDesc().stream()
                        .map(r -> new AnalystDashboardDto.RecentActivityDto(
                                r.getId(), r.getAmount(), r.getCategory(), r.getNote(), r.getDate(), r.getType()
                        ))
                        .toList();

        List<AnalystDashboardDto.TrendPointDto> trends = buildTrends(all, trendType);

        return new AnalystDashboardDto(
                totalIncome,
                totalExpenses,
                netBalance,
                categoryTotals,
                recentActivity,
                trends
        );
    }

    private List<AnalystDashboardDto.TrendPointDto> buildTrends(List<records> all, String trendType) {
        String normalized = trendType == null ? "monthly" : trendType.trim().toLowerCase(Locale.ROOT);
        Map<String, double[]> bucket = new TreeMap<>();

        for (records r : all) {
            if (r.getDate() == null || r.getType() == null) continue;

            LocalDate d = r.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String label;

            if ("weekly".equals(normalized)) {
                WeekFields wf = WeekFields.ISO;
                int week = d.get(wf.weekOfWeekBasedYear());
                int year = d.get(wf.weekBasedYear());
                label = year + "-W" + String.format("%02d", week);
            } else {
                label = d.getYear() + "-" + String.format("%02d", d.getMonthValue());
            }

            bucket.putIfAbsent(label, new double[]{0.0, 0.0});
            double amt = r.getAmount() == null ? 0.0 : r.getAmount();

            if (r.getType() == records.Type.INCOME) {
                bucket.get(label)[0] += amt;
            } else {
                bucket.get(label)[1] += amt;
            }
        }

        return bucket.entrySet().stream()
                .map(e -> new AnalystDashboardDto.TrendPointDto(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .toList();
    }
}
