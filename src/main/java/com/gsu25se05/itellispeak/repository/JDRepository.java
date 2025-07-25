package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.JD;
import com.gsu25se05.itellispeak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JDRepository extends JpaRepository<JD, Long> {

    List<JD> findByUserAndIsDeletedFalse(User user);

}
