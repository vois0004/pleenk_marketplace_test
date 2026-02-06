package com.pleenk.marketplace.dto;
import com.pleenk.marketplace.entities.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {

    private Long id;
    private BigDecimal amount;
    private Long productId;
    private Integer quantity;
    private PaymentStatus status;
    private String pleenkTransactionRef;
    private String pleenkPaymentUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}