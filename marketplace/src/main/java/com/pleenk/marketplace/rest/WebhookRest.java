package com.pleenk.marketplace.rest;

import com.pleenk.marketplace.dto.PleenkWebhookDTO;
import com.pleenk.marketplace.entities.PaymentStatus;
import com.pleenk.marketplace.services.PaymentService;
import com.pleenk.marketplace.services.PleenkService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Webhooks", description = "API de réception des webhooks")
public class WebhookRest {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;
    private final PleenkService pleenkService;




    @PostMapping("/pleenk")
    public ResponseEntity<Void> handlePleenkWebhook(
            @RequestHeader("pleenk-signature") String sig,
            @RequestBody String body
    ) {
        if (!pleenkService.checkSignature(body, sig)) {
            //todo : faire un job qui vérifie les paiements en PENDING depuis trop longtemps
            return ResponseEntity.status(401).build();
        }

        try {
            PleenkWebhookDTO webhook = objectMapper.readValue(body, PleenkWebhookDTO.class);

            for (var event : webhook.getEvents()) {
                if (!"PAYMENT".equals(event.getEventType())) continue;

                var data = event.getData();
                PaymentStatus status = paymentService.mapStatus(data.getStatus());
                paymentService.updatePaymentStatus(data.getTransactionRef(), status);

                log.info("Webhook OK - ref: {}, status: {}", data.getTransactionRef(), status);
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur webhook: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}