package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.payment.CreatePaymentRequest;
import com.gsu25se05.itellispeak.dto.payment.UrlPaymentResponse;
import com.gsu25se05.itellispeak.entity.Transaction;
import com.gsu25se05.itellispeak.entity.TransactionStatus;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.entity.Package;
import com.gsu25se05.itellispeak.exception.auth.NotFoundException;
import com.gsu25se05.itellispeak.repository.PackageRepository;
import com.gsu25se05.itellispeak.repository.TransactionRepository;
import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.repository.UserRepository;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
                .description("Purchase package: " + selectedPackage.getPackageName())
                .returnUrl("https://itelli-speak-web.vercel.app/success?orderCode=" + orderCode)
                .cancelUrl("https://itelli-speak-web.vercel.app/cancel")
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
                tx.setTransactionStatus(TransactionStatus.PAID);
                transactionRepository.save(tx);

                Package purchasedPackage = tx.getAPackage();
                user.setAPackage(purchasedPackage);
                userRepository.save(user);

                return new Response<>(200, "Payment successful, package activated", "PAID");
            }

            return new Response<>(202, "Transaction not paid yet", tx.getTransactionStatus().toString());
        } catch (Exception e) {
            return new Response<>(500, "Error while checking transaction status: " + e.getMessage(), null);
        }
    }

    public Response<String> cancelPayment(Long orderCode) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Please log in", null);

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
}
