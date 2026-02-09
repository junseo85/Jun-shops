package com.dailyproject.Junshops.service.cart;

import com.dailyproject.Junshops.exceptions.ResourceNotFoundException;
import com.dailyproject.Junshops.model.Cart;
import com.dailyproject.Junshops.model.CartItem;
import com.dailyproject.Junshops.model.Product;
import com.dailyproject.Junshops.repository.CartItemRepository;
import com.dailyproject.Junshops.repository.CartRepository;
import com.dailyproject.Junshops.service.product.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartItemService implements ICartItemService{
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final IProductService productService;
    private final ICartService cartService;
    @Override
    public void addItemToCart(Long cartId, Long productId, int quantity) {
        //1. get the cart
        //2. get the product
        //3. check if the product already in the cart
        //4. If Yes, then increase the quantity with the requested quantity
        //5. If No, then initiate a new CartItem entry
        Cart cart = cartService.getCart(cartId);
        Product product = productService.getProductById(productId);
        // Attempts to locate existing item in cart
        CartItem cartItem = cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
        // Updates quantity or initiates new cart item
                .findFirst().orElse(new CartItem());
        // Updates quantity or initiates new cart item
        if (cartItem.getId() == null){
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setUnitPrice(product.getPrice());

        } else{
            cartItem.setQuantity(cartItem.getQuantity()+quantity);
        }
        cartItem.setTotalPrice();
        cart.addItem(cartItem);
        cartItemRepository.save(cartItem);
        cartRepository.save(cart);

    }
    @Transactional
    @Override
    public void removeItemFromCart(Long cartId, Long productId) {
        Cart cart = cartService.getCart(cartId);
        //CartItem itemToRemove = getCartItem(cartId, productId);
        // 2. Find the item within the cart's set to avoid redundant DB calls
        CartItem itemToRemove = cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in cart"));

        cart.removeItem(itemToRemove);
        //cartRepository.save(cart);
    }
    @Transactional
    @Override
    public void updateItemQuantity(Long cartId, Long productId, int quantity) {
        Cart cart = cartService.getCart(cartId);

        // 1. Handle removal if quantity is 0 or less
        if (quantity <= 0) {
            removeItemFromCart(cartId, productId);
            return;
        }

        // 2. Find the item or throw exception if not found
        CartItem item = cart.getItems()
                .stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        // 3. Optional: Check inventory
        if (item.getProduct().getInventory() < quantity) {
            throw new IllegalArgumentException("Not enough stock available");
        }

        // 4. Update item details
        item.setQuantity(quantity);
        item.setUnitPrice(item.getProduct().getPrice());
        item.setTotalPrice();

        // 5. Use the existing logic in the Cart entity
        cart.updateTotalAmount();

        cartRepository.save(cart);
    }
    @Override
    public CartItem getCartItem(Long cartId, Long productId){
        Cart cart = cartService.getCart(cartId);
        // Finds cart item matching product or throws exception
        return cart.getItems()
                .stream().filter(item -> item.getProduct().getId().equals(productId))
                .findFirst().orElseThrow(()-> new ResourceNotFoundException("Product not found"));
    }
}
