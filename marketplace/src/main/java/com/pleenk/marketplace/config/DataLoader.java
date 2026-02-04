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
                            .name("Potion de mana (bleue) — format raid")
                            .description("Restaure 250 MP. Peut provoquer des incantations incontrôlées en réunion.")
                            .price(new BigDecimal("4.99"))
                            .quantity(99)
                            .sellerId(1L)
                            .imageUrl("https://commons.wikimedia.org/wiki/Special:FilePath/Potion-ball_-_Lorc_-_game-icons.svg")
                            .status(ProductStatus.ACTIVE)
                            .build(),

                    Product.builder()
                            .name("Dé D20 béni (critique garanti*)")
                            .description("*Non garanti. Mais il a l’air convaincant. Parfait pour les jets dramatiques.            " +
                            "!! Ne fonctionne pas. Voir liste bugs")
                            .price(new BigDecimal("12.00"))
                            .quantity(40)
                            .sellerId(3L)
                            .imageUrl("https://commons.wikimedia.org/wiki/Special:FilePath/Twenty%20sided%20dice.svg")
                            .status(ProductStatus.ACTIVE)
                            .build(),

                    Product.builder()
                            .name("Cape d’invisibilité")
                            .description("Les joueurs pensent être furtifs. Le MJ sait. Toujours.")
                            .price(new BigDecimal("59.99"))
                            .quantity(0)
                            .sellerId(4L)
                            .imageUrl("https://commons.wikimedia.org/wiki/Special:FilePath/Cloakroom%20icon.svg")
                            .status(ProductStatus.OUT_OF_STOCK)
                            .build(),

                    Product.builder()
                            .name("Grimoire du min-maxeur")
                            .description("Optimise ton build, ruine l’équilibrage, et déclenche des soupirs de MJ.")
                            .price(new BigDecimal("24.99"))
                            .quantity(1)
                            .sellerId(5L)
                            .imageUrl("https://commons.wikimedia.org/wiki/Special:FilePath/Open%20book%2001.svg")
                            .status(ProductStatus.ACTIVE)
                            .build()
            );

            productRepository.saveAll(products);
        }
    }
}
