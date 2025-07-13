package com.gsu25se05.itellispeak.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne
//    @JoinColumn(name = "wallet_id", nullable = false)
//    private  Wallet wallet;

    @Column(name = "order_code")
    private Long orderCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionStatus transactionStatus;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "create_at")
    private LocalDateTime createAt;
}
