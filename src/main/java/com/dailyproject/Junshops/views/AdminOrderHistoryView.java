package com.dailyproject.Junshops.views;

import com.dailyproject.Junshops.dto.OrderDto;
import com.dailyproject.Junshops.dto.OrderItemDto;
import com.dailyproject.Junshops.enums.OrderStatus;
import com.dailyproject.Junshops.service.order.IOrderService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * AdminOrderHistoryView - View ALL orders from ALL users
 *
 * FEATURES:
 * - See all orders in system
 * - Sort by date (newest first)
 * - Filter by order ID, user, status
 * - Expand to see order items
 *
 * SECURITY:
 * - Only admins can see ALL orders
 * - Regular users see only their orders (OrderView)
 *
 * DIFFERENCE FROM OrderView:
 * - OrderView: User sees THEIR orders only
 * - AdminOrderHistoryView: Admin sees ALL orders
 */
@Route(value = "admin/orders", layout = MainLayout.class)
@PageTitle("Order History | Jun-Shops Admin")
@RolesAllowed("ADMIN")  // ✅ CRITICAL: Admin only!
public class AdminOrderHistoryView extends VerticalLayout {

    private final IOrderService orderService;

    private final Grid<OrderDto> grid = new Grid<>(OrderDto.class, false);
    private final TextField searchField = new TextField();

    private List<OrderDto> allOrders = new ArrayList<>();

    public AdminOrderHistoryView(IOrderService orderService) {
        this.orderService = orderService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        H2 title = new H2("📊 Order History (All Users)");
        add(title);

        add(createToolbar());
        configureGrid();
        add(grid);

        loadAllOrders();
    }

    /**
     * Create toolbar with search and refresh
     */
    private HorizontalLayout createToolbar() {
        searchField.setPlaceholder("Search by order ID, user ID, or status...");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("400px");
        searchField.addValueChangeListener(e -> filterOrders());

        Button refreshButton = new Button("Refresh", new Icon(VaadinIcon.REFRESH));
        refreshButton.addClickListener(e -> loadAllOrders());

        HorizontalLayout toolbar = new HorizontalLayout(searchField, refreshButton);
        toolbar.setAlignItems(Alignment.CENTER);
        return toolbar;
    }

    /**
     * Configure the orders grid
     *
     * COLUMNS:
     * - Order ID
     * - User ID
     * - Order Date
     * - Status (with color badge)
     * - Total Amount
     * - Items (expandable details)
     */
    private void configureGrid() {
        grid.addClassName("orders-grid");
        grid.setSizeFull();

        //Order # column
        grid.addColumn(OrderDto::getOrderId)
                .setHeader("Order #")
                .setWidth("100px")
                .setFlexGrow(0)
                .setSortable(true);

        grid.addColumn(order -> {
            // Returns user name or unknown if missing
            if (order.getUserFirstName() != null && order.getUserLastName() != null) {
                return order.getUserFirstName() + " " + order.getUserLastName();
            } else {
                return "Unknown User";
            }
                })
                .setHeader("Customer")
                .setAutoWidth(true)
                .setSortable(true);

        // Email column
        grid.addColumn(order -> order.getUserEmail() != null ? order.getUserEmail() : "N/A")
                .setHeader("Email")
                .setAutoWidth(true);
        //Date column
        grid.addColumn(order ->
                order.getOrderDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
        ).setHeader("Date").setAutoWidth(true);

        // Status column with colored badge
        grid.addColumn(new ComponentRenderer<>(order ->
                createStatusBadge(order.getOrderStatus())
        )).setHeader("Status").setWidth("150px");

        //Total amount column
        grid.addColumn(order -> "$" + order.getTotalAmount())
                .setHeader("Total")
                .setWidth("120px")
                .setFlexGrow(0);

        // Order items (expandable)
        grid.setItemDetailsRenderer(new ComponentRenderer<>(this::createOrderDetailsPanel));

        //View Items button opens dialog
        grid.addColumn(new ComponentRenderer<>(order -> {
            Button viewButton = new Button("View Items", new Icon(VaadinIcon.EYE));
            viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            viewButton.addClickListener(e-> openOrderDetailsDialog(order));
            return viewButton;
        })).setHeader("Actions").setWidth("150px").setFlexGrow(0);
    }

    /**
     * Load ALL orders from database
     *
     * NOTE: We need a new method in OrderService to get all orders
     */
    private void loadAllOrders() {
        try {
            allOrders = orderService.getAllOrders();

            grid.setItems(allOrders);

            showNotification("Loaded " + allOrders.size() + " orders",
                    NotificationVariant.LUMO_SUCCESS);

            // For now, empty grid
            grid.setItems(allOrders);

        } catch (Exception e) {
            showNotification("Error loading orders: " + e.getMessage(),
                    NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Filter orders based on search term
     */
    private void filterOrders() {
        String searchTerm = searchField.getValue().toLowerCase();

        if (searchTerm.isEmpty()) {
            grid.setItems(allOrders);
            return;
        }

        List<OrderDto> filtered = allOrders.stream()
                .filter(order ->
                        String.valueOf(order.getOrderId()).contains(searchTerm) ||
                                String.valueOf(order.getUserId()).contains(searchTerm) ||
                                order.getOrderStatus().name().toLowerCase().contains(searchTerm)
                )
                .toList();

        grid.setItems(filtered);
    }

    /**
     * Open dialog to show order details
     *
     * DISPLAYS:
     * - Order information (ID, date, status, total)
     * - Customer information
     * - List of all items in the order
     * - Item details (product name, brand, quantity, price)
     */
    private void openOrderDetailsDialog(OrderDto order) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setMaxHeight("80vh");

        // Header
        H2 title = new H2("Order #" + order.getOrderId());
        title.getStyle().set("margin-top", "0");

        // Close button
        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickListener(e -> dialog.close());

        HorizontalLayout headerLayout = new HorizontalLayout(title, closeButton);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        dialog.add(headerLayout);

        // Order information section
        VerticalLayout orderInfoLayout = createOrderInfoSection(order);
        dialog.add(orderInfoLayout);

        // Items section
        VerticalLayout itemsLayout = createItemsSection(order);
        dialog.add(itemsLayout);

        dialog.open();
    }

    /**
     * Create order information section
     *
     * SHOWS:
     * - Customer name and email
     * - Order date
     * - Status
     * - Total amount
     */
    private VerticalLayout createOrderInfoSection(OrderDto order) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.setSpacing(true);
        section.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "8px");

        com.vaadin.flow.component.html.H3 sectionTitle = new com.vaadin.flow.component.html.H3("Order Information");
        sectionTitle.getStyle().set("margin-top", "0");

        // Customer info
        HorizontalLayout customerLayout = new HorizontalLayout();
        Span customerLabel = new Span("Customer: ");
        customerLabel.getStyle().set("font-weight", "bold");

        String customerName = "Unknown User";
        if (order.getUserFirstName() != null && order.getUserLastName() != null) {
            customerName = order.getUserFirstName() + " " + order.getUserLastName();
        }
        String customerEmail = order.getUserEmail() != null ? order.getUserEmail() : "N/A";

        Span customerValue = new Span(customerName + " (" + customerEmail + ")");
        customerLayout.add(customerLabel, customerValue);

        // Order date
        HorizontalLayout dateLayout = new HorizontalLayout();
        Span dateLabel = new Span("Order Date: ");
        dateLabel.getStyle().set("font-weight", "bold");
        Span dateValue = new Span(
                order.getOrderDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm"))
        );
        dateLayout.add(dateLabel, dateValue);

        // Status
        HorizontalLayout statusLayout = new HorizontalLayout();
        Span statusLabel = new Span("Status: ");
        statusLabel.getStyle().set("font-weight", "bold");
        Span statusBadge = createStatusBadge(order.getOrderStatus());
        statusLayout.add(statusLabel, statusBadge);
        statusLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Total
        HorizontalLayout totalLayout = new HorizontalLayout();
        Span totalLabel = new Span("Total Amount: ");
        totalLabel.getStyle().set("font-weight", "bold");
        Span totalValue = new Span("$" + order.getTotalAmount());
        totalValue.getStyle()
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("color", "var(--lumo-primary-color)")
                .set("font-weight", "bold");
        totalLayout.add(totalLabel, totalValue);
        totalLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        section.add(sectionTitle, customerLayout, dateLayout, statusLayout, totalLayout);
        return section;
    }

    /**
     * Create items section
     *
     * SHOWS:
     * - List of all products in order
     * - Product name, brand
     * - Quantity ordered
     * - Price per item
     * - Total per item
     */
    private VerticalLayout createItemsSection(OrderDto order) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.setSpacing(true);

        int itemCount = (order.getOrderItems() != null) ? order.getOrderItems().size() : 0;
        com.vaadin.flow.component.html.H3 sectionTitle = new com.vaadin.flow.component.html.H3("Order Items (" + itemCount + ")");
        section.add(sectionTitle);

        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            Span emptyMessage = new Span("No items in this order");
            emptyMessage.getStyle().set("color", "var(--lumo-secondary-text-color)");
            section.add(emptyMessage);
            return section;
        }

        // Create item cards
        for (OrderItemDto item : order.getOrderItems()) {
            VerticalLayout itemCard = createItemCard(item);
            section.add(itemCard);
        }

        return section;
    }

    /**
     * Create a card for each order item
     *
     * DISPLAYS:
     * - Product name (large, bold)
     * - Brand (small, secondary color)
     * - Quantity and unit price (left side)
     * - Item total (right side, large, bold)
     */
    private VerticalLayout createItemCard(OrderItemDto item) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px")
                .set("background", "var(--lumo-base-color)")
                .set("margin-bottom", "10px");

        // Product name
        Span productName = new Span(item.getProductName());
        productName.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "var(--lumo-font-size-l)");

        // Brand
        Span brand = new Span(item.getProductBrand());
        brand.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        // Details layout (quantity and price)
        HorizontalLayout detailsLayout = new HorizontalLayout();
        detailsLayout.setWidthFull();
        detailsLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        detailsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        detailsLayout.setPadding(false);

        // Left side: Quantity and unit price
        VerticalLayout leftSide = new VerticalLayout();
        leftSide.setSpacing(false);
        leftSide.setPadding(false);

        Span quantity = new Span("Quantity: " + item.getQuantity());
        Span unitPrice = new Span("Unit Price: $" + item.getPrice());
        unitPrice.getStyle().set("color", "var(--lumo-secondary-text-color)");

        leftSide.add(quantity, unitPrice);

        // Right side: Total
        VerticalLayout rightSide = new VerticalLayout();
        rightSide.setSpacing(false);
        rightSide.setPadding(false);
        rightSide.setAlignItems(FlexComponent.Alignment.END);

        Span totalLabel = new Span("Total:");
        totalLabel.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        BigDecimal itemTotal = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
        Span totalAmount = new Span("$" + itemTotal);
        totalAmount.getStyle()
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)");

        rightSide.add(totalLabel, totalAmount);

        detailsLayout.add(leftSide, rightSide);
        detailsLayout.expand(leftSide);

        card.add(productName, brand, detailsLayout);
        return card;
    }

    /**
     * Create status badge with color coding
     */
    private Span createStatusBadge(OrderStatus status) {
        Span badge = new Span(status.name());
        badge.getElement().getThemeList().add("badge");

        String color;
        switch (status) {
            case PENDING:
                color = "var(--lumo-warning-color)";
                break;
            case PROCESSING:
                color = "var(--lumo-primary-color)";
                break;
            case SHIPPED:
                color = "var(--lumo-contrast-60pct)";
                break;
            case DELIVERED:
                color = "var(--lumo-success-color)";
                break;
            case CANCELLED:
                color = "var(--lumo-error-color)";
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
     * Create expandable details panel for order items
     */
    private VerticalLayout createOrderDetailsPanel(OrderDto order) {
        VerticalLayout details = new VerticalLayout();
        details.setPadding(true);
        details.setSpacing(true);

        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            details.add(new Span("No items in this order"));
            return details;
        }

        for (OrderItemDto item : order.getOrderItems()) {
            HorizontalLayout itemLayout = new HorizontalLayout();
            itemLayout.setWidthFull();
            itemLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
            itemLayout.getStyle()
                    .set("padding", "10px")
                    .set("border", "1px solid var(--lumo-contrast-10pct)")
                    .set("border-radius", "4px")
                    .set("margin-bottom", "5px");

            VerticalLayout productInfo = new VerticalLayout();
            productInfo.setSpacing(false);
            productInfo.setPadding(false);

            Span productName = new Span(item.getProductName());
            productName.getStyle().set("font-weight", "bold");

            Span brand = new Span(item.getProductBrand());
            brand.getStyle()
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("color", "var(--lumo-secondary-text-color)");

            productInfo.add(productName, brand);

            Span quantity = new Span("Qty: " + item.getQuantity());
            BigDecimal itemTotal = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
            Span price = new Span("$" + itemTotal);
            price.getStyle().set("font-weight", "bold");

            itemLayout.add(productInfo, quantity, price);
            itemLayout.expand(productInfo);

            details.add(itemLayout);
        }

        return details;
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000,
                Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(variant);
        notification.open();
    }
}
