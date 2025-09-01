package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByCompanyIdAndIsDeletedFalse(Long id);

    boolean existsByShortNameIgnoreCase(String shortName);

    boolean existsByShortNameIgnoreCaseAndCompanyIdNot(String shortName, Long excludeId);
}
