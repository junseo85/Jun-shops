package com.dailyproject.Junshops.service.cart;

import com.dailyproject.Junshops.model.Cart;
import com.dailyproject.Junshops.model.User;

import java.math.BigDecimal;

public interface ICartService {

    Cart getCart(Long id);
    void clearCart(Long id);
    BigDecimal getTotalPrice(Long id);




    Cart initializeNewCart(User user);

    Cart getCartByUserId(Long userId);
}
