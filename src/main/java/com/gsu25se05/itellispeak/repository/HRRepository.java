package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.HR;
import com.gsu25se05.itellispeak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HRRepository extends JpaRepository<HR, Long> {
    Optional<HR> findByUser(User user);
}
