package com.gsu25se05.itellispeak.service;


import com.gsu25se05.itellispeak.dto.payment.CreatePaymentRequest;
import com.gsu25se05.itellispeak.dto.payment.UrlPaymentResponse;
import com.gsu25se05.itellispeak.entity.Transaction;
import com.gsu25se05.itellispeak.entity.TransactionStatus;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.entity.Package;
//import com.gsu25se05.itellispeak.entity.Wallet;
import com.gsu25se05.itellispeak.exception.auth.NotFoundException;
import com.gsu25se05.itellispeak.repository.PackageRepository;
import com.gsu25se05.itellispeak.repository.TransactionRepository;
import com.gsu25se05.itellispeak.dto.Response;
//import com.gsu25se05.itellispeak.repository.WalletRepository;
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
//    private final WalletRepository walletRepository;
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
        if (user == null) return new Response<>(401, "Vui lòng đăng nhập", null);

        Package selectedPackage = packageRepository.findByPackageIdAndIsDeletedFalse(request.getPackageId());
        if (selectedPackage == null) {
            return new Response<>(404, "Không tìm thấy gói", null);
        }

        Double amount = selectedPackage.getPrice();
        if (amount == null || amount < 2000) {
            return new Response<>(400, "Giá gói phải từ 2000 trở lên", null);
        }
            Long orderCode = System.currentTimeMillis();

//        // Create wallet & transaction như bạn đã làm
//        Wallet wallet = walletRepository.findByUser(user)
//                .orElseGet(() -> walletRepository.save(Wallet.builder().user(user).total(0D).build()));


            Transaction tx = new Transaction();
            tx.setOrderCode(orderCode);
            tx.setAmount(amount);
            tx.setTransactionStatus(TransactionStatus.PENDING);
            tx.setCreateAt(LocalDateTime.now());
            tx.setDescription("Đăng ký gói: " + selectedPackage.getPackageName());
            tx.setUser(user);
            tx.setAPackage(selectedPackage);
            transactionRepository.save(tx);

            PayOS payOS = new PayOS(clientId, apiKey, checksumKey);

            ItemData item = ItemData.builder()
                    .name("Đăng ký gói: " + selectedPackage.getPackageName())
                    .quantity(1)
                    .price(amount.intValue()) // PayOS yêu cầu int
                    .build();

            PaymentData paymentData = PaymentData.builder()
                    .orderCode(orderCode)
                    .amount(amount.intValue())
                    .description("Đăng ký gói: " + selectedPackage.getPackageName())
                    .returnUrl("https://itelli-speak-web.vercel.app/success?orderCode=" + orderCode)
                    .cancelUrl("https://itelli-speak-web.vercel.app/cancel")
                    .item(item)
                    .build();

            CheckoutResponseData result = payOS.createPaymentLink(paymentData);
            String checkoutUrl = result.getCheckoutUrl();

            return new Response<>(200, "Tạo thanh toán thành công", new UrlPaymentResponse(checkoutUrl, orderCode));
        }


    public Response<String> checkPaymentStatus(Long orderCode) {
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

                Package purchasedPackage = tx.getAPackage();
                user.setAPackage(purchasedPackage);
                userRepository.save(user);

                return new Response<>(200, "Thanh toán thành công, đã kích hoạt gói", "PAID");
            }

            return new Response<>(202, "Giao dịch chưa thanh toán", tx.getTransactionStatus().toString());
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
