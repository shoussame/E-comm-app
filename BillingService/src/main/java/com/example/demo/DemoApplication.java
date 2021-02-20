package com.example.demo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
class Bill{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date billingDate;
    private long customerID;
    @Transient
    private Customer customer;
    @OneToMany(mappedBy = "bill")
    private Collection<ProductItem> productItems;

}
@RepositoryRestResource
interface BillRepository extends JpaRepository<Bill,Long>{
}

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
class ProductItem{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long productID;
    @Transient
    private Product product;
    @Transient
    private String productName;
    private double price;
    private double quantity;
    @ManyToOne @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Bill bill;
}
@RepositoryRestResource
interface ProductItemRepository extends JpaRepository<ProductItem,Long> {
    List<ProductItem> findByBillId(Long billID);
}

@Data
class Product{
    private Long id;
    private String name;
    private double price;
}

@Data
class Customer{
    private Long id;
    private String name;
    private String email;
}

@FeignClient(name="customer-service")
interface CustomerRestClient{
    @GetMapping("/customers/{id}")
    Customer findCustomerById(@PathVariable("id") Long id);
}
@FeignClient(name="inventory-service")
interface InventoryRestClient{
    @GetMapping("/products/{id}")
    Product getProductById(@PathVariable("id") Long id);
    @GetMapping("/products")
    PagedModel<Product> pageProducts();
}

@RestController
class BillRestController{
    @Autowired private BillRepository billRepository;
    @Autowired private ProductItemRepository productItemRepository;
    @Autowired private CustomerRestClient customerRestClient;
    @Autowired private InventoryRestClient inventoryRestClient;
    @GetMapping("/bills/full/{id}")
    Bill getBill(@PathVariable(name="id") Long id){
        Bill bill=billRepository.findById(id).get();
        bill.setCustomer(customerRestClient.findCustomerById(bill.getCustomerID()));
        bill.getProductItems().forEach(pi->{
            Product p = inventoryRestClient.getProductById(pi.getProductID());
            pi.setProduct(p);
            pi.setProductName(p.getName());
        }) ;
        return bill;
    }
}


@SpringBootApplication
@EnableFeignClients
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    CommandLineRunner start(BillRepository billRepository,
                            ProductItemRepository productItemRepository,
                            CustomerRestClient customerRestClient,
                            InventoryRestClient inventoryRestClient) {
        return args -> {
            Customer c1 = customerRestClient.findCustomerById(1L);
            Bill bill = billRepository.save(new Bill(null, new Date(), c1.getId(), null, null));
            System.out.println("*********************");
            System.out.println("ID :" + c1.getId());
            System.out.println("Nom :" + c1.getName());
            System.out.println("Email :" + c1.getEmail());
            System.out.println("*********************");
            PagedModel<Product> products = inventoryRestClient.pageProducts();
            products.forEach(p -> {
                ProductItem productItem = new ProductItem();
                productItem.setPrice(p.getPrice());
                productItem.setQuantity(20);
                productItem.setBill(bill);
                productItem.setProductID(p.getId());
                productItemRepository.save(productItem);
            });
        };
    }
}
