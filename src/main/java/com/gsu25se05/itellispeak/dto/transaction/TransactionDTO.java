package com.gsu25se05.itellispeak.dto.transaction;

import com.gsu25se05.itellispeak.dto.auth.reponse.UserDTO;
import com.gsu25se05.itellispeak.entity.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {
    private Long id;
    private Long orderCode;
    private TransactionStatus transactionStatus;
    private Double amount;
    private String description;
    private LocalDateTime createAt;

    private PackageBriefDTO aPackage;

    private UserDTO user;
}
