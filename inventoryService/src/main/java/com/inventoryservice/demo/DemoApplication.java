package com.inventoryservice.demo;

import com.inventoryservice.demo.dao.ProductRepository;
import com.inventoryservice.demo.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    CommandLineRunner start(ProductRepository productRepository,
                            RepositoryRestConfiguration repositoryRestConfiguration) {
        repositoryRestConfiguration.exposeIdsFor(Product.class);
        return args -> {
            productRepository.save(new Product(null, "tv", 1000));
            productRepository.save(new Product(null, "phone", 800));
            productRepository.save(new Product(null, "pc", 2000));

            productRepository.findAll().forEach(product -> {
                product.toString();
            });
        };
    }
}
