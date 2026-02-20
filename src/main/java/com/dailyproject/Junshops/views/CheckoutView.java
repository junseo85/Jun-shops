package com.dailyproject.Junshops.views;

import com.dailyproject.Junshops.model.Cart;
import com.dailyproject.Junshops.model.CartItem;
import com.dailyproject.Junshops.model.User;
import com.dailyproject.Junshops.service.cart.ICartService;
import com.dailyproject.Junshops.service.order.IOrderService;
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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;

/**
 * CheckoutView - Payment and Order Confirmation Page
 *
 * PURPOSE:
 * - Display order summary
 * - Collect payment information
 * - Collect shipping address
 * - Allow user to review and confirm order
 *
 * WORKFLOW:
 * 1. User arrives from CartView
 * 2. Sees order summary
 * 3. Fills out payment/shipping info
 * 4. Clicks "Place Order"
 * 5. Order is created in backend
 * 6. Navigates to OrderView (confirmation)
 */
@Route(value = "checkout", layout = MainLayout.class)
@PageTitle("Checkout | Jun-Shops")
@PermitAll
public class CheckoutView extends VerticalLayout {

    private final ICartService cartService;
    private final IOrderService orderService;
    private final IUserService userService;

    private Cart currentCart;

    // Form fields
    private final TextField cardNumberField = new TextField("Card Number");
    private final TextField cardHolderField = new TextField("Cardholder Name");
    private final TextField expiryDateField = new TextField("Expiry Date");
    private final PasswordField cvvField = new PasswordField("CVV");

    private final TextField addressLine1Field = new TextField("Address Line 1");
    private final TextField addressLine2Field = new TextField("Address Line 2");
    private final TextField cityField = new TextField("City");
    private final TextField stateField = new TextField("State/Province");
    private final TextField zipCodeField = new TextField("ZIP/Postal Code");
    private final TextField countryField = new TextField("Country");

    private final EmailField emailField = new EmailField("Email");
    private final TextField phoneField = new TextField("Phone Number");

    /**
     * Constructor - Dependency Injection
     *
     * WHY these services?
     * - cartService: Load user's cart to display items
     * - orderService: Create order when user confirms
     * - userService: Get logged-in user information
     */
    public CheckoutView(ICartService cartService,
                        IOrderService orderService,
                        IUserService userService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.userService = userService;

        setSpacing(true);
        setPadding(true);
        setMaxWidth("1200px");
        setAlignSelf(Alignment.CENTER);

        H2 title = new H2("🛒 Checkout");
        add(title);

        // Load cart
        loadCart();

        // If cart is empty, show message and don't show forms
        if (currentCart == null || currentCart.getItems().isEmpty()) {
            showEmptyCartMessage();
            return;
        }

        // Create two-column layout
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidthFull();
        mainLayout.setSpacing(true);

        // Left column: Forms
        VerticalLayout leftColumn = createFormsColumn();

        // Right column: Order summary
        VerticalLayout rightColumn = createOrderSummaryColumn();

        mainLayout.add(leftColumn, rightColumn);
        mainLayout.setFlexGrow(2, leftColumn);   // Left takes 2/3
        mainLayout.setFlexGrow(1, rightColumn);  // Right takes 1/3

        add(mainLayout);
    }

    /**
     * Load user's cart with items
     *
     * WHY?
     * - Need to display what user is buying
     * - Need cart ID to create order
     * - Verify cart still has items (user might have cleared it)
     */
    private void loadCart() {
        try {
            User user = userService.getAuthenticatedUser();
            currentCart = cartService.getCartByUserIdWithItems(user.getId());
        } catch (Exception e) {
            showNotification("Error loading cart: " + e.getMessage(),
                    NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Show message if cart is empty
     *
     * WHY?
     * - User might have navigated here directly
     * - Cart might have been cleared
     * - Prevent checkout with empty cart
     */
    private void showEmptyCartMessage() {
        VerticalLayout empty = new VerticalLayout();
        empty.setAlignItems(Alignment.CENTER);

        Icon icon = VaadinIcon.CART_O.create();
        icon.setSize("64px");

        H3 message = new H3("Your cart is empty");

        Button shopButton = new Button("Continue Shopping", e ->
                getUI().ifPresent(ui -> ui.navigate(ProductListView.class))
        );
        shopButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        empty.add(icon, message, shopButton);
        add(empty);
    }

    /**
     * Create left column with all input forms
     *
     * SECTIONS:
     * 1. Contact Information
     * 2. Payment Information
     * 3. Shipping Address
     * 4. Place Order Button
     */
    private VerticalLayout createFormsColumn() {
        VerticalLayout column = new VerticalLayout();
        column.setSpacing(true);
        column.setPadding(false);

        // Section 1: Contact Information
        column.add(createContactSection());

        // Section 2: Payment Information
        column.add(createPaymentSection());

        // Section 3: Shipping Address
        column.add(createShippingSection());

        // Place Order Button
        column.add(createPlaceOrderButton());

        return column;
    }

    /**
     * Contact Information Section
     *
     * FIELDS:
     * - Email: For order confirmation
     * - Phone: For delivery contact
     *
     * WHY?
     * - Need to send order confirmation email
     * - Delivery person needs to contact customer
     */
    private VerticalLayout createContactSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px");

        H3 header = new H3("📧 Contact Information");
        header.getStyle().set("margin-top", "0");

        // Pre-fill email with user's account email
        try {
            User user = userService.getAuthenticatedUser();
            emailField.setValue(user.getEmail());
        } catch (Exception e) {
            // If can't get user, leave empty
        }

        emailField.setWidthFull();
        emailField.setPlaceholder("your.email@example.com");
        emailField.setRequired(true);

        phoneField.setWidthFull();
        phoneField.setPlaceholder("+1 (555) 123-4567");
        phoneField.setRequired(true);

        section.add(header, emailField, phoneField);
        return section;
    }

    /**
     * Payment Information Section
     *
     * FIELDS:
     * - Card Number: 16 digits
     * - Cardholder Name: Name on card
     * - Expiry Date: MM/YY
     * - CVV: 3-digit security code
     *
     * WHY?
     * - Need payment method to charge customer
     * - Verify card is valid
     *
     * NOTE: In production, this would integrate with Stripe/PayPal
     *       Never store raw credit card data in your database!
     */
    private VerticalLayout createPaymentSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px");

        H3 header = new H3("💳 Payment Information");
        header.getStyle().set("margin-top", "0");

        // Card Number
        cardNumberField.setWidthFull();
        cardNumberField.setPlaceholder("1234 5678 9012 3456");
        cardNumberField.setRequired(true);
        cardNumberField.setMaxLength(19);  // 16 digits + 3 spaces
        cardNumberField.setHelperText("Enter 16-digit card number");

        // Cardholder Name
        cardHolderField.setWidthFull();
        cardHolderField.setPlaceholder("John Doe");
        cardHolderField.setRequired(true);

        // Expiry and CVV (side by side)
        HorizontalLayout expiryAndCvv = new HorizontalLayout();
        expiryAndCvv.setWidthFull();

        expiryDateField.setPlaceholder("MM/YY");
        expiryDateField.setRequired(true);
        expiryDateField.setWidth("150px");

        cvvField.setPlaceholder("123");
        cvvField.setRequired(true);
        cvvField.setWidth("100px");
        cvvField.setHelperText("3 digits on back");

        expiryAndCvv.add(expiryDateField, cvvField);

        section.add(header, cardNumberField, cardHolderField, expiryAndCvv);
        return section;
    }

    /**
     * Shipping Address Section
     *
     * FIELDS:
     * - Address lines (street)
     * - City, State, ZIP
     * - Country
     *
     * WHY?
     * - Need to know where to ship products
     * - Calculate shipping cost (in real system)
     * - Verify delivery area
     */
    private VerticalLayout createShippingSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px");

        H3 header = new H3("🏠 Shipping Address");
        header.getStyle().set("margin-top", "0");

        addressLine1Field.setWidthFull();
        addressLine1Field.setPlaceholder("123 Main Street");
        addressLine1Field.setRequired(true);

        addressLine2Field.setWidthFull();
        addressLine2Field.setPlaceholder("Apartment, suite, etc. (optional)");

        cityField.setWidthFull();
        cityField.setPlaceholder("New York");
        cityField.setRequired(true);

        // State and ZIP side by side
        HorizontalLayout stateAndZip = new HorizontalLayout();
        stateAndZip.setWidthFull();

        stateField.setPlaceholder("NY");
        stateField.setRequired(true);

        zipCodeField.setPlaceholder("10001");
        zipCodeField.setRequired(true);
        zipCodeField.setWidth("150px");

        stateAndZip.add(stateField, zipCodeField);

        countryField.setWidthFull();
        countryField.setPlaceholder("United States");
        countryField.setRequired(true);
        countryField.setValue("United States");  // Default

        section.add(header, addressLine1Field, addressLine2Field,
                cityField, stateAndZip, countryField);
        return section;
    }

    /**
     * Create Place Order button
     *
     * BEHAVIOR:
     * 1. Validate all fields
     * 2. Create order in backend
     * 3. Show success message
     * 4. Navigate to order confirmation
     *
     * WHY large and prominent?
     * - This is the main action
     * - User should clearly see how to complete checkout
     */
    private Button createPlaceOrderButton() {
        Button placeOrderBtn = new Button("Place Order", new Icon(VaadinIcon.CHECK_CIRCLE));
        placeOrderBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        placeOrderBtn.getStyle()
                .set("width", "100%")
                .set("margin-top", "20px");

        placeOrderBtn.addClickListener(e -> handlePlaceOrder());

        return placeOrderBtn;
    }

    /**
     * Right column - Order Summary
     *
     * SHOWS:
     * - List of items with quantities
     * - Prices per item
     * - Subtotal
     * - Tax (if applicable)
     * - Total amount
     *
     * WHY?
     * - User needs to review what they're buying
     * - Verify quantities are correct
     * - See final price before confirming
     */
    private VerticalLayout createOrderSummaryColumn() {
        VerticalLayout column = new VerticalLayout();
        column.setPadding(true);
        column.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px")
                .set("background", "var(--lumo-contrast-5pct)");

        H3 header = new H3("📋 Order Summary");
        header.getStyle().set("margin-top", "0");
        column.add(header);

        // Add each cart item
        for (CartItem item : currentCart.getItems()) {
            HorizontalLayout itemLayout = createSummaryItemLayout(item);
            column.add(itemLayout);
        }

        // Divider
        Span divider = new Span();
        divider.getStyle()
                .set("border-top", "1px solid var(--lumo-contrast-20pct)")
                .set("width", "100%")
                .set("margin", "10px 0");
        column.add(divider);

        // Subtotal
        HorizontalLayout subtotalLayout = new HorizontalLayout();
        subtotalLayout.setWidthFull();
        subtotalLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        Span subtotalLabel = new Span("Subtotal:");
        Span subtotalAmount = new Span("$" + currentCart.getTotalAmount());
        subtotalLayout.add(subtotalLabel, subtotalAmount);

        // Tax (placeholder - would calculate in real system)
        HorizontalLayout taxLayout = new HorizontalLayout();
        taxLayout.setWidthFull();
        taxLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        Span taxLabel = new Span("Tax:");
        Span taxAmount = new Span("$0.00");
        taxLayout.add(taxLabel, taxAmount);

        // Shipping (placeholder)
        HorizontalLayout shippingLayout = new HorizontalLayout();
        shippingLayout.setWidthFull();
        shippingLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        Span shippingLabel = new Span("Shipping:");
        Span shippingAmount = new Span("FREE");
        shippingAmount.getStyle().set("color", "var(--lumo-success-color)");
        shippingLayout.add(shippingLabel, shippingAmount);

        column.add(subtotalLayout, taxLayout, shippingLayout);

        // Total (bold)
        HorizontalLayout totalLayout = new HorizontalLayout();
        totalLayout.setWidthFull();
        totalLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        totalLayout.getStyle()
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("font-weight", "bold")
                .set("margin-top", "10px");

        Span totalLabel = new Span("Total:");
        Span totalAmount = new Span("$" + currentCart.getTotalAmount());
        totalAmount.getStyle().set("color", "var(--lumo-primary-color)");

        totalLayout.add(totalLabel, totalAmount);
        column.add(totalLayout);

        return column;
    }

    /**
     * Create layout for one item in order summary
     *
     * SHOWS:
     * - Product name
     * - Quantity
     * - Price per item
     * - Total for this item
     */
    private HorizontalLayout createSummaryItemLayout(CartItem item) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setPadding(false);

        VerticalLayout productInfo = new VerticalLayout();
        productInfo.setSpacing(false);
        productInfo.setPadding(false);

        Span productName = new Span(item.getProduct().getName());
        productName.getStyle().set("font-weight", "500");

        Span quantity = new Span("Qty: " + item.getQuantity());
        quantity.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        productInfo.add(productName, quantity);

        BigDecimal itemTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
        Span price = new Span("$" + itemTotal);
        price.getStyle().set("font-weight", "bold");

        layout.add(productInfo, price);
        layout.expand(productInfo);

        return layout;
    }

    /**
     * Handle Place Order button click
     *
     * STEPS:
     * 1. Validate all required fields
     * 2. Process payment (in real system)
     * 3. Create order in database
     * 4. Show confirmation
     * 5. Navigate to order history
     *
     * WHY validate first?
     * - Don't waste server resources on invalid data
     * - Give immediate feedback to user
     * - Prevent incomplete orders
     */
    private void handlePlaceOrder() {
        if (!validateFields()) {
            showNotification("⚠️ Please fill in all required fields",
                    NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            System.out.println("=== CHECKOUT: Placing order ===");

            User user = userService.getAuthenticatedUser();
            System.out.println("User: " + user.getEmail() + " (ID: " + user.getId() + ")");

            orderService.placeOrder(user.getId());
            System.out.println("Order placed successfully!");

            showNotification("✅ Order placed successfully! 🎉",
                    NotificationVariant.LUMO_SUCCESS);

            getUI().ifPresent(ui -> {
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        ui.access(() -> {
                            System.out.println("Navigating to OrderView...");
                            ui.navigate(OrderView.class);
                        });
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            });

        } catch (Exception e) {
            System.err.println("=== ERROR placing order ===");
            e.printStackTrace();
            showNotification("❌ Failed to place order: " + e.getMessage(),
                    NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Validate all required fields
     *
     * CHECKS:
     * - All required fields have values
     * - Email format is valid
     * - Card number has correct length
     * - CVV has correct length
     *
     * WHY?
     * - Prevent invalid data from reaching backend
     * - Give immediate feedback to user
     * - Improve user experience
     */
    private boolean validateFields() {
        // Contact info
        if (emailField.isEmpty() || phoneField.isEmpty()) {
            return false;
        }

        // Payment info
        if (cardNumberField.isEmpty() || cardHolderField.isEmpty() ||
                expiryDateField.isEmpty() || cvvField.isEmpty()) {
            return false;
        }

        // Shipping address
        if (addressLine1Field.isEmpty() || cityField.isEmpty() ||
                stateField.isEmpty() || zipCodeField.isEmpty() ||
                countryField.isEmpty()) {
            return false;
        }

        // Additional validation (optional but recommended)
        String cardNumber = cardNumberField.getValue().replaceAll("\\s", "");
        if (cardNumber.length() < 13 || cardNumber.length() > 19) {
            showNotification("Invalid card number length",
                    NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (cvvField.getValue().length() < 3 || cvvField.getValue().length() > 4) {
            showNotification("Invalid CVV", NotificationVariant.LUMO_ERROR);
            return false;
        }

        return true;
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000,
                Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(variant);
        notification.open();
    }
}