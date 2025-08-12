package com.gsu25se05.itellispeak.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne
//    @JoinColumn(name = "wallet_id", nullable = false)
//    private  Wallet wallet;

        @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private  User user;

    @ManyToOne
    @JoinColumn(name = "package_id", nullable = false)
    private  Package aPackage;

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
