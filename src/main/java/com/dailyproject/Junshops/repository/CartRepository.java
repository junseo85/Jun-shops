package com.dailyproject.Junshops.repository;

import com.dailyproject.Junshops.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findByUserId(Long userId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items items LEFT JOIN FETCH items.product WHERE c.id = :id")
    Optional<Cart> findByIdWithItems(@Param("id") Long id);


    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items items LEFT JOIN FETCH items.product WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);
}