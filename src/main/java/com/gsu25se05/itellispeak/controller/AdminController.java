package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.admin.CreateUserDTO;
import com.gsu25se05.itellispeak.dto.admin.UserWithPackageDTO;
import com.gsu25se05.itellispeak.dto.auth.reponse.UserDTO;
import com.gsu25se05.itellispeak.dto.hr.HRAdminResponseDTO;
import com.gsu25se05.itellispeak.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @Operation(summary = "Tất cả Users")
    @GetMapping("/all-users")
    public Response<List<UserDTO>> getAllUsers() {
        List<UserDTO> data = adminService.getAllUsers();
        return new Response<>(200, "Fetched all users successfully", data);
    }

    @Operation(summary = "Admin xem thông tin của HR để duyệt hay từ chối")
    @GetMapping("/hr-applications")
    public Response<List<HRAdminResponseDTO>> getHRApplications() {
        List<HRAdminResponseDTO> data = adminService.getAllHRApplications();
        return new Response<>(200, "Fetched HR applications successfully", data);
    }

    @Operation(summary = "Admin duyệt HR")
    @PutMapping("/hr/{id}/approve")
    public ResponseEntity<Response<String>> approveHR(@PathVariable Long id) {
        adminService.approveHR(id);
        return ResponseEntity.ok(new Response<>(200, "HR approved successfully", null));
    }

    @Operation(summary = "Admin từ chối HR")
    @PutMapping("/hr/{id}/reject")
    public ResponseEntity<Response<String>> rejectHR(@PathVariable Long id) {
        adminService.rejectHR(id);
        return ResponseEntity.ok(new Response<>(200, "HR rejected successfully", null));
    }

    @Operation(summary = "Get all users with their package info")
    @GetMapping("/users-with-package")
    public ResponseEntity<Response<List<UserWithPackageDTO>>> getAllUsersWithPackage() {
        List<UserWithPackageDTO> data = adminService.getAllUsersWithPackage();
        return ResponseEntity.ok(new Response<>(200, "Fetched users with package", data));
    }

    @Operation(summary = "Đếm người dùng đăng kí các gói")
    @GetMapping("/package-stats")
    public ResponseEntity<Response<List<Map<String, Object>>>> getPackageStats(
            @RequestParam int year
    ) {
        List<Map<String, Object>> revenue = adminService.getPackageSubscriptionStats(year);
        return ResponseEntity.ok(new Response<>(200, "Package subscription stats", revenue));
    }

    @Operation(summary = "Admin tạo người dùng")
    @PostMapping("/create-user")
    public ResponseEntity<Response<UserDTO>> createUser(@RequestBody CreateUserDTO dto) {
        UserDTO data = adminService.createUser(dto);
        return ResponseEntity.ok(new Response<>(200, "User created successfully", data));
    }

    @PostMapping("/ban-user/{userId}")
    @Operation(summary = "Ban a user (set isDeleted = true)")
    public ResponseEntity<Response<Void>> banUser(@PathVariable UUID userId) {
        adminService.banUser(userId);
        return ResponseEntity.ok(new Response<>(200, "User banned successfully", null));
    }
}
