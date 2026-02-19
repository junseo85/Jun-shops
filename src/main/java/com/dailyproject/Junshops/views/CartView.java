package com.dailyproject.Junshops.views;

import com.dailyproject.Junshops.model.Cart;
import com.dailyproject.Junshops.model.CartItem;
import com.dailyproject.Junshops.model.User;
import com.dailyproject.Junshops.service.cart.ICartItemService;
import com.dailyproject.Junshops.service.cart.ICartService;
import com.dailyproject.Junshops.service.user.IUserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;

@Route(value = "cart", layout = MainLayout.class)
@PageTitle("Shopping Cart | Jun-Shops")
@PermitAll
public class CartView extends VerticalLayout {

    private final ICartService cartService;
    private final ICartItemService cartItemService;
    private final IUserService userService;

    private VerticalLayout cartItemsLayout;
    private Span totalAmountSpan;
    private Cart currentCart;

    public CartView(ICartService cartService,
                    ICartItemService cartItemService,
                    IUserService userService) {
        this.cartService = cartService;
        this.cartItemService = cartItemService;
        this.userService = userService;

        setSpacing(true);
        setPadding(true);
        setMaxWidth("1200px");
        setAlignSelf(Alignment.CENTER);

        H2 title = new H2("🛒 Shopping Cart");
        add(title);

        cartItemsLayout = new VerticalLayout();
        cartItemsLayout.setSpacing(true);
        cartItemsLayout.setPadding(false);
        cartItemsLayout.setWidthFull();

        add(cartItemsLayout);

        // Total and checkout section
        HorizontalLayout footer = createFooter();
        add(footer);

        loadCart();
    }

    private void loadCart() {
        cartItemsLayout.removeAll();

        try {
            // Get authenticated user
            User user = userService.getAuthenticatedUser();

            //  USE getCartByUserIdWithItems - fetches everything in one query
            currentCart = cartService.getCartByUserIdWithItems(user.getId());

            // Check if cart exists and has items
            if (currentCart == null || currentCart.getItems() == null || currentCart.getItems().isEmpty()) {
                showEmptyCart();
                return;
            }

            // Display items
            currentCart.getItems().forEach(this::addCartItemComponent);
            updateTotal(currentCart.getTotalAmount());

        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Error loading cart: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
            showEmptyCart();
        }
    }

    private void showEmptyCart() {
        VerticalLayout empty = new VerticalLayout();
        empty.setAlignItems(Alignment.CENTER);
        empty.setJustifyContentMode(JustifyContentMode.CENTER);
        empty.setPadding(true);

        Icon icon = VaadinIcon.CART_O.create();
        icon.setSize("64px");
        icon.getStyle().set("color", "var(--lumo-contrast-30pct)");

        H3 message = new H3("Your cart is empty");
        message.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Button shopButton = new Button("Continue Shopping", e ->
                getUI().ifPresent(ui -> ui.navigate(ProductListView.class))
        );
        shopButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        empty.add(icon, message, shopButton);
        cartItemsLayout.add(empty);

        updateTotal(BigDecimal.ZERO);
    }

    private void addCartItemComponent(CartItem item) {
        HorizontalLayout itemLayout = new HorizontalLayout();
        itemLayout.setWidthFull();
        itemLayout.setPadding(true);
        itemLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        itemLayout.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px")
                .set("background", "var(--lumo-base-color)")
                .set("margin-bottom", "10px");

        // Product info
        VerticalLayout productInfo = new VerticalLayout();
        productInfo.setSpacing(false);
        productInfo.setPadding(false);

        H3 productName = new H3(item.getProduct().getName());
        productName.getStyle().set("margin", "0");

        Span brand = new Span(item.getProduct().getBrand());
        brand.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span price = new Span("$" + item.getUnitPrice());
        price.getStyle()
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)");

        productInfo.add(productName, brand, price);

        // Quantity controls
        IntegerField quantityField = new IntegerField();
        quantityField.setValue(item.getQuantity());
        quantityField.setMin(1);
        quantityField.setMax(99);
        quantityField.setWidth("100px");
        quantityField.setStepButtonsVisible(true);

        quantityField.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue() > 0) {
                updateQuantity(item.getProduct().getId(), e.getValue());
            }
        });

        // Item total
        BigDecimal itemTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
        Span itemTotalSpan = new Span("$" + itemTotal);
        itemTotalSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("font-weight", "bold");

        // Remove button
        Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        removeButton.addClickListener(e -> removeItem(item.getProduct().getId()));

        itemLayout.add(productInfo, quantityField, itemTotalSpan, removeButton);
        itemLayout.expand(productInfo);

        cartItemsLayout.add(itemLayout);
    }

    private HorizontalLayout createFooter() {
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.BETWEEN);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setPadding(true);
        footer.getStyle()
                .set("border-top", "2px solid var(--lumo-contrast-10pct)")
                .set("margin-top", "20px");

        // Total amount
        VerticalLayout totalLayout = new VerticalLayout();
        totalLayout.setSpacing(false);
        totalLayout.setPadding(false);

        Span totalLabel = new Span("Total:");
        totalLabel.getStyle().set("font-size", "var(--lumo-font-size-m)");

        totalAmountSpan = new Span("$0.00");
        totalAmountSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)");

        totalLayout.add(totalLabel, totalAmountSpan);

        // Action buttons
        HorizontalLayout buttonLayout = new HorizontalLayout();

        Button clearButton = new Button("Clear Cart", e -> clearCart());
        clearButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        Button checkoutButton = new Button("Proceed to Checkout",
                new Icon(VaadinIcon.ARROW_RIGHT));
        checkoutButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        checkoutButton.addClickListener(e -> {
            // TODO: Navigate to checkout
            showNotification("Checkout coming soon!", NotificationVariant.LUMO_SUCCESS);
        });

        buttonLayout.add(clearButton, checkoutButton);

        footer.add(totalLayout, buttonLayout);
        return footer;
    }

    private void updateQuantity(Long productId, int quantity) {
        try {
            cartItemService.updateItemQuantity(currentCart.getId(), productId, quantity);
            loadCart();
            showNotification("Quantity updated", NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            showNotification("Failed to update quantity: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
            loadCart(); // Reload to show correct values
        }
    }

    private void removeItem(Long productId) {
        try {
            cartItemService.removeItemFromCart(currentCart.getId(), productId);
            loadCart();
            showNotification("Item removed from cart", NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            showNotification("Failed to remove item: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void clearCart() {
        try {
            cartService.clearCart(currentCart.getId());
            loadCart();
            showNotification("Cart cleared", NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            showNotification("Failed to clear cart: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateTotal(BigDecimal total) {
        totalAmountSpan.setText("$" + total);
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000,
                Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(variant);
        notification.open();
    }
}