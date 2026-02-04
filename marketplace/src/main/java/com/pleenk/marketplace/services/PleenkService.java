package com.pleenk.marketplace.services;

import com.pleenk.marketplace.exceptions.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PleenkService {

    @Value("${pleenk.widget.base-url}")
    private String widgetBaseUrl;

    @Value("${pleenk.wallet.id}")
    private String walletId;

    @Value("${pleenk.private-key}")
    private String privateKeyPem;

    private PrivateKey cachedPrivateKey;

    private PrivateKey getPrivateKey() {
        if (cachedPrivateKey != null) {
            return cachedPrivateKey;
        }

        try {
            String cleanKey = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(cleanKey);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

            cachedPrivateKey = KeyFactory.getInstance("EC").generatePrivate(spec);
            return cachedPrivateKey;

        } catch (Exception e) {
            log.error("Impossible de charger la clé privée Pleenk", e);
            throw new RuntimeException("Erreur chargement clé: " + e.getMessage(), e);
        }
    }

    public Map<String, String> createPaymentLink(BigDecimal amount, String description) {
        try {
            String txRef = "Order-" + UUID.randomUUID();

            Map<String, String> params = new LinkedHashMap<>();
            params.put("pw_confidentiality_wallet", walletId);
            params.put("pw_description", description);
            params.put("pw_transaction_ref", txRef);
            params.put("pw_beneficiary_0", walletId + "|" + amount);

            params.put("signature", sign(params));

            return Map.of(
                    "paymentId", txRef,
                    "paymentUrl", buildUrl(params)
            );

        } catch (Exception e) {
            log.error("Erreur génération lien Pleenk", e);
            throw new PaymentException("Impossible de générer le lien: " + e.getMessage(), e);
        }
    }

    private String buildUrl(Map<String, String> params) {
        String query = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        return widgetBaseUrl + "/payment?" + query;
    }

    private String sign(Map<String, String> params) throws Exception {
        String data = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.joining("+"));

        Signature sig = Signature.getInstance("SHA512withECDSA");
        sig.initSign(getPrivateKey());
        sig.update(data.getBytes(StandardCharsets.UTF_8));

        return Base64.getUrlEncoder().withoutPadding().encodeToString(sig.sign());
    }
}