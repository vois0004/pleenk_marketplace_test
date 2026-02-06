package com.pleenk.marketplace.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class PleenkServiceTest {

    private PleenkService service;

    private static final Properties TEST_PROPS = new Properties();

    static {
        try (InputStream is = PleenkServiceTest.class.getClassLoader()
                .getResourceAsStream("application-test.properties")) {
            if (is == null) {
                throw new IllegalStateException(
                        "application-test.properties not found. Copy application-test.properties.example and fill in values.");
            }
            TEST_PROPS.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application-test.properties", e);
        }
    }

    @BeforeEach
    void setUp() {
        service = new PleenkService();
        ReflectionTestUtils.setField(service, "widgetBaseUrl", TEST_PROPS.getProperty("pleenk.widget.base-url"));
        ReflectionTestUtils.setField(service, "walletId", TEST_PROPS.getProperty("pleenk.wallet.id"));
        ReflectionTestUtils.setField(service, "privateKeyPem", TEST_PROPS.getProperty("pleenk.private-key"));
    }

    @Test
    void createPaymentLink_generatesValidLink() {
        Map<String, String> result = service.createPaymentLink(
                new BigDecimal("100.00"), "Test payment");

        assertThat(result).containsKeys("transactionRef", "paymentUrl");
        assertThat(result.get("transactionRef")).startsWith("Order-");
        assertThat(result.get("paymentUrl")).startsWith(TEST_PROPS.getProperty("pleenk.widget.base-url") + "/payment?");
    }

    @Test
    void createPaymentLink_includesRequiredParams() {
        Map<String, String> result = service.createPaymentLink(
                new BigDecimal("50.00"), "Test");

        String url = result.get("paymentUrl");
        assertThat(url).contains("pw_confidentiality_wallet=" + TEST_PROPS.getProperty("pleenk.wallet.id"));
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