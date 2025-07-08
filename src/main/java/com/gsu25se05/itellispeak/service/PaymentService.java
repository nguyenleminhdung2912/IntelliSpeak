package com.gsu25se05.itellispeak.service;


import com.gsu25se05.itellispeak.config.PayOSConfig;
import com.gsu25se05.itellispeak.dto.payment.CreatePaymentRequest;
import com.gsu25se05.itellispeak.dto.payment.UrlPaymentResponse;
import com.gsu25se05.itellispeak.entity.Transaction;
import com.gsu25se05.itellispeak.entity.TransactionStatus;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.entity.Wallet;
import com.gsu25se05.itellispeak.exception.auth.NotFoundException;
import com.gsu25se05.itellispeak.repository.TransactionRepository;
import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.repository.WalletRepository;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final WebClient.Builder webClientBuilder;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final AccountUtils accountUtils;

    @Value("${PAYOS_CLIENT_ID}")
    private String clientId;

    @Value("${PAYOS_API_KEY}")
    private String apiKey;

    @Value("${PAYOS_CHECKSUM_KEY}")
    private String checksumKey;


    public Response<UrlPaymentResponse> createPayment(CreatePaymentRequest request) throws Exception {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Vui lòng đăng nhập", null);

        Long orderCode = System.currentTimeMillis();;

        // Create wallet & transaction như bạn đã làm
        Wallet wallet = walletRepository.findByUser(user)
                .orElseGet(() -> walletRepository.save(Wallet.builder().user(user).total(0D).build()));

        Transaction tx = new Transaction();
        tx.setOrderCode(orderCode);
        tx.setAmount(request.getAmount());
        tx.setWallet(wallet);
        tx.setTransactionStatus(TransactionStatus.PENDING);
        tx.setCreateAt(LocalDateTime.now());
        transactionRepository.save(tx);

        // Sử dụng SDK PayOS
        PayOS payOS = new PayOS(clientId, apiKey, checksumKey);

        ItemData item = ItemData.builder()
                .name("Nạp tiền vào ví")
                .quantity(1)
                .price(request.getAmount().intValue()) // PayOS yêu cầu int
                .build();

        PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount(request.getAmount().intValue())
                .description("Nạp tiền vào ví")
                .returnUrl("https://itelli-speak-web.vercel.app/success?orderCode=" + orderCode)
                .cancelUrl("https://itelli-speak-web.vercel.app/cancel")
                .item(item)
                .build();

        CheckoutResponseData result = payOS.createPaymentLink(paymentData);
        String checkoutUrl = result.getCheckoutUrl();

        return new Response<>(200, "Tạo đơn hàng thành công", new UrlPaymentResponse(checkoutUrl, orderCode));
    }

    public Response<String> checkTopupStatus(Long orderCode) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Vui lòng đăng nhập", null);

        Transaction tx = transactionRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy giao dịch"));

        if (tx.getTransactionStatus() == TransactionStatus.PAID) {
            return new Response<>(200, "Giao dịch đã thanh toán", "PAID");
        }

        PayOS payOS = new PayOS(clientId, apiKey, checksumKey);
        try {
            var paymentLinkData = payOS.getPaymentLinkInformation(orderCode);
            String status = paymentLinkData.getStatus();

            if ("PAID".equalsIgnoreCase(status)) {
                tx.setTransactionStatus(TransactionStatus.PAID);
                transactionRepository.save(tx);

                Wallet wallet = walletRepository.findByUser(user)
                        .orElse(Wallet.builder().user(user).total(0D).build());
                wallet.setTotal(wallet.getTotal() + tx.getAmount());
                walletRepository.save(wallet);

                return new Response<>(200, "Đã cập nhật ví thành công", "PAID");
            }

            return new Response<>(202, "Giao dịch chưa thanh toán", status);
        } catch (Exception e) {
            return new Response<>(500, "Lỗi khi kiểm tra trạng thái giao dịch: " + e.getMessage(), null);
        }
    }


    public Response<String> cancelPayment(Long orderCode) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Vui lòng đăng nhập", null);

        Transaction tx = transactionRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy giao dịch"));

        if (tx.getTransactionStatus() == TransactionStatus.PAID) {
            return new Response<>(400, "Giao dịch đã được thanh toán, không thể hủy", null);
        }

        try {
            PayOS payOS = new PayOS(clientId, apiKey, checksumKey);
            payOS.cancelPaymentLink(orderCode, "Hủy đơn hàng bởi người dùng");

            tx.setTransactionStatus(TransactionStatus.CANCELLED);
            transactionRepository.save(tx);

            return new Response<>(200, "Hủy giao dịch thành công", "CANCELLED");
        } catch (Exception e) {
            return new Response<>(500, "Lỗi khi hủy đơn hàng: " + e.getMessage(), null);
        }
    }



}
