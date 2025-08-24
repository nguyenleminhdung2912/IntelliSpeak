package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.payment.CreatePaymentRequest;
import com.gsu25se05.itellispeak.dto.payment.UrlPaymentResponse;
import com.gsu25se05.itellispeak.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "Tạo link thanh toán (PayOS)")
    @PostMapping("/create")
    public Response<UrlPaymentResponse> createPayment(@RequestBody CreatePaymentRequest request) throws Exception {
        return paymentService.createPayment(request);
    }

    /**
     * Kiểm tra trạng thái đơn thanh toán (PAID / PENDING / FAILED)
     */
    @Operation(summary = "Kiểm tra trạng thái đơn thanh toán (PAID / PENDING / FAILED)")
    @GetMapping("/check")
    public Response<String> checkPaymentStatus(@RequestParam Long orderCode) {
        return paymentService.checkPaymentStatus(orderCode);
    }

    @Operation(summary = "Xử lý thanh toán thành công")
    @GetMapping("/success")
    public ResponseEntity<?> successPayment(
            @RequestParam("orderCode") String orderCode,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "cancel", required = false) Boolean cancel,
            @RequestParam(value = "status", required = false) String status) {
        Response<String> response = paymentService.successPayment(orderCode);
        if (response.getCode() == 200 && "PAID".equals(response.getData())) {
            return ResponseEntity.status(HttpStatus.FOUND)
//                    .header("Location", "https://intelli-speak-web.vercel.app/payment-success")
                    .header("Location", "http://localhost:5173/payment-success")
                    .build();
        } else if ("CANCELLED".equalsIgnoreCase(status) || "EXPIRED".equalsIgnoreCase(status)) {
            return ResponseEntity.status(HttpStatus.FOUND)
//                    .header("Location", "https://intelli-speak-web.vercel.app/payment-failed")
                    .header("Location", "http://localhost:5173/payment-failed")
                    .build();
        }
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @Operation(summary = "Xử lý hủy thanh toán từ PayOS redirect")
    @GetMapping("/cancel")
    public ResponseEntity<?> handleCancelRedirect(
            @RequestParam("orderCode") String orderCode,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "cancel", required = false) Boolean cancel,
            @RequestParam(value = "status", required = false) String status) {
        try {
            Long orderCodeLong = Long.parseLong(orderCode);
            if (cancel != null && cancel || "CANCELLED".equalsIgnoreCase(status)) {
                Response<String> response = paymentService.cancelPayment(orderCodeLong);
                if (response.getCode() == 200 && "CANCELLED".equals(response.getData())) {
                    return ResponseEntity.status(HttpStatus.FOUND)
//                            .header("Location", "https://intelli-speak-web.vercel.app/payment-failed")
                            .header("Location", "http://localhost:5173/payment-failed")
                            .build();
                }
                return ResponseEntity.status(response.getCode()).body(response);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response<>(400, "Invalid cancel request", null));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response<>(400, "Invalid order code format", null));
        }
    }
}
