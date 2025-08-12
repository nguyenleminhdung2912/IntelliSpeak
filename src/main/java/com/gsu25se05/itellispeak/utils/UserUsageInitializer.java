package com.gsu25se05.itellispeak.utils;

import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.entity.UserUsage;
import com.gsu25se05.itellispeak.repository.UserRepository;
import com.gsu25se05.itellispeak.repository.UserUsageRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class UserUsageInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final UserUsageRepository userUsageRepository;

    public UserUsageInitializer(UserRepository userRepository, UserUsageRepository userUsageRepository) {
        this.userRepository = userRepository;
        this.userUsageRepository = userUsageRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            boolean exists = userUsageRepository.existsByUser(user);
            if (!exists) {
                UserUsage usage = new UserUsage();
                usage.setUser(user);
                usage.setCvAnalyzeUsed(0);
                usage.setJdAnalyzeUsed(0);
                usage.setInterviewUsed(0);
                usage.setUpdateAt(LocalDateTime.now());
                userUsageRepository.save(usage);
            }
        }
    }
}
