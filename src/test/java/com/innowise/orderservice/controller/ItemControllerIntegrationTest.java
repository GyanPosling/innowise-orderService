package com.innowise.orderservice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.innowise.orderservice.integration.AbstractIntegrationTest;
import com.innowise.orderservice.model.dto.request.ItemCreateRequest;
import com.innowise.orderservice.model.dto.request.ItemUpdateRequest;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.repository.ItemRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class ItemControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void create_shouldReturnCreatedItem() throws Exception {
        ItemCreateRequest request = ItemCreateRequest.builder()
                .name("Laptop")
                .price(BigDecimal.valueOf(1200.50))
                .build();

        mockMvc.perform(post("/api/v1/items")
                        .headers(adminHeaders("POST", "/api/v1/items"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(1200.5));
    }

    @Test
    void getById_shouldReturnItem() throws Exception {
        Item item = itemRepository.save(Item.builder()
                .name("Keyboard")
                .price(BigDecimal.valueOf(99.99))
                .build());

        mockMvc.perform(get("/api/v1/items/{id}", item.getId())
                        .headers(adminHeaders("GET", "/api/v1/items/" + item.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item.getId()))
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.price").value(99.99));
    }

    @Test
    void getAll_shouldReturnItems() throws Exception {
        itemRepository.save(Item.builder()
                .name("Mouse")
                .price(BigDecimal.valueOf(25))
                .build());
        itemRepository.save(Item.builder()
                .name("Monitor")
                .price(BigDecimal.valueOf(350))
                .build());

        mockMvc.perform(get("/api/v1/items")
                        .headers(adminHeaders("GET", "/api/v1/items")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void update_shouldModifyItem() throws Exception {
        Item item = itemRepository.save(Item.builder()
                .name("Tablet")
                .price(BigDecimal.valueOf(450))
                .build());
        ItemUpdateRequest request = ItemUpdateRequest.builder()
                .name("Tablet Pro")
                .price(BigDecimal.valueOf(499.99))
                .build();

        mockMvc.perform(patch("/api/v1/items/{id}", item.getId())
                        .headers(adminHeaders("PATCH", "/api/v1/items/" + item.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item.getId()))
                .andExpect(jsonPath("$.name").value("Tablet Pro"))
                .andExpect(jsonPath("$.price").value(499.99));
    }

    @Test
    void delete_shouldRemoveItem() throws Exception {
        Item item = itemRepository.save(Item.builder()
                .name("Camera")
                .price(BigDecimal.valueOf(800))
                .build());

        mockMvc.perform(delete("/api/v1/items/{id}", item.getId())
                        .headers(adminHeaders("DELETE", "/api/v1/items/" + item.getId())))
                .andExpect(status().isNoContent());
    }
}
