package com.pleenk.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PleenkWebhookDTO {
    private List<Event> events;

    @Data
    public static class Event {
        private String eventType;
        private PaymentData data;
    }

    @Data
    public static class PaymentData {
        private String transactionRef;
        private String status;
    }
}