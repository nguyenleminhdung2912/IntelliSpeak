package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.service.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/monthly-revenue")
    public ResponseEntity<Response<Double>> getMonthlyRevenue(
            @RequestParam int year,
            @RequestParam int month) {
        Double revenue = adminService.getMonthlyRevenue(year, month);
        return ResponseEntity.ok(new Response<>(200, "Monthly revenue fetched", revenue));
    }

    @GetMapping("/plan-counts")
    public ResponseEntity<Response<Map<String, Long>>> getPlanCounts() {
        Map<String, Long> counts = adminService.getPlanCounts();
        return ResponseEntity.ok(new Response<>(200, "Plan counts fetched", counts));
    }

    @GetMapping("/yearly-revenue")
    public ResponseEntity<Response<List<Map<String, String>>>> getYearlyRevenue(@RequestParam int year) {
        List<Map<String, String>> revenue = adminService.getYearlyRevenueFormatted(year);
        return ResponseEntity.ok(new Response<>(200, "Yearly revenue fetched", revenue));
    }


}
