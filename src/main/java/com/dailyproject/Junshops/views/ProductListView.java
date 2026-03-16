package com.dailyproject.Junshops.views;

import com.dailyproject.Junshops.dto.ProductDto;
import com.dailyproject.Junshops.model.Cart;
import com.dailyproject.Junshops.model.Category;
import com.dailyproject.Junshops.model.User;
import com.dailyproject.Junshops.request.AddProductRequest;
import com.dailyproject.Junshops.request.ProductUpdateRequest;
import com.dailyproject.Junshops.service.cart.ICartItemService;
import com.dailyproject.Junshops.service.cart.ICartService;
import com.dailyproject.Junshops.service.category.ICategoryService;
import com.dailyproject.Junshops.service.product.IProductService;
import com.dailyproject.Junshops.service.user.IUserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ProductListView - Product catalog with admin capabilities
 *
 * FEATURES:
 * - Browse all products (all users)
 * - Add to cart (authenticated users)
 * - Add new products (admin only)
 * - Edit products (admin only)
 * - Refresh product list
 *
 * SECURITY:
 * - PermitAll: Anyone can view
 * - Add/Edit buttons: Only visible to admins
 */
@Route(value = "products", layout = MainLayout.class)
@PageTitle("Products | Jun-Shops")
@PermitAll
public class ProductListView extends VerticalLayout {

    private final IProductService productService;
    private final ICartService cartService;
    private final ICartItemService cartItemService;
    private final IUserService userService;
    private final ICategoryService categoryService;

    private final Grid<ProductDto> grid = new Grid<>(ProductDto.class, false);
    private final TextField filterText = new TextField();

    // Track quantity selection state for each product
    private final Map<Long, QuantitySelector> quantitySelectors = new HashMap<>();

    private boolean editMode = false;
    private Button editModeButton;

    public ProductListView(IProductService productService,
                           ICartService cartService,
                           ICartItemService cartItemService,
                           IUserService userService,ICategoryService categoryService) {
        this.productService = productService;
        this.cartService = cartService;
        this.cartItemService = cartItemService;
        this.userService = userService;
        this.categoryService = categoryService;

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

        // interactive quantity selector with confirm
        grid.addComponentColumn(product -> {
            return createQuantitySelectorLayout(product);
        }).setHeader("Actions").setWidth("250px").setFlexGrow(0);

        //Grid selection for edit mode
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addSelectionListener(selection -> {
            if (editMode && selection.getFirstSelectedItem().isPresent()) {
                ProductDto selectedProduct = selection.getFirstSelectedItem().get();
                openEditProductDialog(selectedProduct);
                // Reset edit mode and deselect
                toggleEditMode();
                grid.deselectAll();
                return;
            }selection.getFirstSelectedItem().ifPresent(selected -> {
                UI.getCurrent().navigate("products/" + selected.getId());
            });

        });
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
            layout.add( decreaseBtn, quantityDisplay, increaseBtn,actionButton);
        }

        return layout;
    }

    private void configureFilter() {
        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());
    }
    /**
     * Toolbar with Refresh and Admin Add button
     */
    private HorizontalLayout getToolbar() {
        Button refreshButton = new Button("Refresh", new Icon(VaadinIcon.REFRESH));
        refreshButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        refreshButton.addClickListener(e -> {
            updateList();
            showNotification("Products refreshed!", NotificationVariant.LUMO_SUCCESS);
        });

        HorizontalLayout toolbar = new HorizontalLayout(filterText, refreshButton);
        toolbar.addClassName("toolbar");
        toolbar.setAlignItems(Alignment.CENTER);

        // Add button and Edit button (only visible to admins)
        if (isAdmin()) {
            Button addButton = new Button("Add", new Icon(VaadinIcon.PLUS));
            addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            addButton.addClickListener(e -> openAddProductDialog());
            // Edit button
            editModeButton = new Button("Edit", new Icon(VaadinIcon.EDIT));
            editModeButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            editModeButton.addClickListener(e -> toggleEditMode());

            Button testMySelfButton = new Button("Test Myself", new Icon(VaadinIcon.USER));
            testMySelfButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_CONTRAST);
            testMySelfButton.addClickListener(e -> {});
            toolbar.add(addButton, editModeButton, testMySelfButton);
        }
        return toolbar;
    }

    /**
     * Toggle edit mode
     *
     * EDIT MODE:
     * - Button changes to "Cancel Edit"
     * - Grid rows become clickable
     * - Clicking a row opens edit dialog
     * - Clicking "Cancel Edit" exits edit mode
     */
    private void toggleEditMode() {
        editMode = !editMode;

        if (editMode) {
            editModeButton.setText("Cancel Edit");
            editModeButton.setIcon(new Icon(VaadinIcon.CLOSE));
            editModeButton.removeThemeVariants(ButtonVariant.LUMO_SUCCESS);
            editModeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

            // Visual feedback
            grid.addClassName("edit-mode");
            showNotification("📝 Edit mode: Click a product to edit",
                    NotificationVariant.LUMO_CONTRAST);
        } else {
            editModeButton.setText("Edit");
            editModeButton.setIcon(new Icon(VaadinIcon.EDIT));
            editModeButton.removeThemeVariants(ButtonVariant.LUMO_ERROR);
            editModeButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

            grid.removeClassName("edit-mode");
            grid.deselectAll();
        }
    }

    /**
     * Check if current user has ADMIN role
     */
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }
    /**
     * Open popup dialog to add new product
     */
    private void openAddProductDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("➕ Add New Product");
        dialog.setWidth("600px");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);

        // Form layout
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        // Product name
        TextField nameField = new TextField("Product Name");
        nameField.setRequired(true);
        nameField.setPlaceholder("e.g., iPhone 15 Pro");
        nameField.setWidthFull();

        // Brand
        TextField brandField = new TextField("Brand");
        brandField.setRequired(true);
        brandField.setPlaceholder("e.g., Apple");
        brandField.setWidthFull();

        // Category dropdown
        ComboBox<Category> categoryCombo = new ComboBox<>("Category");
        categoryCombo.setItems(categoryService.getAllCategories());
        categoryCombo.setItemLabelGenerator(Category::getName);
        categoryCombo.setRequired(true);
        categoryCombo.setPlaceholder("Select category");
        categoryCombo.setWidthFull();

        // Price
        NumberField priceField = new NumberField("Price");
        priceField.setRequired(true);
        priceField.setPrefixComponent(new Span("$"));
        priceField.setMin(0.01);
        priceField.setStep(0.01);
        priceField.setPlaceholder("0.00");
        priceField.setWidthFull();

        // Inventory
        IntegerField inventoryField = new IntegerField("Inventory");
        inventoryField.setRequired(true);
        inventoryField.setMin(0);
        inventoryField.setValue(0);
        inventoryField.setHelperText("Available stock quantity");
        inventoryField.setWidthFull();

        // Description
        TextArea descriptionField = new TextArea("Description");
        descriptionField.setPlaceholder("Product description...");
        descriptionField.setMaxLength(1000);
        descriptionField.setHelperText("Max 1000 characters");
        descriptionField.setWidthFull();

        // Add fields to form
        formLayout.add(nameField, brandField);
        formLayout.add(categoryCombo, 2);
        formLayout.add(priceField, inventoryField);
        formLayout.add(descriptionField, 2);

        // Confirm button
        Button confirmButton = new Button("Confirm", e -> {
            // Validate all required fields
            if (nameField.isEmpty() || brandField.isEmpty() ||
                    categoryCombo.isEmpty() || priceField.isEmpty() ||
                    inventoryField.isEmpty()) {
                showNotification("⚠️ Please fill all required fields",
                        NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                // Create product request
                AddProductRequest request = new AddProductRequest();
                request.setName(nameField.getValue());
                request.setBrand(brandField.getValue());
                request.setCategory(categoryCombo.getValue());
                request.setPrice(BigDecimal.valueOf(priceField.getValue()));
                request.setInventory(inventoryField.getValue());
                request.setDescription(descriptionField.getValue());

                // Add product (service handles entity creation)
                productService.addProduct(request);

                // Show success message
                showNotification("✅ Product added successfully!",
                        NotificationVariant.LUMO_SUCCESS);

                // Close dialog
                dialog.close();

                // ✅ Refresh product list (updateList() converts entities to DTOs)
                updateList();

            } catch (Exception ex) {
                showNotification("❌ Error adding product: " + ex.getMessage(),
                        NotificationVariant.LUMO_ERROR);
                ex.printStackTrace();
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Cancel button
        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Button layout
        HorizontalLayout buttonsLayout = new HorizontalLayout(confirmButton, cancelButton);
        buttonsLayout.setJustifyContentMode(JustifyContentMode.END);
        buttonsLayout.setPadding(true);

        // Add form and buttons to dialog
        VerticalLayout dialogLayout = new VerticalLayout(formLayout, buttonsLayout);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        dialog.add(dialogLayout);
        dialog.open();
    }

    /**
     * ✅ NEW: Open dialog to edit existing product
     *
     * FEATURES:
     * - Pre-filled with current product data
     * - ID is read-only (displayed but not editable)
     * - All other fields editable
     * - Category dropdown
     * - Confirm to save changes
     */
    private void openEditProductDialog(ProductDto product) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("✏️ Edit Product");
        dialog.setWidth("600px");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        // ✅ Product ID (read-only display)
        H3 productIdDisplay = new H3("Product ID: #" + product.getId());
        productIdDisplay.getStyle()
                .set("margin-top", "0")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-m)");

        // Product Name
        TextField nameField = new TextField("Product Name");
        nameField.setValue(product.getName());
        nameField.setRequired(true);
        nameField.setWidthFull();

        // Brand
        TextField brandField = new TextField("Brand");
        brandField.setValue(product.getBrand());
        brandField.setRequired(true);
        brandField.setWidthFull();

        // Category dropdown
        ComboBox<Category> categoryCombo = new ComboBox<>("Category");
        List<Category> allCategories = categoryService.getAllCategories();
        categoryCombo.setItems(allCategories);
        categoryCombo.setItemLabelGenerator(Category::getName);
        categoryCombo.setRequired(true);
        categoryCombo.setWidthFull();

        // Pre-select current category by name
        allCategories.stream()
                .filter(cat -> cat.getName().equals(product.getCategoryName()))
                .findFirst()
                .ifPresent(categoryCombo::setValue);

        // Price
        NumberField priceField = new NumberField("Price");
        priceField.setValue(product.getPrice().doubleValue());
        priceField.setRequired(true);
        priceField.setPrefixComponent(new Span("$"));
        priceField.setMin(0.01);
        priceField.setStep(0.01);
        priceField.setWidthFull();

        // Inventory (Stock)
        IntegerField inventoryField = new IntegerField("Stock");
        inventoryField.setValue(product.getInventory());
        inventoryField.setRequired(true);
        inventoryField.setMin(0);
        inventoryField.setWidthFull();

        // Low stock warning
        if (product.getInventory() < 10) {
            inventoryField.setHelperText("⚠️ Low stock!");
        }

        // Description
        TextArea descriptionField = new TextArea("Description");
        descriptionField.setValue(product.getDescription() != null ? product.getDescription() : "");
        descriptionField.setMaxLength(1000);
        descriptionField.setWidthFull();

        // Add fields to form
        formLayout.add(productIdDisplay, 2);  // Full width
        formLayout.add(nameField, brandField);
        formLayout.add(categoryCombo, 2);  // Full width
        formLayout.add(priceField, inventoryField);
        formLayout.add(descriptionField, 2);  // Full width

        // Confirm button
        Button confirmButton = new Button("Confirm", e -> {
            if (nameField.isEmpty() || brandField.isEmpty() ||
                    categoryCombo.isEmpty() || priceField.isEmpty() ||
                    inventoryField.isEmpty()) {
                showNotification("⚠️ Please fill all required fields",
                        NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                ProductUpdateRequest request = new ProductUpdateRequest();
                request.setName(nameField.getValue());
                request.setBrand(brandField.getValue());
                request.setCategory(categoryCombo.getValue());
                request.setPrice(BigDecimal.valueOf(priceField.getValue()));
                request.setInventory(inventoryField.getValue());
                request.setDescription(descriptionField.getValue());

                productService.updateProduct(request, product.getId());

                showNotification("✅ Product updated successfully!",
                        NotificationVariant.LUMO_SUCCESS);

                dialog.close();
                updateList();

            } catch (Exception ex) {
                showNotification("❌ Error updating product: " + ex.getMessage(),
                        NotificationVariant.LUMO_ERROR);
                ex.printStackTrace();
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Cancel button
        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttonsLayout = new HorizontalLayout(confirmButton, cancelButton);
        buttonsLayout.setJustifyContentMode(JustifyContentMode.END);
        buttonsLayout.setPadding(true);

        VerticalLayout dialogLayout = new VerticalLayout(formLayout, buttonsLayout);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        dialog.add(dialogLayout);
        dialog.open();
    }
    /**
     * Update product list
     *
     * ✅ IMPORTANT: Uses getConvertedProducts() to convert entities to DTOs
     * This is why Grid<ProductDto> works perfectly!
     */
    private void updateList() {
        try {
            String filter = filterText.getValue();
            List<ProductDto> products;

            if (filter == null || filter.isEmpty()) {
                // Get all products as DTOs
                products = productService.getConvertedProducts(
                        productService.getAllProducts()
                );
            } else {
                // Get filtered products as DTOs
                products = productService.getConvertedProducts(
                        productService.getProductsByName(filter)
                );
            }

            grid.setItems(products);
            quantitySelectors.clear(); // Clear state when list updates
        } catch (Exception e) {
            showNotification("❌ Error loading products: " + e.getMessage(),
                    NotificationVariant.LUMO_ERROR);
            grid.setItems();
        }
    }
    private void addToCart(ProductDto product, int quantity) {
        try {
            User user = userService.getAuthenticatedUser();
            Cart cart = cartService.initializeNewCart(user);
            cartItemService.addItemToCart(cart.getId(), product.getId(), quantity);

            showNotification(
                    "✅ Added " + quantity + "x " + product.getName() + " to cart!",
                    NotificationVariant.LUMO_SUCCESS
            );

        } catch (Exception e) {
            showNotification("❌ Failed to add to cart: " + e.getMessage(),
                    NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 3000,
                Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(variant);
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