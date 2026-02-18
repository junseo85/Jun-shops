package com.dailyproject.Junshops.repository;

import com.dailyproject.Junshops.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.category")
    List<Product> findAll();

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findById(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.category WHERE p.category.name = :category")
    List<Product> findByCategoryName(@Param("category") String category);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.category WHERE p.brand = :brand")
    List<Product> findByBrand(@Param("brand") String brand);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.category WHERE p.category.name = :category AND p.brand = :brand")
    List<Product> findByCategoryNameAndBrand(@Param("category") String category, @Param("brand") String brand);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.category WHERE p.name LIKE %:name%")
    List<Product> findByName(@Param("name") String name);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.category WHERE p.brand = :brand AND p.name LIKE %:name%")
    List<Product> findByBrandAndName(@Param("brand") String brand, @Param("name") String name);

    Long countByBrandAndName(String brand, String name);

    boolean existsByNameAndBrand(String name, String brand);
}