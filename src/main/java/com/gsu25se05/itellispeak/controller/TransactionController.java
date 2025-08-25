package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.transaction.TransactionDTO;
import com.gsu25se05.itellispeak.entity.Transaction;
import com.gsu25se05.itellispeak.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/transaction")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class TransactionController {

    @Autowired
    TransactionService transactionService;

    @Operation(summary = "Lấy tất cả transactions")
    @GetMapping("/transactions")
    public ResponseEntity<Response<List<TransactionDTO>>> getAllTransactions() {
        List<TransactionDTO> data = transactionService.getAllTransactionDetails();
        return ResponseEntity.ok(new Response<>(200, "Fetched all transactions successfully", data));
    }
}
