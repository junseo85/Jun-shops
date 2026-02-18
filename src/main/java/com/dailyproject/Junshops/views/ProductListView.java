package com.dailyproject.Junshops.views;

import com.dailyproject.Junshops.client.CartClient;
import com.dailyproject.Junshops.client.ProductClient;
import com.dailyproject.Junshops.dto.ProductDto;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "products", layout = MainLayout.class)
@PageTitle("Products | Jun-Shops")
@PermitAll
public class ProductListView extends VerticalLayout {

    private final ProductClient productClient;
    private final CartClient cartClient;
    private final Grid<ProductDto> grid = new Grid<>(ProductDto.class, false);
    private final TextField filterText = new TextField();

    /**
     * Initializes view; configures grid and filter; displays products
     */
    @Autowired
    public ProductListView(ProductClient productClient, CartClient cartClient) {
        this.productClient = productClient;
        this.cartClient = cartClient;

        addClassName("product-list-view");
        setSizeFull();

        configureGrid();
        configureFilter();

        add(getToolbar(), grid);
        updateList();
    }

    /**
     * Configures product grid columns and add-to-cart actions
     */
    private void configureGrid() {
        grid.addClassName("product-grid");
        grid.setSizeFull();

        grid.addColumn(ProductDto::getId).setHeader("ID").setWidth("80px");
        grid.addColumn(ProductDto::getName).setHeader("Name").setAutoWidth(true);
        grid.addColumn(ProductDto::getBrand).setHeader("Brand").setAutoWidth(true);
        grid.addColumn(ProductDto::getPrice).setHeader("Price").setAutoWidth(true);
        grid.addColumn(ProductDto::getInventory).setHeader("Stock").setWidth("100px");

        // Adds interactive add‑to‑cart button for each product
        grid.addComponentColumn(product -> {
            Button addToCart = new Button("Add to Cart");
            addToCart.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            addToCart.addClickListener(e -> addToCart(product));
            addToCart.setEnabled(product.getInventory() > 0);
            return addToCart;
        }).setHeader("Actions").setWidth("150px");
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


    /**
     * Updates product grid, handling errors gracefully
     */
    private void updateList() {
        try {
            String filter = filterText.getValue();
            // Sets all or filtered products based on input
            if (filter == null || filter.isEmpty()) {
                grid.setItems(productClient.getAllProducts());
            } else {
                grid.setItems(productClient.searchProductsByName(filter));
            }
        } catch (Exception e) {
            Notification.show("Error loading products: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            grid.setItems();
        }
    }

    private void addToCart(ProductDto product) {
        // Adds item to cart; notifies success or failure
        try {
            cartClient.addItemToCart(product.getId(), 1);

            Notification notification = Notification.show(
                    "Added " + product.getName() + " to cart!",
                    3000,
                    Notification.Position.BOTTOM_END
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Failed to add to cart: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}