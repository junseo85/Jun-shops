package com.dailyproject.Junshops.repository;

import com.dailyproject.Junshops.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    //Eagerly fetch user with cart
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.cart WHERE u.email = :email")
    Optional<User> findByEmailWithCart(@Param("email") String email);
}
