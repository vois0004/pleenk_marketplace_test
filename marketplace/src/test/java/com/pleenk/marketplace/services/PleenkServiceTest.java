package com.pleenk.marketplace.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PleenkServiceTest {

    private PleenkService service;

    private static final String TEST_KEY = """
            -----BEGIN PRIVATE KEY-----
            MIHuAgEAMBAGByqGSM49AgEGBSuBBAAjBIHWMIHTAgEBBEIBzoOGDETTYI4WPab7
            a8NKmLsrwl4I+L3poSO7yZgFaDyim6YIMerAqERfkBFNGL4u1+QLr+iRzcRrxQ0g
            ZskrxqehgYkDgYYABAC8XawWjIErAg8sGG1byI9DkSYNRMBRK4to6ReEefB/uK3D
            Ia+B1dbumJBKbfxkaVCv+gMgtRUCu+vJknRgSk0WSwGYSgAD0I9eB+DgfCXM9/1E
            1QtJW5xrLtgcymw2Zrqxey9/vuRZd1j/A7Afzi3/Dn9bG8YGS2VBtSIlnBP1LC5N
            cg==
            -----END PRIVATE KEY-----""";

    @BeforeEach
    void setUp() {
        service = new PleenkService();
        ReflectionTestUtils.setField(service, "widgetBaseUrl", "https://pleenk.com");
        ReflectionTestUtils.setField(service, "walletId", "wallet-123");
        ReflectionTestUtils.setField(service, "privateKeyPem", TEST_KEY);
    }

    @Test
    void createPaymentLink_generatesValidLink() {
        Map<String, String> result = service.createPaymentLink(
                new BigDecimal("100.00"), "Test payment");

        assertThat(result).containsKeys("transactionRef", "paymentUrl");
        assertThat(result.get("transactionRef")).startsWith("Order-");
        assertThat(result.get("paymentUrl")).startsWith("https://pleenk.com/payment?");
    }

    @Test
    void createPaymentLink_includesRequiredParams() {
        Map<String, String> result = service.createPaymentLink(
                new BigDecimal("50.00"), "Test");

        String url = result.get("paymentUrl");
        assertThat(url).contains("pw_confidentiality_wallet=wallet-123");
        assertThat(url).contains("pw_description=");
        assertThat(url).contains("signature=");
        assertThat(url).contains("pw_beneficiary_0=");
    }

    @Test
    void createPaymentLink_generatesUniqueIds() {
        String id1 = service.createPaymentLink(new BigDecimal("100"), "A").get("transactionRef");
        String id2 = service.createPaymentLink(new BigDecimal("100"), "B").get("transactionRef");

        assertThat(id1).isNotEqualTo(id2);
    }
}