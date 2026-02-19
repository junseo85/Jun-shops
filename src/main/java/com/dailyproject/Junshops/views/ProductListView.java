package com.dailyproject.Junshops.views;

import com.dailyproject.Junshops.dto.ProductDto;
import com.dailyproject.Junshops.model.Cart;
import com.dailyproject.Junshops.model.User;
import com.dailyproject.Junshops.service.cart.ICartItemService;
import com.dailyproject.Junshops.service.cart.ICartService;
import com.dailyproject.Junshops.service.product.IProductService;
import com.dailyproject.Junshops.service.user.IUserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value = "products", layout = MainLayout.class)
@PageTitle("Products | Jun-Shops")
@PermitAll
public class ProductListView extends VerticalLayout {

    private final IProductService productService;
    private final ICartService cartService;
    private final ICartItemService cartItemService;
    private final IUserService userService;

    private final Grid<ProductDto> grid = new Grid<>(ProductDto.class, false);
    private final TextField filterText = new TextField();

    // Track quantity selection state for each product
    private final Map<Long, QuantitySelector> quantitySelectors = new HashMap<>();

    public ProductListView(IProductService productService,
                           ICartService cartService,
                           ICartItemService cartItemService,
                           IUserService userService) {
        this.productService = productService;
        this.cartService = cartService;
        this.cartItemService = cartItemService;
        this.userService = userService;

        addClassName("product-list-view");
        setSizeFull();

        configureGrid();
        configureFilter();

        add(getToolbar(), grid);
        updateList();
    }

    private void configureGrid() {
        grid.addClassName("product-grid");
        grid.setSizeFull();

        grid.addColumn(ProductDto::getId).setHeader("ID").setWidth("80px");
        grid.addColumn(ProductDto::getName).setHeader("Name").setAutoWidth(true);
        grid.addColumn(ProductDto::getBrand).setHeader("Brand").setAutoWidth(true);
        grid.addColumn(ProductDto::getPrice).setHeader("Price").setAutoWidth(true);
        grid.addColumn(ProductDto::getInventory).setHeader("Stock").setWidth("100px");

        // ✅ NEW: Add interactive quantity selector with confirm
        grid.addComponentColumn(product -> {
            return createQuantitySelectorLayout(product);
        }).setHeader("Actions").setWidth("250px").setFlexGrow(0);
    }

    /**
     * Creates an interactive layout with quantity selector and add/confirm button
     */
    private HorizontalLayout createQuantitySelectorLayout(ProductDto product) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setAlignItems(Alignment.CENTER);

        // Quantity selector components
        Button decreaseBtn = new Button(new Icon(VaadinIcon.MINUS));
        decreaseBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        Span quantityDisplay = new Span("1");
        quantityDisplay.getStyle()
                .set("min-width", "30px")
                .set("text-align", "center")
                .set("font-weight", "bold");

        Button increaseBtn = new Button(new Icon(VaadinIcon.PLUS));
        increaseBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        // Main action button (Add to Cart / Confirm)
        Button actionButton = new Button("Add to Cart");
        actionButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        actionButton.setEnabled(product.getInventory() > 0);

        // Initially hide quantity controls
        decreaseBtn.setVisible(false);
        quantityDisplay.setVisible(false);
        increaseBtn.setVisible(false);

        // Store state
        QuantitySelector selector = new QuantitySelector(1, false);
        quantitySelectors.put(product.getId(), selector);

        // Decrease quantity
        decreaseBtn.addClickListener(e -> {
            if (selector.quantity > 1) {
                selector.quantity--;
                quantityDisplay.setText(String.valueOf(selector.quantity));
            }
        });

        // Increase quantity
        increaseBtn.addClickListener(e -> {
            if (selector.quantity < product.getInventory() && selector.quantity < 99) {
                selector.quantity++;
                quantityDisplay.setText(String.valueOf(selector.quantity));
            }
        });

        // Main button click handler
        actionButton.addClickListener(e -> {
            if (!selector.isSelecting) {
                // First click: Show quantity selector, change to "Confirm"
                selector.isSelecting = true;
                decreaseBtn.setVisible(true);
                quantityDisplay.setVisible(true);
                increaseBtn.setVisible(true);
                actionButton.setText("Confirm");
                actionButton.setIcon(new Icon(VaadinIcon.CHECK));
                actionButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
                actionButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            } else {
                // Second click: Confirm and add to cart
                addToCart(product, selector.quantity);

                // Reset state
                selector.isSelecting = false;
                selector.quantity = 1;
                quantityDisplay.setText("1");
                decreaseBtn.setVisible(false);
                quantityDisplay.setVisible(false);
                increaseBtn.setVisible(false);
                actionButton.setText("Add to Cart");
                actionButton.setIcon(null);
                actionButton.removeThemeVariants(ButtonVariant.LUMO_SUCCESS);
                actionButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            }
        });

        if (product.getInventory() == 0) {
            layout.add(new Span("Out of Stock"));
        } else {
            layout.add(actionButton, decreaseBtn, quantityDisplay, increaseBtn);
        }

        return layout;
    }

    private void configureFilter() {
        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());
    }

    private HorizontalLayout getToolbar() {
        Button refreshButton = new Button("Refresh");
        refreshButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        refreshButton.addClickListener(e -> updateList());

        HorizontalLayout toolbar = new HorizontalLayout(filterText, refreshButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void updateList() {
        try {
            String filter = filterText.getValue();
            List<ProductDto> products;

            if (filter == null || filter.isEmpty()) {
                products = productService.getConvertedProducts(
                        productService.getAllProducts()
                );
            } else {
                products = productService.getConvertedProducts(
                        productService.getProductsByName(filter)
                );
            }

            grid.setItems(products);
            quantitySelectors.clear(); // Clear state when list updates
        } catch (Exception e) {
            Notification.show("Error loading products: " + e.getMessage(),
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            grid.setItems();
        }
    }

    private void addToCart(ProductDto product, int quantity) {
        try {
            User user = userService.getAuthenticatedUser();
            Cart cart = cartService.initializeNewCart(user);
            cartItemService.addItemToCart(cart.getId(), product.getId(), quantity);

            Notification notification = Notification.show(
                    "✅ Added " + quantity + "x " + product.getName() + " to cart!",
                    3000,
                    Notification.Position.BOTTOM_END
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (Exception e) {
            Notification.show("Failed to add to cart: " + e.getMessage(),
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Helper class to track quantity selection state
     */
    private static class QuantitySelector {
        int quantity;
        boolean isSelecting;

        public QuantitySelector(int quantity, boolean isSelecting) {
            this.quantity = quantity;
            this.isSelecting = isSelecting;
        }
    }
}