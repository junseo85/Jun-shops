package com.dailyproject.Junshops.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * Data Transfer Object for OrderItem
 *
 * WHY?
 * - Represents one line item in an order
 * - Shows product details at time of purchase
 */
@Data  // ← CRITICAL!
public class OrderItemDto {

    /**
     * Product ID
     * WHY: Link to product for details, images, etc.
     */
    private Long productId;

    /**
     * Product name at time of order
     * WHY: Even if product is deleted, we still show name
     */
    private String productName;

    /**
     * Product brand at time of order
     * WHY: Show brand in order history
     */
    private String productBrand;

    /**
     * Quantity ordered
     * WHY: Show how many user bought
     */
    private int quantity;

    /**
     * Price per unit at time of order (SNAPSHOT)
     * WHY: Historical accuracy - price might change later
     */
    private BigDecimal price;
}