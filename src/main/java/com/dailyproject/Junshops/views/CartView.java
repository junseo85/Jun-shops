package com.dailyproject.Junshops.views;

import com.dailyproject.Junshops.model.Cart;
import com.dailyproject.Junshops.model.CartItem;
import com.dailyproject.Junshops.model.User;
import com.dailyproject.Junshops.security.user.ShopUserDetails;
import com.dailyproject.Junshops.service.cart.ICartItemService;
import com.dailyproject.Junshops.service.cart.ICartService;
import com.dailyproject.Junshops.service.user.IUserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;

@Route(value = "cart", layout = MainLayout.class)
@PermitAll
public class CartView extends VerticalLayout {

    private final ICartService cartService;
    private final ICartItemService cartItemService;
    private final IUserService userService;
    private final AuthenticationContext authenticationContext;

    private final Grid<CartItem> grid = new Grid<>(CartItem.class, false);
    private final Span totalAmountSpan = new Span();
    private Cart currentCart;

    public CartView(ICartService cartService, ICartItemService cartItemService, 
                    IUserService userService, AuthenticationContext authenticationContext) {
        this.cartService = cartService;
        this.cartItemService = cartItemService;
        this.userService = userService;
        this.authenticationContext = authenticationContext;

        addClassName("cart-view");
        setSizeFull();

        add(new H2("Shopping Cart"));
        configureGrid();
        add(grid);
        add(createFooter());
        add(createButtons());

        loadCart();
    }

    private void configureGrid() {
        grid.addClassName("cart-grid");
        grid.setSizeFull();
        
        grid.addColumn(item -> item.getProduct().getName()).setHeader("Product");
        grid.addColumn(item -> item.getProduct().getBrand()).setHeader("Brand");
        grid.addColumn(CartItem::getQuantity).setHeader("Quantity");
        grid.addColumn(CartItem::getUnitPrice).setHeader("Unit Price");
        grid.addColumn(CartItem::getTotalPrice).setHeader("Total Price");
        
        grid.addComponentColumn(item -> {
            Button removeButton = new Button("Remove");
            removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            removeButton.addClickListener(e -> removeItem(item));
            return removeButton;
        }).setHeader("Actions");

        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private HorizontalLayout createFooter() {
        totalAmountSpan.getStyle().set("font-size", "1.5em").set("font-weight", "bold");
        HorizontalLayout footer = new HorizontalLayout(new Span("Total: "), totalAmountSpan);
        footer.setAlignItems(Alignment.CENTER);
        return footer;
    }

    private HorizontalLayout createButtons() {
        Button clearCart = new Button("Clear Cart");
        clearCart.addThemeVariants(ButtonVariant.LUMO_ERROR);
        clearCart.addClickListener(e -> clearCart());

        Button checkout = new Button("Checkout");
        checkout.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        checkout.addClickListener(e -> checkout());

        return new HorizontalLayout(clearCart, checkout);
    }

    private void loadCart() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                currentCart = cartService.getCartByUserId(currentUser.getId());
                if (currentCart != null) {
                    grid.setItems(currentCart.getItems());
                    updateTotal();
                }
            }
        } catch (Exception e) {
            Notification.show("Error loading cart: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateTotal() {
        if (currentCart != null) {
            BigDecimal total = cartService.getTotalPrice(currentCart.getId());
            totalAmountSpan.setText("$" + total.toString());
        }
    }

    private void removeItem(CartItem item) {
        try {
            cartItemService.removeItemFromCart(currentCart.getId(), item.getProduct().getId());
            loadCart();
            Notification.show("Item removed from cart")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Error removing item: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void clearCart() {
        try {
            cartService.clearCart(currentCart.getId());
            loadCart();
            Notification.show("Cart cleared successfully")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Error clearing cart: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void checkout() {
        Notification.show("Checkout functionality coming soon!")
                .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
    }

    private User getCurrentUser() {
        return authenticationContext.getAuthenticatedUser(ShopUserDetails.class)
                .map(userDetails -> {
                    try {
                        return userService.getUserById(userDetails.getId());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .orElse(null);
    }
}
