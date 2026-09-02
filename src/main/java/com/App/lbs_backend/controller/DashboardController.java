package com.App.lbs_backend.controller;

import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final HttpServletRequest request;

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK",
                dashboardService.getStats(), request.getRequestURI()));
    }
}
