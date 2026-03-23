package com.innowise.orderservice.model.entity;

public enum OrderStatus {
    NEW,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }
        if (this == targetStatus) {
            return true;
        }
        return switch (this) {
            case NEW -> targetStatus == PAID || targetStatus == CANCELLED;
            case PAID -> targetStatus == SHIPPED;
            case SHIPPED -> targetStatus == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
