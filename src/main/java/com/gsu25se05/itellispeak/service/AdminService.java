package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.entity.PlanType;
import com.gsu25se05.itellispeak.repository.TransactionRepository;
import com.gsu25se05.itellispeak.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public AdminService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public Double getMonthlyRevenue(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        Double revenue = transactionRepository.sumAmountByCreateAtBetween(start, end);
        return revenue != null ? revenue : 0.0;
    }

    public Map<String, Long> getPlanCounts() {
        Map<String, Long> result = new HashMap<>();
        result.put("PROFESSIONAL", userRepository.countByPlanType(PlanType.PROFESSIONAL));
        result.put("BUSINESS", userRepository.countByPlanType(PlanType.BUSINESS));
        return result;
    }

    public List<Map<String, String>> getYearlyRevenueFormatted(int year) {
        List<Map<String, String>> result = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            Double amount = getMonthlyRevenue(year, month);
            String formattedAmount = String.format("%,.0f Đồng", amount);
            Map<String, String> entry = new HashMap<>();
            entry.put("Month", "Thg " + month);
            entry.put("Amount", formattedAmount);
            result.add(entry);
        }
        return result;
    }
}
