package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.CompanyJDEvaluate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyJDEvaluateRepository extends JpaRepository<CompanyJDEvaluate, Long> {
}
