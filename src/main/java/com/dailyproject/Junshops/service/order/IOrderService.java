package com.dailyproject.Junshops.service.order;

import com.dailyproject.Junshops.dto.OrderDto;
import com.dailyproject.Junshops.model.Order;
import jakarta.transaction.Transactional;

import java.util.List;

public interface IOrderService {

    @Transactional
    Order placeOrder(Long userId);

    OrderDto getOrder(Long orderId);

    List<OrderDto> getUserOrders(Long userId);

    //Get all orders(admin only)
    List<OrderDto> getAllOrders();

    OrderDto convertToDto(Order order);
}