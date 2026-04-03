package com.example.finance.finance.controller;

import com.example.finance.finance.entity.records;
import com.example.finance.finance.services.RecordService;
import com.example.finance.finance.services.ViewerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/view")

public class ViewerController {
    @Autowired
    ViewerService viewerService;

    @Autowired
    RecordService recordService;

    @GetMapping("/records")
    public ResponseEntity<?> getRecords() {
        List<records> l = viewerService.getAllRecords();
        if (l.size() == 0) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(l);
    }

    /**
     * GET /view/records/search
     *
     * Query params (all optional):
     * - start: yyyy-MM-dd
     * - end: yyyy-MM-dd
     * - category: exact match
     * - type: INCOME | EXPENSE
     *
     * Examples:
     * /finance/view/records/search?type=INCOME
     * /finance/view/records/search?category=Food&type=EXPENSE
     * /finance/view/records/search?start=2026-04-01&end=2026-04-30
     */
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date start,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date end,

            @RequestParam(required = false) String category,

            @RequestParam(required = false) records.Type type
    ) {
        if (start != null && end != null && start.after(end)) {
            return ResponseEntity.badRequest().body(
                    new ApiError("INVALID_DATE_RANGE", "`start` must be <= `end`.")
            );
        }

        List<records> result = recordService.search(start, end, category, type);

        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

    public record ApiError(String code, String message) {}

}

