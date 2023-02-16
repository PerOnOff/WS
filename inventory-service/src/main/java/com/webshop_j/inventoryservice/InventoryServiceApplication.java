package com.webshop_j.inventoryservice;

import com.webshop_j.inventoryservice.model.Inventory;
import com.webshop_j.inventoryservice.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadData(InventoryRepository inventoryRepository) {
        return args -> {
            Inventory inventory1 = new Inventory();

            //test data

            inventory1.setSkuCode("test_skuCode");
            inventory1.setQuantity(100);
            inventory1.setId(1L);
            Inventory inventory2 = new Inventory();
            inventory2.setSkuCode("test_skuCode2");
            inventory2.setQuantity(0);
            inventory2.setId(2L);
            inventoryRepository.save(inventory1);
            inventoryRepository.save(inventory2);
        };
    }

}
