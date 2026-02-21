package com.dailyproject.Junshops.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;


/**
 * MainLayout - Application shell with navigation
 *
 * FEATURES:
 * - Top navigation bar
 * - Side drawer menu
 * - Role-based menu visibility
 * - Admin-only sections
 */
public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }
    /**
     * Create top navigation bar
     *
     * SHOWS:
     * - App title
     * - Drawer toggle button
     */

    private void createHeader() {
        H1 logo = new H1("Jun-Shops 🛍️");
        logo.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.MEDIUM
        );

        // Logout button
        Button logoutButton = new Button("Logout", e -> logout());

        HorizontalLayout header = new HorizontalLayout(
                new DrawerToggle(),
                logo,
                logoutButton
        );
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames(
                LumoUtility.Padding.Vertical.NONE,
                LumoUtility.Padding.Horizontal.LARGE
        );
        addToNavbar(header);
    }



    /**
     * Create side navigation drawer
     *
     * MENU STRUCTURE:
     * - Regular user items (visible to all)
     * - Admin section (visible only to ADMIN role)
     *
     * WHY role-based?
     * - Security: Don't show admin options to regular users
     * - UX: Cleaner menu for non-admins
     * - Compliance: Separation of duties
     */

    private void createDrawer() {
        VerticalLayout menu = new VerticalLayout();
        menu.setSpacing(true);
        menu.setPadding(true);

        // ===== USER SECTION (Visible to all) =====
        Span userSection = new Span("🛍️ Shopping");
        userSection.getStyle()
                .set("font-weight", "bold")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("text-transform", "uppercase");

        menu.add(userSection);
        menu.add(new RouterLink("🏠 Home", HomeView.class));
        menu.add(new RouterLink("🛍️ Products", ProductListView.class));
        menu.add(new RouterLink("🛒 Cart", CartView.class));
        menu.add(new RouterLink("📦 My Orders", OrderView.class));
        menu.add(new RouterLink("📁 Categories", CategoryView.class));

        // ===== ADMIN SECTION (Visible only to admins) =====
        if (isAdmin()) {
            menu.add(new Hr());  // Divider

            Span adminSection = new Span("⚙️ Administration");
            adminSection.getStyle()
                    .set("font-weight", "bold")
                    .set("color", "var(--lumo-error-color)")
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("text-transform", "uppercase");

            menu.add(adminSection);
            menu.add(new RouterLink("👥 Users", UsersManagementView.class));
            menu.add(new RouterLink("📊 Order History", AdminOrderHistoryView.class));
        }

        addToDrawer(menu);
    }
    /**
     * Check if current user has ADMIN role
     *
     * HOW IT WORKS:
     * 1. Get current authentication from Spring Security
     * 2. Get authorities (roles) assigned to user
     * 3. Check if any authority contains "ADMIN"
     *
     * WHY?
     * - Control menu visibility
     * - Show/hide admin features
     * - Client-side role check (server still validates!)
     *
     * @return true if user has ADMIN role
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

    private void logout() {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(
                VaadinServletRequest.getCurrent().getHttpServletRequest(),
                null,
                SecurityContextHolder.getContext().getAuthentication()
        );
        getUI().ifPresent(ui -> ui.navigate("login"));
    }
}