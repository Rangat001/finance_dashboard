package com.example.finance.finance.controller;

import com.example.finance.finance.entity.records;
import com.example.finance.finance.services.ViewerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/view")

public class ViewerController {
    @Autowired
    ViewerService viewerService;

    @GetMapping("/records")
    public ResponseEntity<?> getRecords() {
        List<records> l = viewerService.getAllRecords();
        if (l.size() == 0) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(l);
    }

}

