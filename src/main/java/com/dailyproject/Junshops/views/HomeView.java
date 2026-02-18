package com.dailyproject.Junshops.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Home | Jun-Shops")
@PermitAll
public class HomeView extends VerticalLayout {

    /**
     * Configures layout; adds welcome message and description
     */
    public HomeView() {
        setSpacing(false);
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");

        add(new H1("Welcome to Jun-Shops! 🛍️"));
        add(new Paragraph("Your one-stop eCommerce solution"));
    }
}