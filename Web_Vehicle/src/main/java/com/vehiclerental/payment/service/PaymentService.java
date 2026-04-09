package com.vehiclerental.payment.service;

import com.vehiclerental.common.entity.*;
import com.vehiclerental.common.repository.*;
import com.vehiclerental.payment.dto.PaymentRequest;
import com.vehiclerental.payment.dto.PaymentResponse;
import com.vehiclerental.payment.gateway.*;
import com.vehiclerental.delivery.service.DeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private List<PaymentGateway> paymentGateways;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DeliveryRepository deliveryRepository;
    
    @Autowired
    private DeliveryService deliveryService;

    @Transactional(rollbackFor = Exception.class)
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for booking: {}", request.getBookingId());

        // Validate booking
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getPaymentStatus() == Booking.PaymentStatus.PAID) {
            throw new RuntimeException("Booking already paid");
        }
        
        // Check if successful payment already exists for this booking
        if (paymentRepository.findByBooking(booking).stream().anyMatch(p -> 
            p.getStatus() == Payment.PaymentStatus.COMPLETED)) {
            throw new RuntimeException("Payment already completed for this booking");
        }
        
        // Find existing payment or create new one
        Payment payment = paymentRepository.findByBooking(booking).stream()
            .filter(p -> p.getStatus() == Payment.PaymentStatus.PENDING || p.getStatus() == Payment.PaymentStatus.FAILED)
            .findFirst()
            .orElse(null);
            
        if (payment == null) {
            // Create new payment record
            payment = new Payment();
            payment.setBooking(booking);
            payment.setTransactionId(UUID.randomUUID().toString());
            payment = paymentRepository.save(payment);
        }
        
        // Update payment details
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setGatewayType(request.getGatewayType());
        payment.setAmount(request.getAmount());
        payment.setCurrencyCode(request.getCurrencyCode());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setFailureReason(null);
        payment.setGatewayResponse(null);
        payment.setGatewayTransactionId(null);
        payment.setProcessedAt(null);
        
        payment = paymentRepository.save(payment);

        try {
            log.info("Processing payment through gateway for booking {}", booking.getId());
            
            // Process payment through gateway
            PaymentGateway gateway = getGateway(request.getGatewayType());
            PaymentResponse response = gateway.processPayment(request);
            
            log.info("Gateway response: success={}, status={}, message={}", 
                response.isSuccess(), response.getStatus(), response.getMessage());

            // Update payment record
            payment.setStatus(response.getStatus());
            payment.setGatewayTransactionId(response.getGatewayTransactionId());
            payment.setGatewayResponse(response.getMessage());
            payment.setProcessedAt(response.getProcessedAt());

            if (!response.isSuccess()) {
                payment.setFailureReason(response.getMessage());
                payment.setStatus(Payment.PaymentStatus.FAILED);
                log.warn("Payment failed for booking {}: {}", booking.getId(), response.getMessage());
            }

            paymentRepository.save(payment);
            log.info("Payment record updated with status: {}", payment.getStatus());

            // Update booking status if payment successful
            if (response.isSuccess() && response.getStatus() == Payment.PaymentStatus.COMPLETED) {
                log.info("Payment successful, updating booking {} status to PAID", booking.getId());
                booking.setPaymentStatus(Booking.PaymentStatus.PAID);
                booking.setStatus(Booking.BookingStatus.CONFIRMED);
                bookingRepository.save(booking);
                log.info("Booking {} status updated to PAID and CONFIRMED", booking.getId());
            } else {
                log.info("Payment not completed - success: {}, status: {}", response.isSuccess(), response.getStatus());
            }

            // Save card if requested
            if (request.isSaveCard() && response.isSuccess() && request.getCardNumber() != null) {
                try {
                    savePaymentCard(booking.getCustomer(), request);
                } catch (Exception e) {
                    log.warn("Failed to save payment card, but payment was successful", e);
                }
            }

            // Build response
            return PaymentResponse.builder()
                    .paymentId(payment.getId())
                    .transactionId(payment.getTransactionId())
                    .status(response.getStatus())
                    .amount(response.getAmount())
                    .currencyCode(response.getCurrencyCode())
                    .gatewayTransactionId(response.getGatewayTransactionId())
                    .redirectUrl(response.getRedirectUrl())
                    .message(response.getMessage())
                    .success(response.isSuccess())
                    .processedAt(response.getProcessedAt())
                    .build();

        } catch (Exception e) {
            log.error("Payment processing failed for booking {}", request.getBookingId(), e);
            
            // Update payment status to failed
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            paymentRepository.save(payment);
            
            throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public PaymentResponse refundPayment(Long paymentId, java.math.BigDecimal amount) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        PaymentGateway gateway = getGateway(payment.getGatewayType());
        PaymentResponse response = gateway.refundPayment(payment.getGatewayTransactionId(), amount);

        if (response.isSuccess()) {
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            payment.getBooking().setPaymentStatus(Booking.PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            bookingRepository.save(payment.getBooking());
        }

        return response;
    }

    public PaymentResponse getPaymentStatus(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        PaymentGateway gateway = getGateway(payment.getGatewayType());
        return gateway.getPaymentStatus(payment.getGatewayTransactionId());
    }

    private PaymentGateway getGateway(Payment.GatewayType gatewayType) {
        return paymentGateways.stream()
                .filter(gateway -> gateway.isSupported(gatewayType))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unsupported gateway: " + gatewayType));
    }

    private void savePaymentCard(User user, PaymentRequest request) {
        try {
            PaymentCard card = new PaymentCard();
            card.setUser(user);
            card.setCardToken(tokenizeCard(request.getCardNumber()));
            card.setLastFourDigits(request.getCardNumber().substring(request.getCardNumber().length() - 4));
            card.setCardType(determineCardType(request.getCardNumber()));
            card.setExpiryMonth(Integer.parseInt(request.getExpiryMonth()));
            card.setExpiryYear(Integer.parseInt(request.getExpiryYear()));
            card.setCardholderName(request.getCardHolderName());

            paymentCardRepository.save(card);
            log.info("Payment card saved for user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to save payment card", e);
        }
    }

    private String tokenizeCard(String cardNumber) {
        // Simple tokenization - in production use proper tokenization service
        return "tok_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
    }

    private String determineCardType(String cardNumber) {
        if (cardNumber.startsWith("4")) return "VISA";
        if (cardNumber.startsWith("5") || cardNumber.startsWith("2")) return "MASTERCARD";
        if (cardNumber.startsWith("3")) return "AMEX";
        return "UNKNOWN";
    }
    

}