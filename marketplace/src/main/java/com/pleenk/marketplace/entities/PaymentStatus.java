package com.pleenk.marketplace.entities;

public enum PaymentStatus {
    PENDING,      // En attente de paiement
    FROZEN,       // Gelé
    SUCCESS,      // Paiement réussi
    FAILED,       // Paiement échoué
    CANCELLED,    // Paiement annulé par l'utilisateur
    REFUNDED      // Paiement remboursé
}