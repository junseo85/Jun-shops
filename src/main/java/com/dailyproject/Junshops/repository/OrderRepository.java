package com.dailyproject.Junshops.repository;

import com.dailyproject.Junshops.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * OrderRepository - Data access for Order entities
 *
 * CUSTOM QUERIES:
 * - JOIN FETCH eagerly loads relationships
 * - Prevents lazy loading exceptions
 * - More efficient (one query instead of N+1)
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find orders by user ID
     *
     * NOTE: This uses default lazy loading
     * Use findByUserIdWithItems() to eagerly load items
     */
    List<Order> findByUserId(Long userId);

    /**
     * ✅ Find order by ID with items eagerly loaded
     *
     * JOIN FETCH:
     * - Loads orderItems in same query
     * - Loads each item's product too
     * - Prevents lazy loading exceptions
     *
     * WHY?
     * - We ALWAYS need items when displaying orders
     * - Avoids N+1 query problem
     * - One query instead of 1 + N queries
     */
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems items " +
            "LEFT JOIN FETCH items.product " +
            "WHERE o.orderId = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);

    /**
     * ✅ Find orders by user ID with items eagerly loaded
     *
     * DISTINCT:
     * - Needed because JOIN FETCH can create duplicates
     * - If order has 3 items, query returns 3 rows
     * - DISTINCT removes duplicate orders
     *
     * WHY?
     * - Load all user's orders with items in one query
     * - Much more efficient than lazy loading
     */
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems items " +
            "LEFT JOIN FETCH items.product " +
            "WHERE o.user.id = :userId " +
            "ORDER BY o.orderDate DESC")
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);
    // Add this method to OrderRepository.java

    /**
     * Find ALL orders with items eagerly loaded
     *
     * ADMIN ONLY:
     * - Returns all orders in system
     * - Includes all order items and products
     * - Sorted by date (newest first)
     *
     * WARNING:
     * - Can be slow with many orders
     * - Consider pagination for production
     */
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems items " +
            "LEFT JOIN FETCH items.product " +
            "ORDER BY o.orderDate DESC")
    List<Order> findAllWithItems();
}