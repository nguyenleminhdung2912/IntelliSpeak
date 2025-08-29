package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.CVSubmission;
import com.gsu25se05.itellispeak.entity.Company;
import com.gsu25se05.itellispeak.entity.MemberCV;
import com.gsu25se05.itellispeak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CVSubmissionRepository extends JpaRepository<CVSubmission, Long> {

    Optional<CVSubmission> findByCompanyAndMemberCV(Company company, MemberCV memberCV);

    List<CVSubmission> findByMemberCV_User(User user);

    List<CVSubmission> findByCompany(Company company);
}
