package com.innowise.orderservice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.innowise.orderservice.integration.AbstractIntegrationTest;
import com.innowise.orderservice.model.dto.request.OrderCreateRequest;
import com.innowise.orderservice.model.dto.request.OrderItemRequest;
import com.innowise.orderservice.model.dto.request.OrderUpdateRequest;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class OrderControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void create_shouldReturnCreatedOrder() throws Exception {
        Item item = itemRepository.save(Item.builder()
                .name("Phone")
                .price(BigDecimal.valueOf(500))
                .build());
        stubUserByEmail("buyer@example.com");

        OrderCreateRequest request = OrderCreateRequest.builder()
                .userId(10L)
                .userEmail("buyer@example.com")
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.valueOf(500))
                .items(List.of(OrderItemRequest.builder()
                        .itemId(item.getId())
                        .quantity(1)
                        .build()))
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .header(AUTH_HEADER, adminAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.userEmail").value("buyer@example.com"))
                .andExpect(jsonPath("$.user.email").value("buyer@example.com"))
                .andExpect(jsonPath("$.items[0].itemId").value(item.getId()))
                .andExpect(jsonPath("$.items[0].quantity").value(1));
    }

    @Test
    void getById_shouldReturnOrder() throws Exception {
        Item item = itemRepository.save(Item.builder()
                .name("Headphones")
                .price(BigDecimal.valueOf(150))
                .build());
        Order order = Order.builder()
                .userId(11L)
                .userEmail("buyer11@example.com")
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.valueOf(150))
                .build();
        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .item(item)
                .quantity(1)
                .build();
        order.setOrderItems(new java.util.ArrayList<>(List.of(orderItem)));
        Order saved = orderRepository.save(order);
        stubUserByEmail("buyer11@example.com");

        mockMvc.perform(get("/api/v1/orders/{id}", saved.getId())
                        .header(AUTH_HEADER, adminAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.userEmail").value("buyer11@example.com"))
                .andExpect(jsonPath("$.user.email").value("buyer11@example.com"))
                .andExpect(jsonPath("$.items[0].itemId").value(item.getId()));
    }

    @Test
    void getAll_shouldReturnPage() throws Exception {
        Item item = itemRepository.save(Item.builder()
                .name("Tablet")
                .price(BigDecimal.valueOf(300))
                .build());

        Order orderOne = Order.builder()
                .userId(12L)
                .userEmail("buyer12@example.com")
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.valueOf(300))
                .build();
        OrderItem itemOne = OrderItem.builder()
                .order(orderOne)
                .item(item)
                .quantity(1)
                .build();
        orderOne.setOrderItems(new java.util.ArrayList<>(List.of(itemOne)));
        orderRepository.save(orderOne);

        Order orderTwo = Order.builder()
                .userId(13L)
                .userEmail("buyer13@example.com")
                .status(OrderStatus.PAID)
                .totalPrice(BigDecimal.valueOf(600))
                .build();
        OrderItem itemTwo = OrderItem.builder()
                .order(orderTwo)
                .item(item)
                .quantity(2)
                .build();
        orderTwo.setOrderItems(new java.util.ArrayList<>(List.of(itemTwo)));
        orderRepository.save(orderTwo);

        stubUserByEmail("buyer12@example.com");
        stubUserByEmail("buyer13@example.com");

        mockMvc.perform(get("/api/v1/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .header(AUTH_HEADER, adminAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void getByUserId_shouldReturnOrdersForUser() throws Exception {
        Item item = itemRepository.save(Item.builder()
                .name("Speaker")
                .price(BigDecimal.valueOf(75))
                .build());
        Order order = Order.builder()
                .userId(20L)
                .userEmail("buyer20@example.com")
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.valueOf(75))
                .build();
        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .item(item)
                .quantity(1)
                .build();
        order.setOrderItems(new java.util.ArrayList<>(List.of(orderItem)));
        orderRepository.save(order);
        stubUserByEmail("buyer20@example.com");

        mockMvc.perform(get("/api/v1/orders/user/{userId}", 20L)
                        .header(AUTH_HEADER, userAuthHeader(20L, "buyer20@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userEmail").value("buyer20@example.com"));
    }

    @Test
    void update_shouldModifyOrder() throws Exception {
        Item item = itemRepository.save(Item.builder()
                .name("Watch")
                .price(BigDecimal.valueOf(200))
                .build());
        Item newItem = itemRepository.save(Item.builder()
                .name("Watch Pro")
                .price(BigDecimal.valueOf(250))
                .build());
        Order order = Order.builder()
                .userId(14L)
                .userEmail("buyer14@example.com")
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.valueOf(200))
                .build();
        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .item(item)
                .quantity(1)
                .build();
        order.setOrderItems(new java.util.ArrayList<>(List.of(orderItem)));
        Order saved = orderRepository.save(order);
        stubUserByEmail("buyer14@example.com");

        OrderUpdateRequest request = OrderUpdateRequest.builder()
                .status(OrderStatus.PAID)
                .totalPrice(BigDecimal.valueOf(250))
                .items(List.of(OrderItemRequest.builder()
                        .itemId(newItem.getId())
                        .quantity(1)
                        .build()))
                .build();

        mockMvc.perform(put("/api/v1/orders/{id}", saved.getId())
                        .header(AUTH_HEADER, adminAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.items[0].itemId").value(newItem.getId()));
    }

    @Test
    void delete_shouldRemoveOrder() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .userId(15L)
                .userEmail("buyer15@example.com")
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.valueOf(100))
                .build());

        mockMvc.perform(delete("/api/v1/orders/{id}", order.getId())
                        .header(AUTH_HEADER, adminAuthHeader()))
                .andExpect(status().isNoContent());
    }
}
