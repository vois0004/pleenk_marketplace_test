package com.pleenk.marketplace.rest;

import com.pleenk.marketplace.dto.ProductResponseDTO;
import com.pleenk.marketplace.mappers.ProductMapper;
import com.pleenk.marketplace.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Products", description = "API de gestion des produits")
public class ProductRest {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @Operation(summary = "Liste des produits", description = "Retourne tous les produits de la marketplace")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ProductResponseDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        log.info("Récupération de tous les produits");

        return ResponseEntity.ok(
                productService.getAllProducts().stream()
                        .map(productMapper::toDTO)
                        .toList()
        );
    }

    @Operation(summary = "Détails d'un produit", description = "Retourne un produit par son ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produit trouvé",
                    content = @Content(schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Produit introuvable")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        log.info("Récupération produit {}", id);
        return ResponseEntity.ok(productMapper.toDTO(productService.getProductById(id)));
    }
}