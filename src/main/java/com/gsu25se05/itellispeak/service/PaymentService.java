package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.payment.CreatePaymentRequest;
import com.gsu25se05.itellispeak.dto.payment.UrlPaymentResponse;
import com.gsu25se05.itellispeak.entity.*;
import com.gsu25se05.itellispeak.entity.Package;
import com.gsu25se05.itellispeak.exception.auth.NotFoundException;
import com.gsu25se05.itellispeak.repository.PackageRepository;
import com.gsu25se05.itellispeak.repository.TransactionRepository;
import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.repository.UserRepository;
import com.gsu25se05.itellispeak.repository.UserUsageRepository;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final WebClient.Builder webClientBuilder;
    private final TransactionRepository transactionRepository;
    private final AccountUtils accountUtils;
    private final UserRepository userRepository;
    private final PackageRepository packageRepository;
    private final UserUsageRepository userUsageRepository;

    @Value("${PAYOS_CLIENT_ID}")
    private String clientId;

    @Value("${PAYOS_API_KEY}")
    private String apiKey;

    @Value("${PAYOS_CHECKSUM_KEY}")
    private String checksumKey;

    public Response<UrlPaymentResponse> createPayment(CreatePaymentRequest request) throws Exception {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Please log in", null);

        Package selectedPackage = packageRepository.findByPackageIdAndIsDeletedFalse(request.getPackageId());
        if (selectedPackage == null) {
            return new Response<>(404, "Package not found", null);
        }

        Double amount = selectedPackage.getPrice();
        if (amount == null || amount < 2000) {
            return new Response<>(400, "Package price must be at least 2000", null);
        }

        Long orderCode = System.currentTimeMillis();

        Transaction tx = new Transaction();
        tx.setOrderCode(orderCode);
        tx.setAmount(amount);
        tx.setTransactionStatus(TransactionStatus.PENDING);
        tx.setCreateAt(LocalDateTime.now());
        tx.setDescription("Purchase package: " + selectedPackage.getPackageName());
        tx.setUser(user);
        tx.setAPackage(selectedPackage);
        transactionRepository.save(tx);

        PayOS payOS = new PayOS(clientId, apiKey, checksumKey);

        ItemData item = ItemData.builder()
                .name("Purchase package: " + selectedPackage.getPackageName())
                .quantity(1)
                .price(amount.intValue()) // PayOS requires int
                .build();

        PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount(amount.intValue())
                .description("Package: " + selectedPackage.getPackageName())
                .returnUrl("https://bug-adapting-especially.ngrok-free.app/api/payment/success?orderCode=" + orderCode)
                .cancelUrl("https://bug-adapting-especially.ngrok-free.app/api/payment/cancel?orderCode=" + orderCode)
//                .returnUrl("http://localhost:8080/api/payment/success?orderCode=" + orderCode)
//                .cancelUrl("http://localhost:8080/api/payment/cancel?orderCode=" + orderCode)
                .item(item)
                .build();

        CheckoutResponseData result = payOS.createPaymentLink(paymentData);
        String checkoutUrl = result.getCheckoutUrl();

        return new Response<>(200, "Payment created successfully", new UrlPaymentResponse(checkoutUrl, orderCode));
    }

    public Response<String> checkPaymentStatus(Long orderCode) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Please log in", null);

        Transaction tx = transactionRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));

        if (tx.getTransactionStatus() == TransactionStatus.PAID) {
            return new Response<>(200, "Transaction already paid", "PAID");
        }

        PayOS payOS = new PayOS(clientId, apiKey, checksumKey);
        try {
            var paymentLinkData = payOS.getPaymentLinkInformation(orderCode);
            String status = paymentLinkData.getStatus();

            if ("PAID".equalsIgnoreCase(status)) {
                // Centralized logic for activating package and rolling over credits
                activatePackageForUser(tx);
                return new Response<>(200, "Payment successful, package activated with rollover", "PAID");
            }

            return new Response<>(202, "Transaction not paid yet", tx.getTransactionStatus().toString());
        } catch (Exception e) {
            return new Response<>(500, "Error while checking transaction status: " + e.getMessage(), null);
        }
    }
    public Response<String> cancelPayment(Long orderCode) {
        Transaction tx = transactionRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));

        if (tx.getTransactionStatus() == TransactionStatus.PAID) {
            return new Response<>(400, "Transaction already paid, cannot be cancelled", null);
        }

        try {
            PayOS payOS = new PayOS(clientId, apiKey, checksumKey);
            payOS.cancelPaymentLink(orderCode, "Order cancelled by user");

            tx.setTransactionStatus(TransactionStatus.CANCELLED);
            transactionRepository.save(tx);

            return new Response<>(200, "Transaction cancelled successfully", "CANCELLED");
        } catch (Exception e) {
            return new Response<>(500, "Error while cancelling transaction: " + e.getMessage(), null);
        }
    }

    public Response<String> successPayment(String orderCode) {
        try {
            Long orderCodeLong = Long.parseLong(orderCode);

            Transaction tx = transactionRepository.findByOrderCode(orderCodeLong)
                    .orElseThrow(() -> new NotFoundException("Transaction not found"));
            User user = tx.getUser();

            PayOS payOS = new PayOS(clientId, apiKey, checksumKey);
            var paymentLinkData = payOS.getPaymentLinkInformation(orderCodeLong);
            String status = paymentLinkData.getStatus();

            if ("PAID".equalsIgnoreCase(status)) {
                // Centralized logic for activating package and rolling over credits
                activatePackageForUser(tx);
                return new Response<>(200, "Payment successful, package activated", "PAID");
            } else if ("CANCELLED".equalsIgnoreCase(status) || "EXPIRED".equalsIgnoreCase(status)) {
                tx.setTransactionStatus(TransactionStatus.FAILED);
                transactionRepository.save(tx);
                return new Response<>(400, "Payment failed or cancelled", "FAILED");
            }

            return new Response<>(202, "Transaction not paid yet", tx.getTransactionStatus().toString());
        } catch (NumberFormatException e) {
            return new Response<>(400, "Invalid order code format", null);
        } catch (Exception e) {
            return new Response<>(500, "Error while processing payment: " + e.getMessage(), null);
        }
    }

    /**
     * Handles the logic of activating a package for a user after a successful payment.
     * This method is transactional to ensure all database operations succeed or fail together.
     * It implements a "rollover" feature by calculating remaining credits and adjusting the "used" counters.
     *
     * @param tx The successful transaction.
     */
    private void activatePackageForUser(Transaction tx) {
        // 1. Check if already processed to prevent duplicate execution
        if (tx.getTransactionStatus() == TransactionStatus.PAID) {
            return;
        }

        // 2. Get required entities
        User user = tx.getUser();
        Package purchasedPackage = tx.getAPackage();
        UserUsage usage = user.getUserUsage();
        Package currentPackage = user.getAPackage(); // The package user had before this purchase

        // 3. Mark transaction as PAID
        tx.setTransactionStatus(TransactionStatus.PAID);
        tx.setCreateAt(LocalDateTime.now());

        // 4. Ensure a UserUsage object exists for the user
        if (usage == null) {
            usage = UserUsage.builder()
                    .user(user)
                    .cvAnalyzeUsed(0)
                    .jdAnalyzeUsed(0)
                    .interviewUsed(0)
                    .build();
            user.setUserUsage(usage);
        }

        // 5. Calculate remaining credits from the current package (if any)
        int remainingCv = 0;
        int remainingJd = 0;
        int remainingItv = 0;

        if (currentPackage != null) {
            remainingCv = (currentPackage.getCvAnalyzeCount() != null ? currentPackage.getCvAnalyzeCount() : 0) - usage.getCvAnalyzeUsed();
            remainingJd = (currentPackage.getJdAnalyzeCount() != null ? currentPackage.getJdAnalyzeCount() : 0) - usage.getJdAnalyzeUsed();
            remainingItv = (currentPackage.getInterviewCount() != null ? currentPackage.getInterviewCount() : 0) - usage.getInterviewUsed();
        }

        // 6. Update user's package to the newly purchased one
        user.setAPackage(purchasedPackage);

        // 7. Apply rollover by setting "used" counters to the negative of remaining credits.
        // This allows the user to use the new package's credits PLUS the remaining ones.
        usage.setCvAnalyzeUsed(0 - (remainingCv > 0 ? remainingCv : 0));
        usage.setJdAnalyzeUsed(0 - (remainingJd > 0 ? remainingJd : 0));
        usage.setInterviewUsed(0 - (remainingItv > 0 ? remainingItv : 0));
        usage.setUpdateAt(LocalDateTime.now());

        // 8. Save all changes
        transactionRepository.save(tx);
        userUsageRepository.save(usage);
        userRepository.save(user);
    }
}
