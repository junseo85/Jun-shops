package com.dailyproject.Junshops.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "orders", layout = MainLayout.class)
@PermitAll
public class OrdersView extends VerticalLayout {

    public OrdersView() {
        addClassName("orders-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        add(new H2("Orders - Coming Soon"));
    }
}
