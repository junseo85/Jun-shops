package com.dailyproject.Junshops.client;

import com.dailyproject.Junshops.model.Cart;
import com.dailyproject.Junshops.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartClient {

    private final WebClient webClient;
    private final AuthClient authClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get cart by ID
     */
    public Cart getCart(Long cartId) {
        // Fetches cart by ID; throws exception on failure
        try {
            ApiResponse response = webClient.get()
                    .uri("/carts/{cartId}/my-cart", cartId)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();

            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), Cart.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch cart: " + e.getMessage(), e);
        }
    }

    /**
     * Add item to cart
     */
    public void addItemToCart(Long productId, Integer quantity) {
        String token = authClient.getToken();
        if (token == null) {
            throw new RuntimeException("Not authenticated");
        }

        try {
            // Adds item to cart using authenticated request
            webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/cartItems/item/add")
                            .queryParam("productId", productId)
                            .queryParam("quantity", quantity)
                            .build())
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to add item to cart: " + e.getMessage(), e);
        }
    }

    /**
     * Remove item from cart
     */
    public void removeItemFromCart(Long cartId, Long itemId) {
        String token = authClient.getToken();
        if (token == null) {
            throw new RuntimeException("Not authenticated");
        }

        // Removes item; throws exception on failure
        try {
            webClient.delete()
                    .uri("/cartItems/cart/{cartId}/item/{itemId}/remove", cartId, itemId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove item: " + e.getMessage(), e);
        }
    }

    /**
     * Update item quantity
     */
    public void updateItemQuantity(Long cartId, Long itemId, Integer quantity) {
        String token = authClient.getToken();
        if (token == null) {
            throw new RuntimeException("Not authenticated");
        }

        try {
            // Updates item quantity; includes authentication header
            webClient.put()
                    .uri(uriBuilder -> uriBuilder
                            .path("/cartItems/cart/{cartId}/item/{itemId}/update")
                            .queryParam("quantity", quantity)
                            .build(cartId, itemId))
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update quantity: " + e.getMessage(), e);
        }
    }

    /**
     * Clear cart
     */
    public void clearCart(Long cartId) {
        String token = authClient.getToken();
        if (token == null) {
            throw new RuntimeException("Not authenticated");
        }

        // Clears cart via authenticated request; handles exceptions
        try {
            webClient.delete()
                    .uri("/carts/{cartId}/clear", cartId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear cart: " + e.getMessage(), e);
        }
    }

    /**
     * Get cart total price
     */
    public BigDecimal getCartTotalPrice(Long cartId) {
        // Fetches cart total price; handles exceptions
        try {
            ApiResponse response = webClient.get()
                    .uri("/carts/{cartId}/cart/total-price", cartId)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();

            // Extracts total price from response data
            if (response != null && response.getData() != null) {
                return new BigDecimal(response.getData().toString());
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch total: " + e.getMessage(), e);
        }
    }
}