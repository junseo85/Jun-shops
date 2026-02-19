package com.dailyproject.Junshops.service.cart;

import com.dailyproject.Junshops.exceptions.ResourceNotFoundException;
import com.dailyproject.Junshops.model.Cart;
import com.dailyproject.Junshops.model.User;
import com.dailyproject.Junshops.repository.CartItemRepository;
import com.dailyproject.Junshops.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AtomicLong cartIdGenerator = new AtomicLong(0);

    @Override
    public Cart getCart(Long id) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        cart.updateTotalAmount();
        return cartRepository.save(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public Cart getCartWithItems(Long id) {
        return cartRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }


    @Override
    @Transactional(readOnly = true)
    public Cart getCartByUserIdWithItems(Long userId) {
        return cartRepository.findByUserIdWithItems(userId).orElse(null);
    }

    @Transactional
    @Override
    public void clearCart(Long id) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cartItemRepository.deleteAllByCartId(id);
        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        cartRepository.save(cart);
    }

    @Override
    public BigDecimal getTotalPrice(Long id) {
        Cart cart = getCart(id);
        return cart.getTotalAmount();
    }

    @Override
    public Cart initializeNewCart(User user) {
        return Optional.ofNullable(getCartByUserId(user.getId()))
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    @Override
    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }
}