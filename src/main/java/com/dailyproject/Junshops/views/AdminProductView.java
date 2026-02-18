package com.dailyproject.Junshops.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin/products", layout = MainLayout.class)
@PageTitle("Manage Products | Jun-Shops")
@RolesAllowed("ADMIN")  // Only admins can access
public class AdminProductView extends VerticalLayout {
    public AdminProductView() {
        add(new H2("Admin: Manage Products - Coming Soon!"));
    }
}