package com.dailyproject.Junshops.views;

import com.dailyproject.Junshops.dto.ImageDto;
import com.dailyproject.Junshops.dto.ProductDto;
import com.dailyproject.Junshops.model.Cart;
import com.dailyproject.Junshops.model.Category;
import com.dailyproject.Junshops.model.User;
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
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;

@Route(value = "products/:productId", layout = MainLayout.class)
@PageTitle("Product Detail | Jun-Shops")
@PermitAll
public class ProductDetailView extends VerticalLayout implements BeforeEnterObserver {

    private final IProductService productService;
    private final ICartService cartService;
    private final ICartItemService cartItemService;
    private final IUserService userService;
    private final ICategoryService categoryService;

    // UI sections
    private final VerticalLayout leftMedia = new VerticalLayout();
    private final VerticalLayout rightActions = new VerticalLayout();
    private final VerticalLayout detailsSection = new VerticalLayout();
    private final HorizontalLayout topRow = new HorizontalLayout(leftMedia, rightActions);

    // state
    private Long productId;
    private ProductDto product;

    public ProductDetailView(IProductService productService,
                             ICartService cartService,
                             ICartItemService cartItemService,
                             IUserService userService, ICategoryService categoryService) {

        this.productService = productService;
        this.cartService = cartService;
        this.cartItemService = cartItemService;
        this.userService = userService;
        this.categoryService = categoryService;

        addClassName("product-detail-view");
        setSizeFull();
        setSpacing(true);
        setPadding(true);

        // Layout setup (3 sections)
        leftMedia.setWidth("50%");
        rightActions.setWidth("50%");
        rightActions.setAlignItems(Alignment.END);

        topRow.setWidthFull();
        topRow.setSpacing(true);

        detailsSection.setWidthFull();
        detailsSection.setSpacing(false);
        detailsSection.setPadding(false);

        add(topRow, detailsSection);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // 1) read productId from URL
        String idParam = event.getRouteParameters().get("productId").orElse(null);
        if (idParam == null) {
            showNotFoundAndGoBack("Missing product id");
            return;
        }

        try {
            productId = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            showNotFoundAndGoBack("Invalid product id: " + idParam);
            return;
        }

        // 2) load product
        try {
            product = productService.converToDto(productService.getProductById(productId));
        } catch (Exception e) {
            showNotFoundAndGoBack("Product not found (ID: " + productId + ")");
            return;
        }

        // 3) render UI
        render();
    }

    private void render() {
        leftMedia.removeAll();
        rightActions.removeAll();
        detailsSection.removeAll();

        // ---------- Top left: Image / video (image for now) ----------
        leftMedia.add(new H2(product.getName() != null ? product.getName() : "Product"));

        Image imageComponent = buildMainImage(product.getImages());
        leftMedia.add(imageComponent);

        // ---------- Top right: buttons ----------
        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addToCartButton.setEnabled(product.getInventory() > 0);
        addToCartButton.addClickListener(e -> addToCart(1));

        rightActions.add(addToCartButton);

        // Admin-only Edit button
        if (isAdmin()) {
            Button editButton = new Button("Edit");
            editButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            editButton.addClickListener(e -> {
                openEditProductDialog(product);
            });
            rightActions.add(editButton);
        }

        // ---------- Bottom: detail info ----------
        detailsSection.add(
                //line("ID", String.valueOf(product.getId())),
                line("Brand", safe(product.getBrand())),
                line("Category", safe(product.getCategoryName())),
                line("Price", product.getPrice() != null ? product.getPrice().toString() : ""),
                line("Current Stock", String.valueOf(product.getInventory())),
                line("Description", safe(product.getDescription()))
        );
    }
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

        // Product ID (read-only display)
        H3 productIdDisplay = new H3("Product ID: #" + product.getId());
        productIdDisplay.getStyle()
                .set("margin-top", "0")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-m)");

        TextField nameField = new TextField("Product Name");
        nameField.setValue(product.getName());
        nameField.setRequired(true);
        nameField.setWidthFull();

        TextField brandField = new TextField("Brand");
        brandField.setValue(product.getBrand());
        brandField.setRequired(true);
        brandField.setWidthFull();

        ComboBox<Category> categoryCombo = new ComboBox<>("Category");
        List<Category> allCategories = categoryService.getAllCategories();
        categoryCombo.setItems(allCategories);
        categoryCombo.setItemLabelGenerator(Category::getName);
        categoryCombo.setRequired(true);
        categoryCombo.setWidthFull();

        allCategories.stream()
                .filter(cat -> cat.getName().equals(product.getCategoryName()))
                .findFirst()
                .ifPresent(categoryCombo::setValue);

        NumberField priceField = new NumberField("Price");
        priceField.setValue(product.getPrice().doubleValue());
        priceField.setRequired(true);
        priceField.setPrefixComponent(new Span("$"));
        priceField.setMin(0.01);
        priceField.setStep(0.01);
        priceField.setWidthFull();

        IntegerField inventoryField = new IntegerField("Stock");
        inventoryField.setValue(product.getInventory());
        inventoryField.setRequired(true);
        inventoryField.setMin(0);
        inventoryField.setWidthFull();

        if (product.getInventory() < 10) {
            inventoryField.setHelperText("⚠️ Low stock!");
        }

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setValue(product.getDescription() != null ? product.getDescription() : "");
        descriptionField.setMaxLength(1000);
        descriptionField.setWidthFull();

        formLayout.add(productIdDisplay, 2);
        formLayout.add(nameField, brandField);
        formLayout.add(categoryCombo, 2);
        formLayout.add(priceField, inventoryField);
        formLayout.add(descriptionField, 2);

        Button confirmButton = new Button("Confirm", e -> {
            if (nameField.isEmpty() || brandField.isEmpty() ||
                    categoryCombo.isEmpty() || priceField.isEmpty() ||
                    inventoryField.isEmpty()) {
                Notification n = Notification.show("⚠️ Please fill all required fields", 3000,
                        Notification.Position.BOTTOM_CENTER);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
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

                Notification n = Notification.show("✅ Product updated successfully!", 3000,
                        Notification.Position.BOTTOM_CENTER);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                dialog.close();

                // ✅ refresh current detail page with updated data
                this.product = productService.converToDto(productService.getProductById(product.getId()));
                render();

            } catch (Exception ex) {
                Notification n = Notification.show("❌ Error updating product: " + ex.getMessage(), 3000,
                        Notification.Position.BOTTOM_CENTER);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                ex.printStackTrace();
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

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

    private Image buildMainImage(List<ImageDto> images) {
        // If no photos -> show placeholder image-like box
        if (images == null || images.isEmpty() || images.get(0) == null || images.get(0).getDownloadUrl() == null) {
            Image placeholder = new Image();
            placeholder.setAlt("image not available");
            placeholder.setSrc("data:image/svg+xml;charset=utf-8," +
                    "<svg xmlns='http://www.w3.org/2000/svg' width='640' height='360'>" +
                    "<rect width='100%25' height='100%25' fill='%23f3f4f6'/>" +
                    "<text x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' " +
                    "fill='%236b7280' font-size='24'>image not available</text></svg>");
            placeholder.setWidth("100%");
            placeholder.setHeight("360px");
            return placeholder;
        }

        String downloadUrl = images.get(0).getDownloadUrl();
        Image img = new Image(downloadUrl, "product image");
        img.setWidth("100%");
        img.setHeight("360px");
        img.getStyle().set("object-fit", "contain");
        return img;
    }

    private void addToCart(int quantity) {
        try {
            User user = userService.getAuthenticatedUser();
            Cart cart = cartService.initializeNewCart(user);
            cartItemService.addItemToCart(cart.getId(), product.getId(), quantity);

            Notification n = Notification.show(
                    "✅ Added " + quantity + "x " + product.getName() + " to cart!",
                    3000, Notification.Position.BOTTOM_CENTER);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (Exception e) {
            Notification n = Notification.show(
                    "❌ Failed to add to cart: " + e.getMessage(),
                    3000, Notification.Position.BOTTOM_CENTER);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return false;

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }

    private Span line(String label, String value) {
        Span s = new Span(label + ": " + value);
        s.getStyle().set("display", "block");
        return s;
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }

    private void showNotFoundAndGoBack(String message) {
        Notification n = Notification.show(message, 3000, Notification.Position.BOTTOM_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);

        // go back to products list
        UI.getCurrent().navigate("products");
    }
}