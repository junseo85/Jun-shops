package com.dailyproject.Junshops.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;

public class MainLayout extends AppLayout {

    private final AuthenticationContext authenticationContext;

    public MainLayout(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Jun Shops");
        logo.addClassNames("text-l", "m-m");

        Button logout = new Button("Logout", click ->
                authenticationContext.logout());

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, logout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private void createDrawer() {
        RouterLink productsLink = new RouterLink("Products", ProductsView.class);
        RouterLink categoriesLink = new RouterLink("Categories", CategoriesView.class);
        RouterLink cartLink = new RouterLink("Cart", CartView.class);
        RouterLink ordersLink = new RouterLink("Orders", OrdersView.class);

        VerticalLayout drawerContent = new VerticalLayout(
                productsLink,
                categoriesLink,
                cartLink,
                ordersLink
        );

        // Add Users link only if user has admin role
        authenticationContext.getGrantedRoles().stream()
                .filter(role -> role.equals("ROLE_ADMIN"))
                .findFirst()
                .ifPresent(role -> {
                    RouterLink usersLink = new RouterLink("Users", UsersView.class);
                    drawerContent.add(usersLink);
                });

        addToDrawer(drawerContent);
    }
}
