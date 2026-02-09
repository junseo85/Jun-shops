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

    OrderDto convertToDto(Order order);
}
