package com.driftstay.payment.service;

import com.driftstay.booking.entity.Payment;
import com.driftstay.common.enums.TransactionStatus;
import com.driftstay.common.exception.ResourceNotFoundException;
import com.driftstay.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment processPayment(Payment payment) {
        payment.setStatus(TransactionStatus.PENDING);
        Payment saved = paymentRepository.save(payment);
        log.info("Payment initiated: {} for booking: {}", saved.getId(), payment.getBooking().getId());
        return saved;
    }

    @Transactional
    public Payment confirmPayment(String providerPaymentId) {
        Payment payment = paymentRepository.findByProviderPaymentId(providerPaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + providerPaymentId));
        payment.setStatus(TransactionStatus.SUCCESS);
        Payment saved = paymentRepository.save(payment);
        log.info("Payment confirmed: {}", providerPaymentId);
        return saved;
    }

    @Transactional
    public Payment failPayment(String providerPaymentId, String reason) {
        Payment payment = paymentRepository.findByProviderPaymentId(providerPaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + providerPaymentId));
        payment.setStatus(TransactionStatus.FAILED);
        Payment saved = paymentRepository.save(payment);
        log.warn("Payment failed: {} - reason: {}", providerPaymentId, reason);
        return saved;
    }

    public Payment getPaymentByProviderPaymentId(String providerPaymentId) {
        return paymentRepository.findByProviderPaymentId(providerPaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + providerPaymentId));
    }
}
