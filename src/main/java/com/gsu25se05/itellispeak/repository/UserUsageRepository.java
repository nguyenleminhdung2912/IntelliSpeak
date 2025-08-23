package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.entity.UserUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserUsageRepository extends JpaRepository<UserUsage, Long> {
    boolean existsByUser(User user);

    Optional<UserUsage> findByUser(User user);

}
