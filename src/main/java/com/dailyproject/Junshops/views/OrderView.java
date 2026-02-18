package com.dailyproject.Junshops.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "orders", layout = MainLayout.class)
@PageTitle("Orders | Jun-Shops")
@PermitAll
public class OrderView extends VerticalLayout {
    public OrderView() {
        add(new H2("My Orders - Coming Soon!"));
    }
}