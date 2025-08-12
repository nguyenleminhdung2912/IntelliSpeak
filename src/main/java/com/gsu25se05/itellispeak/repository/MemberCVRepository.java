package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.MemberCV;
import com.gsu25se05.itellispeak.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberCVRepository extends JpaRepository<MemberCV, Long> {
    @Modifying
    @Transactional
    @Query("UPDATE MemberCV m SET m.isActive = false WHERE m.user = :user AND m.isActive = true")
    void deactivateOldCVsByUser(@Param("user") User user);

    Optional<MemberCV> findByUserAndIsActiveTrue(User user);

    List<MemberCV> findByUserUserIdAndIsDeletedFalse(UUID userId);
}
