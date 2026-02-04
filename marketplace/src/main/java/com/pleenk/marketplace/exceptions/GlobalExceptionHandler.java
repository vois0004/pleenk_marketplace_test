package com.pleenk.marketplace.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gestion des produits non trouvés
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleProductNotFoundException(ProductNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );

        problemDetail.setTitle("Product Not Found");
        problemDetail.setProperty("timestamp", LocalDateTime.now());

        return problemDetail;
    }

    /**
     * Gestion du stock insuffisant
     */
    @ExceptionHandler(NotEnoughStockException.class)
    public ProblemDetail handleInsufficientStockException(NotEnoughStockException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );

        problemDetail.setTitle("Not Enough Stock");
        problemDetail.setProperty("timestamp", LocalDateTime.now());

        return problemDetail;
    }

    /**
     * Gestion des erreurs de paiement
     */
    @ExceptionHandler(PaymentException.class)
    public ProblemDetail handlePaymentException(PaymentException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.PAYMENT_REQUIRED,
                ex.getMessage()
        );

        problemDetail.setTitle("Payment Error");
        problemDetail.setProperty("timestamp", LocalDateTime.now());

        return problemDetail;
    }

    /**
     * Gestion des erreurs de validation (annotations @Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Les données fournies sont invalides"
        );

        problemDetail.setTitle("Validation Failed");
        problemDetail.setProperty("timestamp", LocalDateTime.now());

        // Extraire les erreurs de validation
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        problemDetail.setProperty("validationErrors", validationErrors);

        return problemDetail;
    }

    /**
     * Gestion des exceptions génériques
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGlobalException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur inattendue s'est produite"
        );

        problemDetail.setTitle("Internal Server Error");
        problemDetail.setProperty("timestamp", LocalDateTime.now());

        return problemDetail;
    }
}