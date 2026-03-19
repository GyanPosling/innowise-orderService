package com.innowise.orderservice.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "orders")
@SQLDelete(sql = "UPDATE orders SET deleted_at = now() WHERE id = ?")
@Filter(name = "softDelete", condition = "deleted_at is null or :includeDeleted = true")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Order extends Auditable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "user_email", length = 100)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Filter(name = "softDelete", condition = "deleted_at is null or :includeDeleted = true")
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "order_payment_events", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "payment_id", nullable = false, length = 255)
    @Builder.Default
    private Set<String> processedPaymentIds = new HashSet<>();
}
