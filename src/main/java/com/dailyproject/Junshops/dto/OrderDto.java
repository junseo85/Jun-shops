package com.dailyproject.Junshops.dto;

import com.dailyproject.Junshops.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Order
 *
 * WHY DTO?
 * - Separates internal model from external API
 * - Controls what data is exposed to UI
 * - Prevents accidental exposure of sensitive data
 */
@Data  // ← CRITICAL: Generates getters, setters, equals, hashCode, toString
public class OrderDto {

    /**
     * Order ID from database
     *
     * WHY: Unique identifier to track orders
     */
    private Long orderId;

    /**
     * When the order was placed
     *
     * WHY: Display to user, sort orders, generate reports
     */
    private LocalDateTime orderDate;

    /**
     * Total cost of the order
     *
     * WHY: Show user how much they paid
     * NOTE: Uses BigDecimal for precise money calculations
     */
    private BigDecimal totalAmount;

    /**
     * Current status of the order
     *
     * WHY: User needs to know if order is pending, shipped, delivered
     * VALUES: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
     */
    private OrderStatus orderStatus;

    /**
     * List of items in this order
     *
     * WHY: Show user what they ordered
     * NOTE: Each item has product details, quantity, price
     */
    private List<OrderItemDto> orderItems;

    /**
     * User who placed the order
     *
     * WHY: Link order to user (optional to display in UI)
     */
    private Long userId;

    private String userFirstName;
    private String userLastName;
    private String userEmail;
}