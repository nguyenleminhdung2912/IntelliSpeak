package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.JD;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JDRepository extends JpaRepository<JD, Long> {
}
