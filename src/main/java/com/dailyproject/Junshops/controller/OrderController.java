package com.dailyproject.Junshops.controller;

import com.dailyproject.Junshops.dto.OrderDto;
import com.dailyproject.Junshops.exceptions.ResourceNotFoundException;
import com.dailyproject.Junshops.model.Order;
import com.dailyproject.Junshops.response.ApiResponse;
import com.dailyproject.Junshops.service.order.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for order operations (placing an order and retrieving orders).
 *
 * <p>All business rules are handled by {@link IOrderService}; this controller focuses on
 * request/response mapping and HTTP status codes.</p>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/orders")
public class OrderController {
    private final IOrderService orderService;

    /**
     * Places an order for the given user based on their current cart.
     *
     * <p>Side effects typically include: decreasing inventory, creating order items,
     * and clearing the user's cart.</p>
     *
     * @param userId user placing the order
     * @return API response containing an {@link OrderDto} on success
     */
    @PostMapping("/order")
    public ResponseEntity<ApiResponse> createOrder(@RequestParam Long userId) {
        try {
            Order order = orderService.placeOrder(userId);
            OrderDto orderDto = orderService.convertToDto(order);
            return ResponseEntity.ok(new ApiResponse("Order placed successfully!", orderDto));
        } catch (Exception e) {
            // Note: consider mapping known exceptions to specific HTTP statuses in a GlobalExceptionHandler.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Error placing order!", e.getMessage()));
        }
    }

    /**
     * Retrieves a single order by its id.
     *
     * @param orderId order identifier
     * @return API response containing an {@link OrderDto}
     */
    @GetMapping("/{orderId}/order")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable Long orderId) {
        try {
            OrderDto order = orderService.getOrder(orderId);
            return ResponseEntity.ok(new ApiResponse("Order found!", order));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Oopps", e.getMessage()));
        }
    }

    /**
     * Retrieves all orders for a given user.
     *
     * @param userId user identifier
     * @return API response containing a list of {@link OrderDto}
     */
    @GetMapping("/{userId}/order")
    public ResponseEntity<ApiResponse> getUserOrders(@PathVariable Long userId) {
        try {
            List<OrderDto> order = orderService.getUserOrders(userId);
            return ResponseEntity.ok(new ApiResponse("Order found!", order));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Oopps", e.getMessage()));
        }
    }
}
