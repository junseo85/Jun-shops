package com.dailyproject.Junshops.client;

import com.dailyproject.Junshops.dto.OrderDto;
import com.dailyproject.Junshops.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderClient {

    private final WebClient webClient;
    private final AuthClient authClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Place an order
     */
    public OrderDto placeOrder(Long userId) {
        String token = authClient.getToken();
        if (token == null) {
            throw new RuntimeException("Not authenticated");
        }

        // Places order; converts response; handles exceptions
        try {
            // Posts order creation request with user and auth details
            ApiResponse response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/orders/order")
                            .queryParam("userId", userId)
                            .build())
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();

            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), OrderDto.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to place order: " + e.getMessage(), e);
        }
    }

    /**
     * Get order by ID
     */
    public OrderDto getOrderById(Long orderId) {
        String token = authClient.getToken();
        if (token == null) {
            throw new RuntimeException("Not authenticated");
        }

        // Retrieves order; converts response; handles exceptions
        try {
            ApiResponse response = webClient.get()
                    .uri("/orders/{orderId}/order", orderId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();

            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), OrderDto.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch order: " + e.getMessage(), e);
        }
    }

    /**
     * Get all orders for a user
     */
    public List<OrderDto> getUserOrders(Long userId) {
        String token = authClient.getToken();
        if (token == null) {
            throw new RuntimeException("Not authenticated");
        }

        // Retrieves orders; throws exception on failure
        try {
            ApiResponse response = webClient.get()
                    .uri("/orders/{userId}/order", userId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();

            return convertToOrderList(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch orders: " + e.getMessage(), e);
        }
    }

    // Helper method
    private List<OrderDto> convertToOrderList(ApiResponse response) {
        // Converts API response data to list of orders
        if (response != null && response.getData() != null) {
            List<?> dataList = (List<?>) response.getData();
            List<OrderDto> orders = new ArrayList<>();
            for (Object item : dataList) {
                orders.add(objectMapper.convertValue(item, OrderDto.class));
            }
            return orders;
        }
        return new ArrayList<>();
    }
}