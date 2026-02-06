package com.pleenk.marketplace.repositories;

import com.pleenk.marketplace.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Trouver un paiement par son ID Pleenk
     */
    Optional<Payment> findByPleenkTransactionRef(String pleenkTransactionRef);
}