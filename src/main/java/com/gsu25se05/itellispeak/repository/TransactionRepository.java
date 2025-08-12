package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByOrderCode (Long orderCode);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.createAt BETWEEN :start AND :end AND t.transactionStatus = com.gsu25se05.itellispeak.entity.TransactionStatus.PAID")
    Double sumAmountByCreateAtBetween(LocalDateTime start, LocalDateTime end);

}
