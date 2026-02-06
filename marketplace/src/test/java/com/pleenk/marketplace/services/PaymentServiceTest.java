package com.pleenk.marketplace.services;

import com.pleenk.marketplace.entities.Payment;
import com.pleenk.marketplace.entities.PaymentStatus;
import com.pleenk.marketplace.entities.Product;
import com.pleenk.marketplace.entities.ProductStatus;
import com.pleenk.marketplace.exceptions.NotEnoughStockException;
import com.pleenk.marketplace.exceptions.PaymentException;
import com.pleenk.marketplace.repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private ProductService productService;
    @Mock private PleenkService pleenkService;

    @InjectMocks private PaymentService paymentService;

    private Product product;
    private Payment payment;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("MacBook Pro")
                .price(new BigDecimal("2499.99"))
                .quantity(10)
                .status(ProductStatus.ACTIVE)
                .build();

        payment = Payment.builder()
                .id(1L)
                .productId(1L)
                .quantity(2)
                .amount(new BigDecimal("4999.98"))
                .status(PaymentStatus.PENDING)
                .pleenkTransactionRef("Order-test-123")
                .build();
    }

    @Test
    void getPaymentById_found() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        Payment result = paymentService.getPaymentById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualByComparingTo("4999.98");
    }

    @Test
    void getPaymentById_notFound() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentById(999L))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("999");
    }

    @Test
    void createPayment_success() {
        Map<String, String> pleenkData = Map.of(
                "transactionRef", "Order-uuid",
                "paymentUrl", "https://pleenk.com/pay"
        );

        when(productService.getProductById(1L)).thenReturn(product);
        when(paymentRepository.save(any())).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(pleenkService.createPaymentLink(any(), any())).thenReturn(pleenkData);

        Payment result = paymentService.createPayment(1L, 2);

        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getQuantity()).isEqualTo(2);
        assertThat(result.getAmount()).isEqualByComparingTo("4999.98");
        assertThat(result.getPleenkTransactionRef()).isEqualTo("Order-uuid");
        verify(paymentRepository, times(1)).save(any());
    }

    @Test
    void createPayment_calculatesAmountCorrectly() {
        when(productService.getProductById(1L)).thenReturn(product);
        when(paymentRepository.save(any())).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(pleenkService.createPaymentLink(any(), any()))
                .thenReturn(Map.of("transactionRef", "x", "paymentUrl", "y"));

        paymentService.createPayment(1L, 3);

        verify(paymentRepository, atLeastOnce()).save(argThat(p ->
                p.getAmount().compareTo(new BigDecimal("7499.97")) == 0
        ));
    }

    @Test
    void createPayment_notEnoughStock() {
        product.setQuantity(1);
        when(productService.getProductById(1L)).thenReturn(product);
        doThrow(new NotEnoughStockException("Stock insuffisant"))
                .when(productService).checkStock(product, 5);

        assertThatThrownBy(() -> paymentService.createPayment(1L, 5))
                .isInstanceOf(NotEnoughStockException.class)
                .hasMessageContaining("insuffisant");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPayment_pleenkFails() {
        when(productService.getProductById(1L)).thenReturn(product);
        when(pleenkService.createPaymentLink(any(), any()))
                .thenThrow(new RuntimeException("Pleenk error"));

        assertThatThrownBy(() -> paymentService.createPayment(1L, 2))
                .isInstanceOf(PaymentException.class);

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void updatePaymentStatus_toSuccess() {
        when(paymentRepository.findByPleenkTransactionRef("Order-test-123"))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenReturn(payment);

        paymentService.updatePaymentStatus("Order-test-123", PaymentStatus.SUCCESS);

        verify(paymentRepository).save(argThat(p -> p.getStatus() == PaymentStatus.SUCCESS));
        verify(productService).decrementProductQuantity(1L, 2);
    }

    @Test
    void updatePaymentStatus_ignoreDuplicateSuccess() {
        payment.setStatus(PaymentStatus.SUCCESS);
        when(paymentRepository.findByPleenkTransactionRef("Order-test-123"))
                .thenReturn(Optional.of(payment));

        paymentService.updatePaymentStatus("Order-test-123", PaymentStatus.SUCCESS);

        verify(paymentRepository, never()).save(any());
        verify(productService, never()).decrementProductQuantity(any(), any());
    }

    @Test
    void updatePaymentStatus_notFound() {
        when(paymentRepository.findByPleenkTransactionRef("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                paymentService.updatePaymentStatus("unknown", PaymentStatus.SUCCESS))
                .isInstanceOf(PaymentException.class);
    }

    @Test
    void updatePaymentStatus_toFailedWithoutDecrementingStock() {
        when(paymentRepository.findByPleenkTransactionRef("Order-test-123"))
                .thenReturn(Optional.of(payment));

        paymentService.updatePaymentStatus("Order-test-123", PaymentStatus.FAILED);

        verify(productService, never()).decrementProductQuantity(any(), any());
    }

    @Test
    void updatePaymentStatus_handlesStockDecrementFailure() {
        when(paymentRepository.findByPleenkTransactionRef("Order-test-123"))
                .thenReturn(Optional.of(payment));
        doThrow(new RuntimeException()).when(productService)
                .decrementProductQuantity(any(), any());

        // Ne devrait pas throw
        paymentService.updatePaymentStatus("Order-test-123", PaymentStatus.SUCCESS);

        verify(paymentRepository).save(any());
    }
}