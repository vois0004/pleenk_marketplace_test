package com.pleenk.marketplace.config;

import com.pleenk.marketplace.entities.Product;
import com.pleenk.marketplace.entities.ProductStatus;
import com.pleenk.marketplace.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        // Ne charge les données que si la table est vide
        if (productRepository.count() == 0) {

            List<Product> products = List.of(

                    Product.builder()
                            .name("Cosmopolitan")
                            .description("Restaure 250 MP. Peut provoquer des incantations incontrôlées en réunion.")
                            .price(new BigDecimal("15.99"))
                            .quantity(32)
                            .sellerId(2L)
                            .imageUrl("https://images.unsplash.com/photo-1514362545857-3bc16c4c7d1b?w=800&auto=format&fit=crop")
                            .status(ProductStatus.ACTIVE)
                            .build(),


                    Product.builder()
                            .name("Margarita Passion")
                            .description("Restaure 200 MP et +15 charisme. Risque élevé de décisions audacieuses.")
                            .price(new BigDecimal("3.99"))
                            .quantity(1)
                            .sellerId(2L)
                            .imageUrl("https://images.unsplash.com/photo-1609951651556-5334e2706168?w=800&auto=format&fit=crop")
                            .status(ProductStatus.ACTIVE)
                            .build()

                    );

            productRepository.saveAll(products);
        }
    }
}