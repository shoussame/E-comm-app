package com.customerService.demo;

import com.customerService.demo.dao.CustomerRepository;
import com.customerService.demo.entity.Customer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;

@SpringBootApplication
public class DemoApplication{

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    CommandLineRunner start(CustomerRepository customerRepository,
                            RepositoryRestConfiguration repositoryRestConfiguration) {
        repositoryRestConfiguration.exposeIdsFor(Customer.class);
        return args -> {
            customerRepository.save(new Customer(null, "abdo", "email@gmail.com"));
            customerRepository.save(new Customer(null, "reda", "email@gmail.com"));
            customerRepository.save(new Customer(null, "hamza", "email@gmail.com"));

            customerRepository.findAll().forEach(c -> {
                c.toString();
            });
        };
    }

//    @Override
//    public void run(String... args) throws Exception {
//        customerRepository.save(new Customer(null, "abdo", "email@gmail.com"));
//        customerRepository.save(new Customer(null, "reda", "email@gmail.com"));
//        customerRepository.save(new Customer(null, "hamza", "email@gmail.com"));
//
//        customerRepository.findAll().forEach(c -> {
//            c.toString();
//        });
//    }
}
