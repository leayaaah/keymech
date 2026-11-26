package vn.edu.iuh.keymech.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.keymech.dto.DashboardResponse;
import vn.edu.iuh.keymech.service.DashboardService;

@RestController
@RequiredArgsConstructor
public class AdminDashboardController {
    private final DashboardService dashboard;

    @GetMapping("/api/admin/dashboard")
    public DashboardResponse dashboard() {
        return dashboard.getSummary();
    }
}
