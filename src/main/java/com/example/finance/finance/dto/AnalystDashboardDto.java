package com.example.finance.finance.dto;

import com.example.finance.finance.entity.records;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AnalystDashboardDto {
    private double totalIncome;
    private double totalExpenses;
    private double netBalance;
    private List<CategoryTotalDto> categoryWiseTotals;
    private List<RecentActivityDto> recentActivity;
    private List<TrendPointDto> trends;

    @Data
    @AllArgsConstructor
    public static class CategoryTotalDto {
        private String category;
        private records.Type type;
        private double total;
    }

    @Data
    @AllArgsConstructor
    public static class RecentActivityDto {
        private int id;
        private float amount;
        private String category;
        private String note;
        private java.util.Date date;
        private records.Type type;
    }

    @Data
    @AllArgsConstructor
    public static class TrendPointDto {
        private String label; // e.g. 2026-04 or 2026-W14
        private double income;
        private double expense;
    }
}