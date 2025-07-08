package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.payment.CreatePaymentRequest;
import com.gsu25se05.itellispeak.dto.payment.UrlPaymentResponse;
import com.gsu25se05.itellispeak.service.PaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Tạo link thanh toán (PayOS)
     */
    @PostMapping("/create")
    public Response<UrlPaymentResponse> createPayment(@RequestBody CreatePaymentRequest request) throws Exception {
        return paymentService.createPayment(request);
    }

    /**
     * Kiểm tra trạng thái đơn thanh toán (PAID / PENDING / FAILED)
     */
    @GetMapping("/check")
    public Response<String> checkPaymentStatus(@RequestParam Long orderCode) {
        return paymentService.checkTopupStatus(orderCode);
    }

    @PostMapping("/cancel/{orderCode}")
    public Response<String> cancelPayment(@PathVariable Long orderCode) {
        return paymentService.cancelPayment(orderCode);
    }
}
