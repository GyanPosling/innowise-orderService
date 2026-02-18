// package com.innowise.orderservice.controller;

// import static org.hamcrest.Matchers.is;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.innowise.orderservice.AbstractIntegrationTest;
// import com.innowise.orderservice.client.UserServiceClient;
// import com.innowise.orderservice.model.dto.request.OrderCreateRequest;
// import com.innowise.orderservice.model.dto.request.OrderItemRequest;
// import com.innowise.orderservice.model.dto.request.OrderUpdateRequest;
// import com.innowise.orderservice.model.dto.response.UserInfoResponse;
// import com.innowise.orderservice.model.entity.Item;
// import com.innowise.orderservice.model.entity.Order;
// import com.innowise.orderservice.model.entity.OrderStatus;
// import com.innowise.orderservice.repository.ItemRepository;
// import com.innowise.orderservice.repository.OrderRepository;
// import java.math.BigDecimal;
// import java.util.List;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;

// class OrderControllerIntegrationTest extends AbstractIntegrationTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @Autowired
//     private ItemRepository itemRepository;

//     @Autowired
//     private OrderRepository orderRepository;

//     @MockBean
//     private UserServiceClient userServiceClient;

//     @Test
//     @DisplayName("Create Order: Success (201)")
//     void createOrder_Success() throws Exception {
//         Item item = itemRepository.save(Item.builder().name("phone").price(BigDecimal.TEN).build());
//         OrderCreateRequest request = OrderCreateRequest.builder()
//                 .userId(1L)
//                 .userEmail("user@example.com")
//                 .status(OrderStatus.NEW)
//                 .totalPrice(BigDecimal.valueOf(99))
//                 .items(List.of(OrderItemRequest.builder()
//                         .itemId(item.getId())
//                         .quantity(2)
//                         .build()))
//                 .build();

//         UserInfoResponse userInfo = UserInfoResponse.builder().email("user@example.com").build();
//         org.mockito.Mockito.when(userServiceClient.getUserByEmail("user@example.com")).thenReturn(userInfo);

//         String token = generateTestToken(1L, "admin@test.com", "ADMIN");

//         mockMvc.perform(post("/api/v1/orders")
//                         .header("Authorization", token)
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(request)))
//                 .andExpect(status().isCreated())
//                 .andExpect(jsonPath("$.userEmail", is("user@example.com")))
//                 .andExpect(jsonPath("$.items[0].itemId").value(item.getId().intValue()));
//     }

//     @Test
//     @DisplayName("Create Order: Fail Validation (400) on Empty Items")
//     void createOrder_ValidationFail() throws Exception {
//         OrderCreateRequest request = OrderCreateRequest.builder()
//                 .userId(1L)
//                 .userEmail("user@example.com")
//                 .status(OrderStatus.NEW)
//                 .totalPrice(BigDecimal.valueOf(99))
//                 .items(List.of())
//                 .build();

//         String token = generateTestToken(1L, "admin@test.com", "ADMIN");

//         mockMvc.perform(post("/api/v1/orders")
//                         .header("Authorization", token)
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(request)))
//                 .andExpect(status().isBadRequest())
//                 .andExpect(jsonPath("$.message").value("Validation failed"))
//                 .andExpect(jsonPath("$.details.items").exists());
//     }

//     @Test
//     @DisplayName("Get Order By ID: Success (200)")
//     void getOrderById_Success() throws Exception {
//         Order order = new Order();
//         order.setUserId(1L);
//         order.setUserEmail("user@example.com");
//         order.setStatus(OrderStatus.NEW);
//         order.setTotalPrice(BigDecimal.TEN);
//         order = orderRepository.saveAndFlush(order);

//         UserInfoResponse userInfo = UserInfoResponse.builder().email("user@example.com").build();
//         org.mockito.Mockito.when(userServiceClient.getUserByEmail("user@example.com")).thenReturn(userInfo);

//         String token = generateTestToken(1L, "admin@test.com", "ADMIN");

//         mockMvc.perform(get("/api/v1/orders/" + order.getId())
//                         .header("Authorization", token))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.id").value(order.getId()))
//                 .andExpect(jsonPath("$.userEmail", is("user@example.com")));
//     }

//     @Test
//     @DisplayName("Get Order By ID: Not Found (404)")
//     void getOrderById_NotFound() throws Exception {
//         String token = generateTestToken(1L, "admin@test.com", "ADMIN");

//         mockMvc.perform(get("/api/v1/orders/99999")
//                         .header("Authorization", token))
//                 .andExpect(status().isNotFound());
//     }

//     @Test
//     @DisplayName("Update Order: Success (200)")
//     void updateOrder_Success() throws Exception {
//         Item item = itemRepository.save(Item.builder().name("phone").price(BigDecimal.TEN).build());
//         Order order = new Order();
//         order.setUserId(1L);
//         order.setUserEmail("user@example.com");
//         order.setStatus(OrderStatus.NEW);
//         order.setTotalPrice(BigDecimal.TEN);
//         order = orderRepository.saveAndFlush(order);

//         OrderUpdateRequest request = OrderUpdateRequest.builder()
//                 .status(OrderStatus.PAID)
//                 .totalPrice(BigDecimal.valueOf(120))
//                 .items(List.of(OrderItemRequest.builder()
//                         .itemId(item.getId())
//                         .quantity(1)
//                         .build()))
//                 .build();

//         UserInfoResponse userInfo = UserInfoResponse.builder().email("user@example.com").build();
//         org.mockito.Mockito.when(userServiceClient.getUserByEmail("user@example.com")).thenReturn(userInfo);

//         String token = generateTestToken(1L, "admin@test.com", "ADMIN");

//         mockMvc.perform(put("/api/v1/orders/" + order.getId())
//                         .header("Authorization", token)
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(request)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.status", is("PAID")))
//                 .andExpect(jsonPath("$.totalPrice").value(120));
//     }

//     @Test
//     @DisplayName("Delete Order: Success (204)")
//     void deleteOrder_Success() throws Exception {
//         Order order = new Order();
//         order.setUserId(1L);
//         order.setUserEmail("user@example.com");
//         order.setStatus(OrderStatus.NEW);
//         order.setTotalPrice(BigDecimal.TEN);
//         order = orderRepository.saveAndFlush(order);

//         String token = generateTestToken(1L, "admin@test.com", "ADMIN");

//         mockMvc.perform(delete("/api/v1/orders/" + order.getId())
//                         .header("Authorization", token))
//                 .andExpect(status().isNoContent());
//     }

//     @Test
//     @DisplayName("Delete Order: Not Found (404)")
//     void deleteOrder_NotFound() throws Exception {
//         String token = generateTestToken(1L, "admin@test.com", "ADMIN");

//         mockMvc.perform(delete("/api/v1/orders/99999")
//                         .header("Authorization", token))
//                 .andExpect(status().isNotFound());
//     }
// }
