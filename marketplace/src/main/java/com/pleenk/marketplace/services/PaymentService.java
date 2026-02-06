package com.pleenk.marketplace.services;

import com.pleenk.marketplace.entities.Payment;
import com.pleenk.marketplace.entities.PaymentStatus;
import com.pleenk.marketplace.entities.Product;
import com.pleenk.marketplace.exceptions.NotEnoughStockException;
import com.pleenk.marketplace.exceptions.PaymentException;
import com.pleenk.marketplace.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ProductService productService;
    private final PleenkService pleenkService;

    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentException("Paiement " + id + " introuvable"));
    }

    @Transactional
    public Payment createPayment(Long productId, Integer quantity) {
        log.info("Création paiement - produit: {}, qté: {}", productId, quantity);

        Product product = productService.getProductById(productId);

        // TODO: réserver le stock pour éviter des pb si deux commandes en même temps
        productService.checkStock(product, quantity);

        BigDecimal amount = product.getPrice().multiply(BigDecimal.valueOf(quantity));

        Map<String, String> pleenkData;
        try {
            pleenkData = pleenkService.createPaymentLink(amount, product.getName());
        } catch (Exception e) {
            log.error("Échec génération lien Pleenk pour produit {}", productId, e);
            throw new PaymentException("Impossible de créer le lien Pleenk", e);
        }

        Payment payment = paymentRepository.save(Payment.builder()
                .productId(productId)
                .quantity(quantity)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .pleenkTransactionRef(pleenkData.get("transactionRef"))
                .pleenkPaymentUrl(pleenkData.get("paymentUrl"))
                .build());

        log.info("Payment {} créé", payment.getId());
        return payment;
    }

    @Transactional
    public void updatePaymentStatus(String pleenkTransactionRef, PaymentStatus newStatus) {
        log.info("Update statut {} -> {}", pleenkTransactionRef, newStatus);

        Payment payment = paymentRepository.findByPleenkTransactionRef(pleenkTransactionRef)
                .orElseThrow(() -> new PaymentException("Paiement Pleenk introuvable: " + pleenkTransactionRef));

        PaymentStatus oldStatus = payment.getStatus();

        if (isFinal(oldStatus)) {
            log.warn("Payment {} déjà final ({}), ignoré", pleenkTransactionRef, oldStatus);
            return;
        }

        payment.setStatus(newStatus);
        paymentRepository.save(payment);
        log.info("Payment {} - {} -> {}", payment.getId(), oldStatus, newStatus);

        if (newStatus == PaymentStatus.SUCCESS) {
            try {
                productService.decrementProductQuantity(payment.getProductId(), payment.getQuantity());
            } catch (Exception e) {
                log.error("Payment {} - erreur décrémentation stock", payment.getId(), e);
                // TODO: alerte
            }
        }

        if (newStatus == PaymentStatus.CANCELLED) {
            // TODO: libérer réserve stock
        }
    }

    private boolean isFinal(PaymentStatus status) {
        return status == PaymentStatus.SUCCESS || status == PaymentStatus.REFUNDED;
    }

    public PaymentStatus mapStatus(String status) {
        if (status == null || status.isBlank()) {
            log.warn("Statut Pleenk vide, traité comme PENDING");
            return PaymentStatus.PENDING;
        }

        return switch (status.toUpperCase()) {
            case "WAIT_FOR_ACCEPT" -> PaymentStatus.PENDING;
            case "CONFIRMED" -> PaymentStatus.SUCCESS;
            case "REFUSED" -> PaymentStatus.FAILED;
            case "REIMBURSED" -> PaymentStatus.REFUNDED;
            case "CANCELLED" -> PaymentStatus.CANCELLED;
            case "FROZEN" -> PaymentStatus.FROZEN;
            default -> {
                log.warn("Statut Pleenk inconnu: {}", status);
                yield PaymentStatus.PENDING;
            }
        };
    }
}