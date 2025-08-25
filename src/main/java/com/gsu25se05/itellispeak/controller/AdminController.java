package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.admin.CreateUserDTO;
import com.gsu25se05.itellispeak.dto.admin.UpdateUserRoleRequest;
import com.gsu25se05.itellispeak.dto.admin.UserWithPackageDTO;
import com.gsu25se05.itellispeak.dto.apackage.UpgradePackageRequest;
import com.gsu25se05.itellispeak.dto.auth.reponse.UserDTO;
import com.gsu25se05.itellispeak.dto.hr.HRAdminResponseDTO;
import com.gsu25se05.itellispeak.entity.Transaction;
import com.gsu25se05.itellispeak.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @GetMapping("/daily-revenue")
    public ResponseEntity<Response<List<Map<String, String>>>> getDailyRevenue() {
        List<Transaction> transactions = adminService.getPaidTransactions();
        Map<LocalDate, Double> dailyRevenue = transactions.stream()
                .filter(t -> t.getCreateAt() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getCreateAt().toLocalDate(),
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        List<Map<String, String>> revenue = dailyRevenue.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("Date", entry.getKey().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    map.put("Amount", String.format("%,.0f", entry.getValue()));
                    return map;
                })
                .toList();
        if (revenue.isEmpty()) {
            Map<String, String> zeroRevenue = new HashMap<>();
            zeroRevenue.put("Date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            zeroRevenue.put("Amount", "0");
            revenue = List.of(zeroRevenue);
        }
            return ResponseEntity.ok(new Response<>(200, "Daily revenue fetched", revenue));
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
        return ResponseEntity.ok(new Response<>(200, "HR has been approved successfully.", null));
    }

    @Operation(summary = "Admin từ chối HR")
    @PutMapping("/hr/{id}/reject")
    public ResponseEntity<Response<String>> rejectHR(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        adminService.rejectHR(id, reason);
        String msg = "HR has been rejected successfully";
        if (reason != null && !reason.isBlank()) {
            msg += " with reason: " + reason;
        }
        return ResponseEntity.ok(new Response<>(200, msg, null));
    }


    @Operation(summary = "Admin nâng cấp gói cho user bằng packageId")
    @PutMapping("/users/{userId}/upgrade-package")
    public ResponseEntity<Response<UserDTO>> upgradeUserPackage(
            @PathVariable Long userId,
            @RequestBody UpgradePackageRequest request
    ) {
        UserDTO data = adminService.upgradeUserPackage(userId, request.getTargetPackageId());
        return ResponseEntity.ok(new Response<>(200, "User package upgraded successfully", data));
    }

    @Operation(summary = "Admin đổi role người dùng (ví dụ: USER -> HR)")
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<Response<UserDTO>> updateUserRole(
            @PathVariable Long userId,
            @RequestBody UpdateUserRoleRequest request
    ) {
        UserDTO data = adminService.updateUserRole(userId, request.getRole());
        return ResponseEntity.ok(new Response<>(200, "User role updated successfully", data));
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
    public ResponseEntity<Response<Void>> banUser(@PathVariable Long userId) {
        adminService.banUser(userId);
        return ResponseEntity.ok(new Response<>(200, "User banned successfully", null));
    }

    @PostMapping("/unban-user/{userId}")
    @Operation(summary = "Unban a user (set isDeleted = false)")
    public ResponseEntity<Response<Void>> unbanUser(@PathVariable Long userId) {
        adminService.unbanUser(userId);
        return ResponseEntity.ok(new Response<>(200, "User unbanned successfully", null));
    }
}
