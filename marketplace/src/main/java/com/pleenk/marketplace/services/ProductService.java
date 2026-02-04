package com.pleenk.marketplace.services;

import com.pleenk.marketplace.entities.Product;
import com.pleenk.marketplace.entities.ProductStatus;
import com.pleenk.marketplace.exceptions.NotEnoughStockException;
import com.pleenk.marketplace.exceptions.ProductNotFoundException;
import com.pleenk.marketplace.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Produit " + id + " introuvable"));
    }

    @Transactional
    public void decrementProductQuantity(Long id, Integer quantity) {
        Product product = getProductById(id);

        if (product.getQuantity() < quantity) {
            String msg = String.format("Stock insuffisant pour '%s' (%d) : dispo %d, demandé %d",
                    product.getName(), id, product.getQuantity(), quantity);
            log.error(msg);
            throw new NotEnoughStockException(msg);
        }

        int newQty = product.getQuantity() - quantity;
        product.setQuantity(newQty);

        if (newQty == 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
            log.warn("Produit '{}' ({}) en rupture de stock", product.getName(), id);
        }

        productRepository.save(product);
    }
}