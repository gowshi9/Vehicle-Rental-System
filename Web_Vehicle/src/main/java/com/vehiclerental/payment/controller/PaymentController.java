package com.vehiclerental.payment.controller;

import com.vehiclerental.payment.dto.PaymentRequest;
import com.vehiclerental.payment.dto.PaymentResponse;
import com.vehiclerental.payment.service.PaymentService;
import com.vehiclerental.payment.gateway.PayPalGateway;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PayPalGateway payPalGateway;

    @GetMapping("/checkout/{bookingId}")
    public String showCheckout(@PathVariable Long bookingId, Model model) {
        model.addAttribute("bookingId", bookingId);
        model.addAttribute("paymentRequest", new PaymentRequest());
        model.addAttribute("title", "Payment Checkout - DriveEase");
        return "payment/checkout";
    }

    @PostMapping("/process")
    @ResponseBody
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Processing payment request for booking: {}", request.getBookingId());
        
        try {
            PaymentResponse response = paymentService.processPayment(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Payment processing failed for booking {}", request.getBookingId(), e);
            
            PaymentResponse errorResponse = PaymentResponse.builder()
                    .status(com.vehiclerental.common.entity.Payment.PaymentStatus.FAILED)
                    .message("Payment processing failed: " + e.getMessage())
                    .success(false)
                    .processedAt(java.time.LocalDateTime.now())
                    .build();
                    
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/paypal/approve")
    public String handlePayPalApproval(@RequestParam String token,
                                     @RequestParam(required = false) String PayerID,
                                     @RequestParam Long booking,
                                     RedirectAttributes redirectAttributes) {
        
        log.info("Handling PayPal approval for token: {}, booking: {}", token, booking);
        
        try {
            PaymentResponse response = payPalGateway.handleApproval(token, PayerID);
            
            if (response.isSuccess()) {
                redirectAttributes.addFlashAttribute("success", "Payment completed successfully!");
                return "redirect:/customer/bookings/" + booking;
            } else {
                redirectAttributes.addFlashAttribute("error", "Payment failed: " + response.getMessage());
                return "redirect:/payment/checkout/" + booking;
            }
        } catch (Exception e) {
            log.error("PayPal approval handling failed", e);
            redirectAttributes.addFlashAttribute("error", "Payment processing failed");
            return "redirect:/payment/checkout/" + booking;
        }
    }

    @GetMapping("/paypal/cancel")
    public String handlePayPalCancel(@RequestParam Long booking, RedirectAttributes redirectAttributes) {
        log.info("PayPal payment cancelled for booking: {}", booking);
        redirectAttributes.addFlashAttribute("info", "Payment was cancelled");
        return "redirect:/payment/checkout/" + booking;
    }

    @PostMapping("/refund/{paymentId}")
    @ResponseBody
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long paymentId,
                                                       @RequestParam BigDecimal amount) {
        log.info("Processing refund for payment: {}, amount: {}", paymentId, amount);
        
        PaymentResponse response = paymentService.refundPayment(paymentId, amount);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/status/{transactionId}")
    @ResponseBody
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable String transactionId) {
        PaymentResponse response = paymentService.getPaymentStatus(transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/success")
    public String paymentSuccess(Model model) {
        model.addAttribute("title", "Payment Successful - DriveEase");
        return "payment/success";
    }

    @GetMapping("/failed")
    public String paymentFailed(Model model) {
        model.addAttribute("title", "Payment Failed - DriveEase");
        return "payment/failed";
    }
}