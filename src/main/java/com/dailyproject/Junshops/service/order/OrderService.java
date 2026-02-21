package com.dailyproject.Junshops.service.order;

import com.dailyproject.Junshops.dto.OrderDto;
import com.dailyproject.Junshops.dto.OrderItemDto;
import com.dailyproject.Junshops.enums.OrderStatus;
import com.dailyproject.Junshops.exceptions.ResourceNotFoundException;
import com.dailyproject.Junshops.model.Cart;
import com.dailyproject.Junshops.model.Order;
import com.dailyproject.Junshops.model.OrderItem;
import com.dailyproject.Junshops.model.Product;
import com.dailyproject.Junshops.repository.OrderRepository;
import com.dailyproject.Junshops.repository.ProductRepository;
import com.dailyproject.Junshops.service.cart.CartService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public Order placeOrder(Long userId) {
        Cart cart = cartService.getCartByUserId(userId);

        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot place order with empty cart");
        }

        Order order = createOrder(cart);
        orderRepository.save(order);
        List<OrderItem> orderItemList = createOrderItems(order, cart);
        order.getOrderItems().addAll(orderItemList);
        order.setOrderItems(new HashSet<>(orderItemList));
        order.setTotalAmount(calculateTotalAmount(orderItemList));
        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(cart.getId());

        return savedOrder;
    }

    private Order createOrder(Cart cart){
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDate.now().atStartOfDay());
        return order;
    }

    private List<OrderItem> createOrderItems(Order order, Cart cart){
        return cart.getItems().stream().map(cartItem -> {
            Product product = cartItem.getProduct();
            product.setInventory(product.getInventory() - cartItem.getQuantity());
            productRepository.save(product);
            return new OrderItem(
                    order,
                    product,
                    cartItem.getQuantity(),
                    cartItem.getUnitPrice()
            );
        }).toList();
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> orderItemList){
        return orderItemList
                .stream()
                .map(item -> item.getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * ✅ UPDATED: Use findByIdWithItems to eagerly load order items
     *
     * WHY?
     * - Prevents lazy loading exception
     * - Loads order and items in one query
     * - Session stays open during entire fetch
     */
    @Override
    @Transactional  // ✅ Add @Transactional for safety
    public OrderDto getOrder(Long orderId) {
        return orderRepository.findByIdWithItems(orderId)  // ✅ Use new method
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    /**
     * ✅ UPDATED: Use findByUserIdWithItems to eagerly load all orders with items
     *
     * WHY?
     * - Loads all user's orders in one query
     * - Includes all order items and products
     * - Much more efficient than lazy loading
     * - Prevents N+1 query problem
     */
    @Override
    @Transactional
    public List<OrderDto> getUserOrders(Long userId){
        try {
            System.out.println("=== getUserOrders START ===");
            System.out.println("1. Fetching orders for userId: " + userId);

            List<Order> orders = orderRepository.findByUserIdWithItems(userId);

            System.out.println("2. Found " + orders.size() + " orders");

            if (orders.isEmpty()) {
                System.out.println("3. User has no orders yet");
                return List.of();  // Return empty list
            }

            System.out.println("3. Converting orders to DTOs...");
            List<OrderDto> dtos = orders.stream()
                    .map(order -> {
                        System.out.println("   - Converting order #" + order.getOrderId());
                        try {
                            return convertToDto(order);
                        } catch (Exception e) {
                            System.err.println("   ERROR converting order #" + order.getOrderId() + ": " + e.getMessage());
                            e.printStackTrace();
                            throw e;
                        }
                    })
                    .toList();

            System.out.println("4. Successfully converted " + dtos.size() + " orders");
            System.out.println("=== getUserOrders END ===");

            return dtos;

        } catch (Exception e) {
            System.err.println("=== ERROR in getUserOrders ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to load orders: " + e.getMessage(), e);
        }
    }

    /**
     * Manual conversion: Order → OrderDto
     *
     * NOW SAFE:
     * - Order items are already loaded (eager fetch)
     * - No lazy loading exceptions
     * - Can access all data
     */
    @Override
    public OrderDto convertToDto(Order order){
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        System.out.println("   Converting order: " + order.getOrderId());

        OrderDto dto = new OrderDto();

        // Set order ID
        dto.setOrderId(order.getOrderId());
        System.out.println("   - orderId: " + order.getOrderId());

        // Set order date
        dto.setOrderDate(order.getOrderDate());
        System.out.println("   - orderDate: " + order.getOrderDate());

        // Set total amount
        dto.setTotalAmount(order.getTotalAmount());
        System.out.println("   - totalAmount: " + order.getTotalAmount());

        // Set order status
        dto.setOrderStatus(order.getOrderStatus());
        System.out.println("   - orderStatus: " + order.getOrderStatus());

        // Set user ID (null check)
        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getId());
            System.out.println("   - userId: " + order.getUser().getId());
            dto.setUserFirstName(order.getUser().getFirstName());
            System.out.println("   - userId: " + order.getUser().getFirstName());
            dto.setUserLastName(order.getUser().getLastName());
            System.out.println("   - userLastName: " + order.getUser().getLastName());
            dto.setUserEmail(order.getUser().getEmail());
            System.out.println("   - userEmail: " + order.getUser().getEmail());
        } else {
            System.err.println("   WARNING: Order user is null!");
            dto.setUserId(null);
            dto.setUserFirstName(null);
            dto.setUserLastName(null);
            dto.setUserEmail(null);
        }

        // Convert order items (null check)
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            System.out.println("   - Converting " + order.getOrderItems().size() + " items");
            List<OrderItemDto> itemDtos = order.getOrderItems().stream()
                    .map(item -> {
                        try {
                            return convertOrderItemToDto(item);
                        } catch (Exception e) {
                            System.err.println("   ERROR converting order item: " + e.getMessage());
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(item -> item != null)  // Filter out nulls
                    .collect(Collectors.toList());

            dto.setOrderItems(itemDtos);
            System.out.println("   - Converted " + itemDtos.size() + " items successfully");
        } else {
            System.out.println("   - No items in this order");
            dto.setOrderItems(List.of());  // Empty list instead of null
        }

        System.out.println("   Order conversion complete!");

        return dto;
    }

    private OrderItemDto convertOrderItemToDto(OrderItem orderItem) {
        if (orderItem == null) {
            throw new IllegalArgumentException("OrderItem cannot be null");
        }

        OrderItemDto dto = new OrderItemDto();

        // Check if product exists (might have been deleted)
        if (orderItem.getProduct() == null) {
            System.err.println("     WARNING: OrderItem has no product! ItemId: " + orderItem.getId());
            // Set default values
            dto.setProductId(null);
            dto.setProductName("Product no longer available");
            dto.setProductBrand("Unknown");
        } else {
            // Normal conversion
            dto.setProductId(orderItem.getProduct().getId());
            dto.setProductName(orderItem.getProduct().getName());
            dto.setProductBrand(orderItem.getProduct().getBrand());
        }

        // Set quantity and price (should never be null)
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());

        return dto;
    }


    /**
     * Get ALL orders from ALL users (admin only)
     *
     * SECURITY NOTE:
     * - This method returns ALL orders
     * - Should only be called by admin endpoints
     * - @RolesAllowed annotation on controller enforces this
     *
     * PERFORMANCE:
     * - Eagerly loads order items
     * - Sorted by date (newest first)
     * - May be slow with many orders (consider pagination)
     */
    @Override
    @Transactional
    public List<OrderDto> getAllOrders() {
        // Get all orders with items eagerly loaded
        List<Order> orders = orderRepository.findAllWithItems();

        return orders.stream()
                .map(this::convertToDto)
                .toList();  // Already sorted by query
    }
}