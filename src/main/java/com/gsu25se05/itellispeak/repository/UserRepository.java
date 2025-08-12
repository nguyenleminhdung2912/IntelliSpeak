package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    @Query("""
                SELECT COUNT(u) 
                FROM User u 
                WHERE u.aPackage.packageId = :packageId 
                  AND u.createAt BETWEEN :start AND :end
            """)
    Long countByPackageIdAndCreateAtBetween(@Param("packageId") Long packageId,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(u) FROM User u WHERE u.aPackage = :aPackage AND u.isDeleted = false")
    long countUsersByPackage(@Param("aPackage") com.gsu25se05.itellispeak.entity.Package aPackage);
}