package com.innowise.orderservice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.innowise.orderservice.integration.AbstractIntegrationTest;
import com.innowise.orderservice.model.dto.request.OrderItemCreateRequest;
import com.innowise.orderservice.model.dto.request.OrderItemUpdateRequest;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class OrderItemControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    void create_shouldReturnCreatedOrderItem() throws Exception {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Order order = orderRepository.save(Order.builder()
                .userId(userId)
                .userEmail("buyer@example.com")
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.valueOf(50))
                .build());
        Item item = itemRepository.save(Item.builder()
                .name("Book")
                .price(BigDecimal.valueOf(25))
                .build());

        OrderItemCreateRequest request = OrderItemCreateRequest.builder()
                .itemId(item.getId())
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/v1/orders/{orderId}/items", order.getId())
                        .header(AUTH_HEADER, adminAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.orderId").value(order.getId()))
                .andExpect(jsonPath("$.itemId").value(item.getId()))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void getById_shouldReturnOrderItem() throws Exception {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        Order order = orderRepository.save(Order.builder()
                .userId(userId)
                .userEmail("buyer2@example.com")
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.valueOf(100))
                .build());
        Item item = itemRepository.save(Item.builder()
                .name("Pen")
                .price(BigDecimal.valueOf(5))
                .build());
        OrderItem orderItem = orderItemRepository.save(OrderItem.builder()
                .order(order)
                .item(item)
                .quantity(3)
                .build());

        mockMvc.perform(get("/api/v1/orders/{orderId}/items/{id}", order.getId(), orderItem.getId())
                        .header(AUTH_HEADER, adminAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderItem.getId()))
                .andExpect(jsonPath("$.orderId").value(order.getId()))
                .andExpect(jsonPath("$.itemId").value(item.getId()))
                .andExpect(jsonPath("$.quantity").value(3));
    }

    @Test
    void getAll_shouldReturnOrderItems() throws Exception {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000003");
        Order order = orderRepository.save(Order.builder()
                .userId(userId)
                .userEmail("buyer3@example.com")
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.valueOf(200))
                .build());
        Item item = itemRepository.save(Item.builder()
                .name("Notebook")
                .price(BigDecimal.valueOf(10))
                .build());
        orderItemRepository.save(OrderItem.builder()
                .order(order)
                .item(item)
                .quantity(1)
                .build());
        orderItemRepository.save(OrderItem.builder()
                .order(order)
                .item(item)
                .quantity(2)
                .build());

        mockMvc.perform(get("/api/v1/orders/{orderId}/items", order.getId())
                        .header(AUTH_HEADER, adminAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void update_shouldModifyOrderItem() throws Exception {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000004");
        Order order = orderRepository.save(Order.builder()
                .userId(userId)
                .userEmail("buyer4@example.com")
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.valueOf(75))
                .build());
        Item item = itemRepository.save(Item.builder()
                .name("Lamp")
                .price(BigDecimal.valueOf(35))
                .build());
        OrderItem orderItem = orderItemRepository.save(OrderItem.builder()
                .order(order)
                .item(item)
                .quantity(1)
                .build());

        OrderItemUpdateRequest request = OrderItemUpdateRequest.builder()
                .quantity(5)
                .build();

        mockMvc.perform(patch("/api/v1/orders/{orderId}/items/{id}", order.getId(), orderItem.getId())
                        .header(AUTH_HEADER, adminAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderItem.getId()))
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void delete_shouldRemoveOrderItem() throws Exception {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000005");
        Order order = orderRepository.save(Order.builder()
                .userId(userId)
                .userEmail("buyer5@example.com")
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.valueOf(300))
                .build());
        Item item = itemRepository.save(Item.builder()
                .name("Chair")
                .price(BigDecimal.valueOf(150))
                .build());
        OrderItem orderItem = orderItemRepository.save(OrderItem.builder()
                .order(order)
                .item(item)
                .quantity(2)
                .build());

        mockMvc.perform(delete("/api/v1/orders/{orderId}/items/{id}", order.getId(), orderItem.getId())
                        .header(AUTH_HEADER, adminAuthHeader()))
                .andExpect(status().isNoContent());
    }
}
