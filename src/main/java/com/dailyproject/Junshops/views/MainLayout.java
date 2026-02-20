package com.dailyproject.Junshops.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

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

    private void createDrawer() {
        VerticalLayout menu = new VerticalLayout();

        // Navigation links
        menu.add(new RouterLink("🏠 Home", HomeView.class));
        menu.add(new RouterLink("🛍️ Products", ProductListView.class));
        menu.add(new RouterLink("🛒 Cart", CartView.class));
        menu.add(new RouterLink("📦 Orders", OrderView.class));
        menu.add(new RouterLink("📁 Categories", CategoryView.class));

        addToDrawer(menu);
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