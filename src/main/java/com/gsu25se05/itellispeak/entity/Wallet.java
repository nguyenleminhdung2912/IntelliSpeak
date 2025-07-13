//package com.gsu25se05.itellispeak.entity;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.util.List;
//
//@Entity
//@Table(name = "wallet")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Wallet {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @OneToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//    @Column(name = "total", nullable = false)
//    private Double total;
//
//    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
//    private List<Transaction> transactions;
//
//
//}
