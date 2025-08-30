package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CVSubmissionRepository extends JpaRepository<CVSubmission, Long> {

    Optional<CVSubmission> findByCompanyJDAndMemberCV(CompanyJD companyJD, MemberCV memberCV);

    List<CVSubmission> findByMemberCV_UserOrderBySubmittedAtDesc(User user);

    List<CVSubmission> findByCompanyOrderBySubmittedAtDesc(Company company);
}
