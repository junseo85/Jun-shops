package com.dailyproject.Junshops.service.product;

import com.dailyproject.Junshops.dto.ImageDto;
import com.dailyproject.Junshops.dto.ProductDto;
import com.dailyproject.Junshops.exceptions.AlreadyExistsException;
import com.dailyproject.Junshops.exceptions.ResourceNotFoundException;
import com.dailyproject.Junshops.model.Category;
import com.dailyproject.Junshops.model.Product;
import com.dailyproject.Junshops.repository.CategoryRepository;
import com.dailyproject.Junshops.repository.ImageRepository;
import com.dailyproject.Junshops.repository.ProductRepository;
import com.dailyproject.Junshops.request.AddProductRequest;
import com.dailyproject.Junshops.request.ProductUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final ImageRepository imageRepository;

    /**
     * ✅ CACHED: Get all products
     *
     * HOW IT WORKS:
     * 1st call: Query database → Store in Redis → Return
     * 2nd call: Return from Redis (fast!)
     *
     * CACHE KEY: "products::allProducts"
     * TTL: 1 hour (configured in CacheManager)
     */
    @Override
    //@Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'allProducts'")
    public List<Product> getAllProducts() {
        System.out.println("🔍 Cache MISS: Fetching all products from database");
        return productRepository.findAll();
    }

    /**
     * ✅ CACHED: Get product by ID
     *
     * CACHE KEY: "products::{id}"
     * Example: "products::123"
     *
     * WHY productId in key?
     * - Each product has unique cache entry
     * - Can invalidate individual products
     */
    @Override
    //@Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public Product getProductById(Long id) {
        System.out.println("🔍 Cache MISS: Fetching product " + id + " from database");
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
    }

    /**
     * ✅ CACHED: Get products by category
     *
     * CACHE KEY: "products::category_{categoryName}"
     * Example: "products::category_Electronics"
     */
    @Override
    //@Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'category_' + #category")
    public List<Product> getProductsByCategory(String category) {
        System.out.println("🔍 Cache MISS: Fetching products in category: " + category);
        return productRepository.findByCategoryName(category);
    }

    /**
     * ✅ CACHED: Get products by brand
     *
     * CACHE KEY: "products::brand_{brandName}"
     */
    @Override
    //@Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'brand_' + #brand")
    public List<Product> getProductsByBrand(String brand) {
        System.out.println("🔍 Cache MISS: Fetching products by brand: " + brand);
        return productRepository.findByBrand(brand);
    }

    /**
     * ✅ CACHED: Get products by category and brand
     *
     * CACHE KEY: "products::category_{category}_brand_{brand}"
     */
    @Override
    //@Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'category_' + #category + '_brand_' + #brand")
    public List<Product> getProductsByCategoryAndBrand(String category, String brand) {
        System.out.println("🔍 Cache MISS: Fetching products - Category: " + category + ", Brand: " + brand);
        return productRepository.findByCategoryNameAndBrand(category, brand);
    }

    /**
     * ✅ CACHED: Get products by name
     *
     * CACHE KEY: "products::name_{productName}"
     */
    @Override
    //@Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'name_' + #name")
    public List<Product> getProductsByName(String name) {
        System.out.println("🔍 Cache MISS: Fetching products by name: " + name);
        return productRepository.findByName(name);
    }

    /**
     * ✅ CACHED: Get products by brand and name
     *
     * CACHE KEY: "products::brand_{brand}_name_{name}"
     */
    @Override
    //@Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'brand_' + #brand + '_name_' + #name")
    public List<Product> getProductsByBrandAndName(String brand, String name) {
        System.out.println("🔍 Cache MISS: Fetching products - Brand: " + brand + ", Name: " + name);
        return productRepository.findByBrandAndName(brand, name);
    }

    /**
     * ✅ CACHED: Count products by brand and name
     *
     * CACHE KEY: "products::count_brand_{brand}_name_{name}"
     */
    @Override
    //@Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'count_brand_' + #brand + '_name_' + #name")
    public Long countProductsByBrandAndName(String brand, String name) {
        System.out.println("🔍 Cache MISS: Counting products - Brand: " + brand + ", Name: " + name);
        return productRepository.countByBrandAndName(brand, name);
    }



    @Override
    public ProductDto converToDto(Product product) {
        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setBrand(product.getBrand());
        productDto.setPrice(product.getPrice());
        productDto.setInventory(product.getInventory());
        productDto.setDescription(product.getDescription());

        // Map category
        if (product.getCategory() != null) {
            productDto.setCategoryName(product.getCategory().getName());
        }

        // Map images - now safe because they're eagerly fetched
        List<ImageDto> imageDtos = product.getImages().stream()
                .map(image -> {
                    ImageDto imageDto = new ImageDto();
                    imageDto.setId(image.getId());
                    imageDto.setFileName(image.getFileName());
                    imageDto.setDownloadUrl(image.getDownloadUrl());
                    return imageDto;
                })
                .collect(Collectors.toList());

        productDto.setImages(imageDtos);

        return productDto;
    }

    @Override
    public List<ProductDto> getConvertedProducts(List<Product> products) {
        return products.stream()
                .map(this::converToDto)
                .collect(Collectors.toList());
    }

    /**
     * ✅ CACHE EVICTION: Add product
     *
     * WHY @CacheEvict?
     * - New product added → "allProducts" cache is outdated
     * - Must clear cache to show new product
     *
     * allEntries=true: Clear ALL product caches
     * (Safe but aggressive - alternative: clear only relevant caches)
     */
    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public Product addProduct(AddProductRequest request) {
        System.out.println("🔄 Adding new product: " + request.getName());

        // Validate request
        if (request == null) {
            throw new IllegalArgumentException("Product request cannot be null");
        }

        if (request.getName() == null || request.getBrand() == null) {
            throw new IllegalArgumentException("Product name and brand are required");
        }

        // Check if product already exists
        if (productExists(request.getName(), request.getBrand())) {
            throw new AlreadyExistsException(
                    request.getBrand() + " " + request.getName() + " already exists"
            );
        }

        // ✅ FIX: Handle category properly (avoid detached entity)
        Category category;

        if (request.getCategory() == null) {
            throw new IllegalArgumentException("Category is required");
        }

        // If category has ID, load it from database
        if (request.getCategory().getId() != null) {
            category = categoryRepository.findById(request.getCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with ID: " + request.getCategory().getId()
                    ));
            System.out.println("✅ Loaded existing category: " + category.getName() + " (ID: " + category.getId() + ")");
        }
        // If category only has name, find or create
        else if (request.getCategory().getName() != null) {
            String categoryName = request.getCategory().getName();
            category = categoryRepository.findByName(categoryName);

            if (category == null) {
                // Create new category
                category = new Category(categoryName);
                category = categoryRepository.save(category);
                System.out.println("✅ Created new category: " + category.getName() + " (ID: " + category.getId() + ")");
            } else {
                System.out.println("✅ Found existing category: " + category.getName() + " (ID: " + category.getId() + ")");
            }
        }
        else {
            throw new IllegalArgumentException("Category must have either ID or name");
        }

        // Create product with managed category
        Product product = new Product(
                request.getName(),
                request.getBrand(),
                request.getPrice(),
                request.getInventory(),
                request.getDescription(),
                category  // ✅ This is now a MANAGED entity
        );

        // Save product
        Product savedProduct = productRepository.save(product);
        System.out.println("✅ Product saved successfully with ID: " + savedProduct.getId());

        return savedProduct;
    }

    /**
     * ✅ CACHE UPDATE: Update product
     *
     * @CachePut:
     * - Updates cache with new value
     * - Returns updated product
     * - Cache key matches the product ID
     *
     * ALSO: @CacheEvict for "allProducts"
     * - Updated product affects list queries
     */
    @Override
    //@Transactional
    @CachePut(value = "products", key = "#productId")
    @CacheEvict(value = "products", key = "'allProducts'")
    public Product updateProduct(ProductUpdateRequest request, Long productId) {
        System.out.println("🔄 Cache UPDATED: Updating product " + productId);
        return productRepository.findById(productId)
                .map(existingProduct -> updateExistingProduct(existingProduct, request))
                .map(productRepository::save)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
    }

    /**
     * ✅ CACHE EVICTION: Delete product
     *
     * allEntries=true:
     * - Deleted product affects all queries
     * - Clear everything to be safe
     */
    @Override
    //@Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProductById(Long id) {
        System.out.println("🗑️ Cache CLEARED: Deleting product " + id);
        productRepository.findById(id)
                .ifPresentOrElse(productRepository::delete,
                        () -> { throw new ResourceNotFoundException("Product not found!"); });
    }

    private boolean productExists(String name, String brand) {
        return productRepository.existsByNameAndBrand(name, brand);
    }

    private Product createProduct(AddProductRequest request, Category category) {
        return new Product(
                request.getName(),
                request.getBrand(),
                request.getPrice(),
                request.getInventory(),
                request.getDescription(),
                category
        );
    }

    private Product updateExistingProduct(Product existingProduct, ProductUpdateRequest request) {
        existingProduct.setName(request.getName());
        existingProduct.setBrand(request.getBrand());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setInventory(request.getInventory());
        existingProduct.setDescription(request.getDescription());

        Category category = categoryRepository.findByName(request.getCategory().getName());
        existingProduct.setCategory(category);
        return existingProduct;
    }
}