package com.dailyproject.Junshops.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "categories", layout = MainLayout.class)
@PermitAll
public class CategoriesView extends VerticalLayout {

    public CategoriesView() {
        addClassName("categories-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        add(new H2("Categories - Coming Soon"));
    }
}
