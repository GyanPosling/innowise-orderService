// package com.innowise.orderservice.controller;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.hamcrest.Matchers.hasSize;
// import static org.hamcrest.Matchers.is;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.innowise.orderservice.AbstractIntegrationTest;
// import com.innowise.orderservice.model.dto.request.ItemCreateRequest;
// import com.innowise.orderservice.model.dto.request.ItemUpdateRequest;
// import com.innowise.orderservice.model.entity.Item;
// import com.innowise.orderservice.repository.ItemRepository;
// import java.math.BigDecimal;
// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;

// class ItemControllerIntegrationTest extends AbstractIntegrationTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private ItemRepository itemRepository;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @AfterEach
//     void tearDown() {
//         itemRepository.deleteAll();
//     }

//     @Test
//     @DisplayName("Create Item: Success (201)")
//     void createItem_Success() throws Exception {
//         ItemCreateRequest request = ItemCreateRequest.builder()
//                 .name("Integration GPU")
//                 .price(new BigDecimal("999.99"))
//                 .build();

//         String token = generateTestToken(1L, "admin@test.com", "ADMIN");

//         mockMvc.perform(post("/api/v1/items")
//                         .header("Authorization", token)
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(request)))
//                 .andExpect(status().isCreated())
//                 .andExpect(jsonPath("$.name", is("Integration GPU")));
//     }

//     @Test
//     @DisplayName("Create Item: Fail Validation (400) on Negative Price")
//     void createItem_ValidationFail() throws Exception {
//         ItemCreateRequest request = ItemCreateRequest.builder()
//                 .name("Bad Item")
//                 .price(new BigDecimal("-10.00"))
//                 .build();

//         String token = generateTestToken(1L, "admin@test.com", "ADMIN");

//         mockMvc.perform(post("/api/v1/items")
//                         .header("Authorization", token)
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(request)))
//                 .andExpect(status().isBadRequest());
//     }

//     @Test
//     @DisplayName("Get Items: Success")
//     void getItems_Success() throws Exception {
//         itemRepository.save(Item.builder().name("Apple iPhone").price(BigDecimal.TEN).build());
//         itemRepository.save(Item.builder().name("Samsung Galaxy").price(BigDecimal.TEN).build());

//         String token = generateTestToken(1L, "user@test.com", "USER");

//         mockMvc.perform(get("/api/v1/items")
//                         .header("Authorization", token))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$", hasSize(2)));
//     }

//     @Test
//     @DisplayName("Get Item By ID: Success (200)")
//     void getItemById_Success() throws Exception {
//         Item item = itemRepository.save(Item.builder().name("Existing Item").price(BigDecimal.TEN).build());
//         String token = generateTestToken(2L, "user@test.com", "USER");

//         mockMvc.perform(get("/api/v1/items/" + item.getId())
//                         .header("Authorization", token))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.name", is("Existing Item")));
//     }

//     @Test
//     @DisplayName("Get Item By ID: Not Found (404)")
//     void getItemById_NotFound() throws Exception {
//         String token = generateTestToken(1L, "user@test.com", "USER");

//         mockMvc.perform(get("/api/v1/items/99999")
//                         .header("Authorization", token))
//                 .andExpect(status().isNotFound());
//     }

//     @Test
//     @DisplayName("Update Item: Success (200)")
//     void updateItem_Success() throws Exception {
//         Item saved = itemRepository.save(Item.builder().name("Old Name").price(BigDecimal.ONE).build());
//         String token = generateTestToken(1L, "admin@test.com", "ADMIN");

//         ItemUpdateRequest updateRequest = ItemUpdateRequest.builder()
//                 .name("New Name")
//                 .price(BigDecimal.TEN)
//                 .build();

//         mockMvc.perform(put("/api/v1/items/" + saved.getId())
//                         .header("Authorization", token)
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(updateRequest)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.name", is("New Name")));
//     }

//     @Test
//     @DisplayName("Update Item: Not Found (404)")
//     void updateItem_NotFound() throws Exception {
//         String token = generateTestToken(1L, "admin@test.com", "ADMIN");
//         ItemUpdateRequest updateRequest = ItemUpdateRequest.builder()
//                 .name("New Name")
//                 .price(BigDecimal.TEN)
//                 .build();

//         mockMvc.perform(put("/api/v1/items/99999")
//                         .header("Authorization", token)
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(updateRequest)))
//                 .andExpect(status().isNotFound());
//     }

//     @Test
//     @DisplayName("Delete Item: Success (204)")
//     void deleteItem_Success() throws Exception {
//         Item saved = itemRepository.save(Item.builder().name("Delete Me").price(BigDecimal.ONE).build());
//         String token = generateTestToken(1L, "admin@test.com", "ADMIN");

//         mockMvc.perform(delete("/api/v1/items/" + saved.getId())
//                         .header("Authorization", token))
//                 .andExpect(status().isNoContent());

//         assertThat(itemRepository.existsById(saved.getId())).isFalse();
//     }

//     @Test
//     @DisplayName("Delete Item: Not Found (404)")
//     void deleteItem_NotFound() throws Exception {
//         String token = generateTestToken(1L, "admin@test.com", "ADMIN");

//         mockMvc.perform(delete("/api/v1/items/99999")
//                         .header("Authorization", token))
//                 .andExpect(status().isNotFound());
//     }
// }
