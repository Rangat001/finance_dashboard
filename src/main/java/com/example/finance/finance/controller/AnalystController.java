package com.example.finance.finance.controller;

import com.example.finance.finance.dto.AnalystDashboardDto;
import com.example.finance.finance.services.AnalystService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analyst")
public class AnalystController {

    @Autowired
    private AnalystService analystService;

    @GetMapping("/dashboard")
    public ResponseEntity<AnalystDashboardDto> getDashboard(
            @RequestParam(defaultValue = "monthly") String trend
    ) {
        return ResponseEntity.ok(analystService.getDashboard(trend));
    }
}
