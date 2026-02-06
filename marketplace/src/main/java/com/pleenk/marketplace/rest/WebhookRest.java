package com.pleenk.marketplace.rest;

import com.pleenk.marketplace.dto.PleenkWebhookDTO;
import com.pleenk.marketplace.entities.PaymentStatus;
import com.pleenk.marketplace.services.PaymentService;
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

    @Value("${pleenk.public-key}")
    private String pleenkPublicKeyPem;

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;

    private PublicKey cachedKey;

    private PublicKey getPublicKey() {
        if (cachedKey != null) return cachedKey;

        try {
            String clean = pleenkPublicKeyPem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] bytes = Base64.getDecoder().decode(clean);
            cachedKey = KeyFactory.getInstance("EC")
                    .generatePublic(new X509EncodedKeySpec(bytes));
            return cachedKey;

        } catch (Exception e) {
            log.error("Impossible de charger la clé Pleenk", e);
            throw new RuntimeException("Erreur chargement clé: " + e.getMessage(), e);
        }
    }

    private boolean checkSignature(String body, String sig) {
        try {

            byte[] sigBytes = Base64.getUrlDecoder().decode(sig);

            Signature signature = Signature.getInstance("SHA512withECDSA");
            signature.initVerify(getPublicKey());
            signature.update(body.getBytes(StandardCharsets.UTF_8));

            boolean valid = signature.verify(sigBytes);
            if (!valid) log.warn("Signature rejetée");
            return valid;

        } catch (Exception e) {
            log.error("Erreur vérification signature", e);
            return false;
        }
    }

    @PostMapping("/pleenk")
    public ResponseEntity<Void> handlePleenkWebhook(
            @RequestHeader("pleenk-signature") String sig,
            @RequestBody String body
    ) {
        if (!checkSignature(body, sig)) {
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