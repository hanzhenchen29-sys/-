package org.example.demo1.controller;

import org.example.demo1.pojo.Result;
import org.example.demo1.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminDashboardController {
    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/admin/dashboard")
    public Result dashboard() {
        return Result.success(dashboardService.overview());
    }
}
