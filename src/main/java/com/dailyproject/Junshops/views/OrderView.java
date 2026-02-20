package com.dailyproject.Junshops.views;

import com.dailyproject.Junshops.dto.OrderDto;
import com.dailyproject.Junshops.dto.OrderItemDto;
import com.dailyproject.Junshops.enums.OrderStatus;
import com.dailyproject.Junshops.model.User;
import com.dailyproject.Junshops.service.order.IOrderService;
import com.dailyproject.Junshops.service.user.IUserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "orders", layout = MainLayout.class)
@PageTitle("Orders | Jun-Shops")
@PermitAll
public class OrderView extends VerticalLayout {

    private final IOrderService orderService;
    private final IUserService userService;

    private VerticalLayout ordersLayout;

    /**
     * Configures layout; displays title; loads orders
     */
    public OrderView(IOrderService orderService, IUserService userService) {
        this.orderService = orderService;
        this.userService = userService;

        setSpacing(true);
        setPadding(true);
        setMaxWidth("1200px");
        setAlignSelf(Alignment.CENTER);

        H2 title = new H2("📦 My Orders");
        add(title);

        ordersLayout = new VerticalLayout();
        ordersLayout.setSpacing(true);
        ordersLayout.setPadding(false);
        ordersLayout.setWidthFull();

        add(ordersLayout);

        loadOrders();
    }

    private void loadOrders() {
        ordersLayout.removeAll();

        try {
            System.out.println("=== ORDERVIEW: Loading orders ===");

            User user = userService.getAuthenticatedUser();
            System.out.println("User: " + user.getEmail() + " (ID: " + user.getId() + ")");

            List<OrderDto> orders = orderService.getUserOrders(user.getId());
            System.out.println("Loaded " + (orders != null ? orders.size() : "null") + " orders");

            if (orders == null || orders.isEmpty()) {
                System.out.println("No orders found, showing empty state");
                showEmptyOrders();
                return;
            }

            // ✅ FIX: Use stream().sorted() instead of .sort()
            // This creates a NEW sorted list instead of modifying the immutable one
            List<OrderDto> sortedOrders = orders.stream()
                    .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
                    .toList();

            System.out.println("Displaying " + sortedOrders.size() + " orders");
            sortedOrders.forEach(order -> {
                System.out.println("  - Order #" + order.getOrderId());
                addOrderCard(order);
            });

            System.out.println("=== ORDERVIEW: Orders loaded successfully ===");

        } catch (Exception e) {
            System.err.println("=== ORDERVIEW ERROR ===");
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();

            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.equals("null")) {
                errorMsg = "Unknown error occurred. Check console for details.";
            }

            showNotification("Error loading orders: " + errorMsg, NotificationVariant.LUMO_ERROR);
            showEmptyOrders();
        }
    }

    /**
     * Displays placeholder UI when no orders exist
     */
    private void showEmptyOrders() {
        VerticalLayout empty = new VerticalLayout();
        empty.setAlignItems(Alignment.CENTER);
        empty.setJustifyContentMode(JustifyContentMode.CENTER);
        empty.setPadding(true);

        Icon icon = VaadinIcon.PACKAGE.create();
        icon.setSize("64px");
        icon.getStyle().set("color", "var(--lumo-contrast-30pct)");

        H3 message = new H3("No orders yet");
        message.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span subMessage = new Span("Start shopping to create your first order!");
        subMessage.getStyle().set("color", "var(--lumo-tertiary-text-color)");

        Button shopButton = new Button("Browse Products", e ->
                getUI().ifPresent(ui -> ui.navigate(ProductListView.class))
        );
        shopButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        empty.add(icon, message, subMessage, shopButton);
        ordersLayout.add(empty);
    }

    /**
     * Adds order details card to orders layout
     */
    private void addOrderCard(OrderDto order) {
        VerticalLayout card = new VerticalLayout();
        card.setSpacing(false);
        card.setPadding(true);
        card.setWidthFull();
        card.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px") // ← Rounded corners
                .set("background", "var(--lumo-base-color)")
                .set("margin-bottom", "15px"); // ← Space between cards

        // Header with order info
        HorizontalLayout header = createOrderHeader(order);
        card.add(header);

        // Order items details (expandable)
        Details itemsDetails = createOrderItemsDetails(order);
        card.add(itemsDetails);

        ordersLayout.add(card);
    }

    /**
     * Creates header displaying order details and total
     */
    private HorizontalLayout createOrderHeader(OrderDto order) {
        HorizontalLayout header = new HorizontalLayout();
        // Layout configuration
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN); // ← Space between items
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setPadding(false);

        // Left side: Order info
        VerticalLayout leftInfo = new VerticalLayout();
        leftInfo.setSpacing(false);
        leftInfo.setPadding(false);

        H3 orderNumber = new H3("Order #" + order.getOrderId());
        orderNumber.getStyle().set("margin", "0");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        Span orderDate = new Span("Placed on: " + order.getOrderDate().format(formatter));
        orderDate.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        leftInfo.add(orderNumber, orderDate);

        // Middle: Status badge
        Span statusBadge = createStatusBadge(order.getOrderStatus());

        // Right side: Total amount
        VerticalLayout rightInfo = new VerticalLayout();
        rightInfo.setSpacing(false);
        rightInfo.setPadding(false);
        rightInfo.setAlignItems(FlexComponent.Alignment.END);

        Span totalLabel = new Span("Total");
        totalLabel.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        Span totalAmount = new Span("$" + order.getTotalAmount());
        totalAmount.getStyle()
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)");

        rightInfo.add(totalLabel, totalAmount);

        header.add(leftInfo, statusBadge, rightInfo);
        header.expand(leftInfo);

        return header;
    }

    /**
     * Creates styled badge reflecting order status
     */
    private Span createStatusBadge(OrderStatus status) {
        Span badge = new Span(status.name()); // ← "PENDING", "SHIPPED", etc.
        badge.getElement().getThemeList().add("badge");
        // Color based on status
        String color;
        switch (status) {
            case PENDING:
                color = "var(--lumo-warning-color)"; // ← Yellow/Orange
                break;
            case PROCESSING:
                color = "var(--lumo-primary-color)"; // ← Blue
                break;
            case SHIPPED:
                color = "var(--lumo-contrast-60pct)"; // ← Gray
                break;
            case DELIVERED:
                color = "var(--lumo-success-color)"; // ← Green
                break;
            case CANCELLED:
                color = "var(--lumo-error-color)"; // ← Red
    // Creates expandable order item list from order data
                break;
            default:
                color = "var(--lumo-contrast-50pct)";
        }

        badge.getStyle()
                .set("background-color", color)
                .set("color", "white")
                .set("padding", "5px 15px")
                .set("border-radius", "12px")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "500");

        return badge;
    }

    /**
     * Creates expandable order items details component
     */
    private Details createOrderItemsDetails(OrderDto order) {
        Details details = new Details("Order Items (" + order.getOrderItems().size() + ")");
        details.setWidthFull();

        VerticalLayout itemsLayout = new VerticalLayout();
        itemsLayout.setSpacing(true);
        itemsLayout.setPadding(false);
        // Add each item
        for (OrderItemDto item : order.getOrderItems()) {
            HorizontalLayout itemLayout = createOrderItemLayout(item);
            itemsLayout.add(itemLayout);
        }

        details.setContent(itemsLayout);
        return details;
    }

    /**
     * Creates styled layout for order item details
     */
    private HorizontalLayout createOrderItemLayout(OrderItemDto item) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setPadding(true);
        layout.getStyle()
                .set("border", "1px solid var(--lumo-contrast-5pct)")
                .set("border-radius", "4px")
                .set("background", "var(--lumo-contrast-5pct)");

        // Product info
        VerticalLayout productInfo = new VerticalLayout();
        productInfo.setSpacing(false);
        productInfo.setPadding(false);

        Span productName = new Span(item.getProductName());
        productName.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "var(--lumo-font-size-m)");

        Span productBrand = new Span(item.getProductBrand());
        productBrand.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        productInfo.add(productName, productBrand);

        // Quantity
        Span quantity = new Span("Qty: " + item.getQuantity());
        quantity.getStyle()
                .set("font-size", "var(--lumo-font-size-m)")
                .set("font-weight", "500");

        // Price
        BigDecimal itemTotal = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
        Span price = new Span("$" + itemTotal);
        price.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)");

        layout.add(productInfo, quantity, price);
        layout.expand(productInfo);

        return layout;
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000,
                Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(variant);
        notification.open();
    }
}