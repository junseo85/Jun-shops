package com.dailyproject.Junshops.client;

import com.dailyproject.Junshops.dto.ProductDto;
import com.dailyproject.Junshops.request.AddProductRequest;
import com.dailyproject.Junshops.request.ProductUpdateRequest;
import com.dailyproject.Junshops.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;  // ✅ Changed
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductClient {

    private final WebClient webClient;
    private final AuthClient authClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get all products
     */
    public List<ProductDto> getAllProducts() {
        // Fetches all products; throws exception on failure
        try {
            ApiResponse response = webClient.get()
                    .uri("/products/all")
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();

            return convertToProductList(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch products: " + e.getMessage(), e);
        }
    }

    /**
     * Get product by ID
     */
    public ProductDto getProductById(Long productId) {
        // Fetches product by ID; throws exception on failure
        try {
            ApiResponse response = webClient.get()
                    .uri("/products/product/{productId}/product", productId)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();

            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), ProductDto.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch product: " + e.getMessage(), e);
        }
    }

    /**
     * Search products by name
     */
    public List<ProductDto> searchProductsByName(String name) {
        try {
            ApiResponse response = webClient.get()
                    .uri("/products/{name}/products", name)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();

            return convertToProductList(response);
        } catch (Exception e) {
            return new ArrayList<>(); // Return empty list if not found
        }
    }

    /**
     * Get products by category
     */
    public List<ProductDto> getProductsByCategory(String category) {
        // Gets products by category; returns empty list on error
        try {
            ApiResponse response = webClient.get()
                    .uri("/products/product/{category}/all/products", category)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();

            return convertToProductList(response);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Get products by brand
     */
    public List<ProductDto> getProductsByBrand(String brand) {
        try {
            // Retrieves API response for products by brand
            ApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/products/product/by-brand")
                            .queryParam("brand", brand)
                            .build())
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();

            return convertToProductList(response);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Add new product (Admin only)
     */
    public ProductDto addProduct(AddProductRequest request) {
        String token = authClient.getToken();
        if (token == null) {
            throw new RuntimeException("Not authenticated");
        }

        // Attempts auditable product creation; throws on failure
            // Posts product creation request with authentication token
        try {
            // Posts product creation request with authentication token
            ApiResponse response = webClient.post()
                    .uri("/products/add")
                    .header("Authorization", "Bearer " + token)
                    .body(Mono.just(request), AddProductRequest.class)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();

            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), ProductDto.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to add product: " + e.getMessage(), e);
        }
    }

    /**
     * Update product (Admin only)
     */
    public ProductDto updateProduct(Long productId, ProductUpdateRequest request) {
        String token = authClient.getToken();
        if (token == null) {
            throw new RuntimeException("Not authenticated");
        }

        // Attempts product update; throws exception on failure
            // Updates product via authenticated web request
        try {
            // Updates product via authenticated web request
            ApiResponse response = webClient.put()
                    .uri("/products/product/{productId}/update", productId)
                    .header("Authorization", "Bearer " + token)
                    .body(Mono.just(request), ProductUpdateRequest.class)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();

            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), ProductDto.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update product: " + e.getMessage(), e);
        }
    }

    /**
     * Delete product (Admin only)
     */
    public void deleteProduct(Long productId) {
        String token = authClient.getToken();
        if (token == null) {
            throw new RuntimeException("Not authenticated");
        }

        // Attempts product deletion; throws exception on failure
        try {
            webClient.delete()
                    .uri("/products/product/{productId}/delete", productId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete product: " + e.getMessage(), e);
        }
    }

    // Helper method to convert API response to list of ProductDto
    private List<ProductDto> convertToProductList(ApiResponse response) {
        // Converts API response data to product list
        if (response != null && response.getData() != null) {
            List<?> dataList = (List<?>) response.getData();
            List<ProductDto> products = new ArrayList<>();
            for (Object item : dataList) {
                products.add(objectMapper.convertValue(item, ProductDto.class));
            }
            return products;
        }
        return new ArrayList<>();
    }
}