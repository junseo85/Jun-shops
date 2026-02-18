package com.dailyproject.Junshops.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "cart", layout = MainLayout.class)
@PageTitle("Cart | Jun-Shops")
@PermitAll
public class CartView extends VerticalLayout {
    public CartView() {
        add(new H2("Shopping Cart - Coming Soon!"));
    }
}