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
// import com.innowise.orderservice.model.dto.request.OrderItemCreateRequest;
// import com.innowise.orderservice.model.dto.request.OrderItemUpdateRequest;
// import com.innowise.orderservice.model.entity.Item;
// import com.innowise.orderservice.model.entity.Order;
// import com.innowise.orderservice.model.entity.OrderItem;
// import com.innowise.orderservice.model.entity.OrderStatus;
// import com.innowise.orderservice.repository.ItemRepository;
// import com.innowise.orderservice.repository.OrderItemRepository;
// import com.innowise.orderservice.repository.OrderRepository;
// import java.math.BigDecimal;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;

// class OrderItemControllerIntegrationTest extends AbstractIntegrationTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @Autowired
//     private OrderRepository orderRepository;

//     @Autowired
//     private ItemRepository itemRepository;

//     @Autowired
//     private OrderItemRepository orderItemRepository;

//     @Test
//     @DisplayName("Create Order Item: Success (201)")
//     void createOrderItem_Success() throws Exception {
//         Order order = new Order();
//         order.setUserId(1L);
//         order.setUserEmail("user@example.com");
//         order.setStatus(OrderStatus.NEW);
//         order.setTotalPrice(BigDecimal.valueOf(40));
//         order = orderRepository.saveAndFlush(order);

//         Item item = itemRepository.save(Item.builder().name("headphones").price(BigDecimal.valueOf(20)).build());

//         OrderItemCreateRequest request = OrderItemCreateRequest.builder()
//                 .orderId(order.getId())
//                 .itemId(item.getId())
//                 .quantity(3)
//                 .build();

//         String token = generateTestToken(1L, "admin@test.com", "ADMIN");

//         mockMvc.perform(post("/api/v1/order-items")
//                         .header("Authorization", token)
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(request)))
//                 .andExpect(status().isCreated())
//                 .andExpect(jsonPath("$.orderId").value(order.getId().intValue()))
//                 .andExpect(jsonPath("$.itemId").value(item.getId().intValue()))
//                 .andExpect(jsonPath("$.quantity", is(3)));
//     }

//     @Test
//     @DisplayName("Create Order Item: Fail Validation (400) on Negative Quantity")
//     void createOrderItem_ValidationFail() throws Exception {
//         Order order = new Order();
//         order.setUserId(1L);
//         order.setUserEmail("user@example.com");
//         order.setStatus(OrderStatus.NEW);
//         order.setTotalPrice(BigDecimal.valueOf(40));
//         order = orderRepository.saveAndFlush(order);

//         Item item = itemRepository.save(Item.builder().name("headphones").price(BigDecimal.valueOf(20)).build());

//         OrderItemCreateRequest request = OrderItemCreateRequest.builder()
//                 .orderId(order.getId())
//                 .itemId(item.getId())
//                 .quantity(-1)
//                 .build();

//         String token = generateTestToken(1L, "admin@test.com", "ADMIN");

//         mockMvc.perform(post("/api/v1/order-items")
//                         .header("Authorization", token)
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(request)))
//                 .andExpect(status().isBadRequest())
//                 .andExpect(jsonPath("$.message").value("Validation failed"))
//                 .andExpect(jsonPath("$.details.quantity").exists());
//     }

//     @Test
//     @DisplayName("Get Order Item By ID: Success (200)")
//     void getOrderItemById_Success() throws Exception {
//         Order order = new Order();
//         order.setUserId(1L);
//         order.setUserEmail("user@example.com");
//         order.setStatus(OrderStatus.NEW);
//         order.setTotalPrice(BigDecimal.valueOf(40));
//         order = orderRepository.saveAndFlush(order);

//         Item item = itemRepository.save(Item.builder().name("headphones").price(BigDecimal.valueOf(20)).build());

//         OrderItem orderItem = OrderItem.builder()
//                 .order(order)
//                 .item(item)
//                 .quantity(2)
//                 .build();
//         orderItem = orderItemRepository.saveAndFlush(orderItem);

//         String token = generateTestToken(1L, "admin@test.com", "ADMIN");

//         mockMvc.perform(get("/api/v1/order-items/" + orderItem.getId())
//                         .header("Authorization", token))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.id").value(orderItem.getId()))
//                 .andExpect(jsonPath("$.quantity").value(2));
//     }

//     @Test
//     @DisplayName("Get Order Item By ID: Not Found (404)")
//     void getOrderItemById_NotFound() throws Exception {
//         String token = generateTestToken(1L, "admin@test.com", "ADMIN");

//         mockMvc.perform(get("/api/v1/order-items/99999")
//                         .header("Authorization", token))
//                 .andExpect(status().isNotFound());
//     }

//     @Test
//     @DisplayName("Update Order Item: Success (200)")
//     void updateOrderItem_Success() throws Exception {
//         Order order = new Order();
//         order.setUserId(1L);
//         order.setUserEmail("user@example.com");
//         order.setStatus(OrderStatus.NEW);
//         order.setTotalPrice(BigDecimal.valueOf(40));
//         order = orderRepository.saveAndFlush(order);

//         Item item = itemRepository.save(Item.builder().name("headphones").price(BigDecimal.valueOf(20)).build());

//         OrderItem orderItem = OrderItem.builder()
//                 .order(order)
//                 .item(item)
//                 .quantity(2)
//                 .build();
//         orderItem = orderItemRepository.saveAndFlush(orderItem);

//         OrderItemUpdateRequest request = OrderItemUpdateRequest.builder()
//                 .quantity(5)
//                 .build();

//         String token = generateTestToken(1L, "admin@test.com", "ADMIN");

//         mockMvc.perform(put("/api/v1/order-items/" + orderItem.getId())
//                         .header("Authorization", token)
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(request)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.id").value(orderItem.getId()))
//                 .andExpect(jsonPath("$.quantity", is(5)));
//     }

//     @Test
//     @DisplayName("Delete Order Item: Success (204)")
//     void deleteOrderItem_Success() throws Exception {
//         Order order = new Order();
//         order.setUserId(1L);
//         order.setUserEmail("user@example.com");
//         order.setStatus(OrderStatus.NEW);
//         order.setTotalPrice(BigDecimal.valueOf(40));
//         order = orderRepository.saveAndFlush(order);

//         Item item = itemRepository.save(Item.builder().name("headphones").price(BigDecimal.valueOf(20)).build());

//         OrderItem orderItem = OrderItem.builder()
//                 .order(order)
//                 .item(item)
//                 .quantity(2)
//                 .build();
//         orderItem = orderItemRepository.saveAndFlush(orderItem);

//         String token = generateTestToken(1L, "admin@test.com", "ADMIN");

//         mockMvc.perform(delete("/api/v1/order-items/" + orderItem.getId())
//                         .header("Authorization", token))
//                 .andExpect(status().isNoContent());
//     }

//     @Test
//     @DisplayName("Delete Order Item: Not Found (404)")
//     void deleteOrderItem_NotFound() throws Exception {
//         String token = generateTestToken(1L, "admin@test.com", "ADMIN");

//         mockMvc.perform(delete("/api/v1/order-items/99999")
//                         .header("Authorization", token))
//                 .andExpect(status().isNotFound());
//     }
// }
