package com.pleenk.marketplace.rest;

import com.pleenk.marketplace.dto.CreatePaymentRequestDTO;
import com.pleenk.marketplace.dto.PaymentResponseDTO;
import com.pleenk.marketplace.entities.Payment;
import com.pleenk.marketplace.mappers.PaymentMapper;
import com.pleenk.marketplace.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Payments", description = "API de gestion des paiements")
public class PaymentRest {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @Operation(summary = "Créer un paiement", description = "Crée un paiement et génère le lien Pleenk")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Paiement créé",
                    content = @Content(schema = @Schema(implementation = PaymentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "404", description = "Produit introuvable"),
            @ApiResponse(responseCode = "409", description = "Stock insuffisant"),
            @ApiResponse(responseCode = "402", description = "Erreur paiement")
    })
    @PostMapping("/create")
    public ResponseEntity<PaymentResponseDTO> createPayment(@Valid @RequestBody CreatePaymentRequestDTO request) {
        log.info("Création paiement - produit: {}, qté: {}", request.getProductId(), request.getQuantity());

        Payment payment = paymentService.createPayment(request.getProductId(), request.getQuantity());

        return ResponseEntity.status(201).body(paymentMapper.toDTO(payment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentMapper.toDTO(paymentService.getPaymentById(id)));
    }
}