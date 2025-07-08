package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByOrderCode (Long orderCode);

}
