package com.dailyproject.Junshops.client;

import com.dailyproject.Junshops.model.Category;
import com.dailyproject.Junshops.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryClient {

    private final WebClient webClient;
    private final AuthClient authClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get all categories
     */
    public List<Category> getAllCategories() {
        // Retrieves all categories; throws exception on failure
        try {
            ApiResponse response = webClient.get()
                    .uri("/categories/all")
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();

            return convertToCategoryList(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch categories: " + e.getMessage(), e);
        }
    }

        /**
         * Retrieves category by ID; throws exception on failure
         */
    /**
     * Get category by ID
     */
    public Category getCategoryById(Long id) {
        // Fetches category; throws exception on failure
        try {
            ApiResponse response = webClient.get()
                    .uri("/categories/category/{id}/category", id)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();

            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), Category.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch category: " + e.getMessage(), e);
        }
    }

    /**
     * Add new category (Admin only)
     */
    public Category addCategory(Category category) {
        String token = authClient.getToken();
        if (token == null) {
            throw new RuntimeException("Not authenticated");
        }

        // Posts category; returns result or throws exception
            // Posts category with token; retrieves API response
        try {
            // Posts category with token; retrieves API response
            ApiResponse response = webClient.post()
                    .uri("/categories/add")
                    .header("Authorization", "Bearer " + token)
                    .body(Mono.just(category), Category.class)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();

            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), Category.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to add category: " + e.getMessage(), e);
        }
    }

    /**
     * Update category (Admin only)
     */
    public Category updateCategory(Long id, Category category) {
        String token = authClient.getToken();
        if (token == null) {
            throw new RuntimeException("Not authenticated");
        }
        // Attempts authenticated category update; throws on failure


        try {
            // Updates category via authenticated request with token
            ApiResponse response = webClient.put()
                    .uri("/categories/category/{id}/update", id)
                    .header("Authorization", "Bearer " + token)
                    .body(Mono.just(category), Category.class)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();

            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), Category.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update category: " + e.getMessage(), e);
        }
    }

    /**
     * Delete category (Admin only)
     */
    public void deleteCategory(Long id) {
        String token = authClient.getToken();
        if (token == null) {
            throw new RuntimeException("Not authenticated");
        }

        // Deletes category; throws exception on failure
        try {
            webClient.delete()
                    .uri("/categories/category/{id}/delete", id)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete category: " + e.getMessage(), e);
        }
    }

    // Helper method
    private List<Category> convertToCategoryList(ApiResponse response) {
        // Converts generic data to list of categories
        if (response != null && response.getData() != null) {
            List<?> dataList = (List<?>) response.getData();
            List<Category> categories = new ArrayList<>();
            for (Object item : dataList) {
                categories.add(objectMapper.convertValue(item, Category.class));
            }
            return categories;
        }
        return new ArrayList<>();
    }
}