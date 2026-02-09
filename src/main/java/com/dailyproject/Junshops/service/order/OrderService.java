package com.dailyproject.Junshops.service.order;

import com.dailyproject.Junshops.dto.OrderDto;
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

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService{
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final ModelMapper modelMapper;
    @Transactional
    @Override
    public Order placeOrder(Long userId) {
        Cart cart = cartService.getCartByUserId(userId);

        Order order = createOrder(cart);
        orderRepository.save(order);//ensure order has id
        List<OrderItem> orderItemList = createOrderItems(order, cart);
        order.getOrderItems().addAll(orderItemList);//add order items to order
        order.setOrderItems(new HashSet<>(orderItemList));
        order.setTotalAmount(calculateTotalAmount(orderItemList));
        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(cart.getId());

        return savedOrder;
    }

    /**
     * Creates order; sets user, status, and date
     */
    private Order createOrder(Cart cart){
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDate.now().atStartOfDay());
        return order;
    }
    private List<OrderItem> createOrderItems(Order order, Cart cart){
        // Creates order items; updates product inventory; persists changes
        return cart.getItems().stream().map(cartItem ->{
            Product product = cartItem.getProduct();
            product.setInventory(product.getInventory()-cartItem.getQuantity());
            productRepository.save(product);
            return new OrderItem(
                    order,
                    product,
                    cartItem.getQuantity(),
                    cartItem.getUnitPrice());
        }).toList();
    }

    /**
     * Calculates total amount from order items
     */
    private BigDecimal calculateTotalAmount(List<OrderItem> orderItemList){
        return orderItemList
                .stream()
                .map(item -> item.getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public OrderDto getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .map(this :: convertToDto)
                .orElseThrow(()-> new ResourceNotFoundException("Order not found"));
    }

    @Override
    public List<OrderDto> getUserOrders(Long userId){
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream().map(this :: convertToDto).toList();
    }

    @Override
    public OrderDto convertToDto(Order order){
        return modelMapper.map(order, OrderDto.class);
    }
}
