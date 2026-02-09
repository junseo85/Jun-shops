package com.dailyproject.Junshops.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Order line item entity.
 *
 * <p>Represents a snapshot of a product purchase within an {@link Order}:
 * quantity and price are stored so the order remains historically correct
 * even if the product price changes later.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
public class OrderItem {

    /** Primary key for the order item row. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Quantity of the product purchased in this order line. */
    private int quantity;

    /** Unit price at the time of ordering (snapshot). */
    private BigDecimal price;

    /**
     * Owning order.
     *
     * <p>Many items belong to one order.</p>
     */
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    /**
     * Product purchased.
     *
     * <p>Stored as a relation for navigation; business logic should treat price as snapshot.</p>
     */
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    /**
     * Convenience constructor used when creating order items from cart items.
     *
     * @param order owning order
     * @param product purchased product
     * @param quantity quantity purchased
     * @param price unit price captured at order time
     */
    public OrderItem(Order order, Product product, int quantity, BigDecimal price) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }
}
