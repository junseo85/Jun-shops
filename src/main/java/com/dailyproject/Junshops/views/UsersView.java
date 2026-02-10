package com.dailyproject.Junshops.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "users", layout = MainLayout.class)
@RolesAllowed("ROLE_ADMIN")
public class UsersView extends VerticalLayout {

    public UsersView() {
        addClassName("users-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        add(new H2("Users Management - Admin Only"));
    }
}
