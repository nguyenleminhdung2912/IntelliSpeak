package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.PlanType;
import com.gsu25se05.itellispeak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUserId(UUID uuid);
    long countByPlanType(PlanType planType);
}
