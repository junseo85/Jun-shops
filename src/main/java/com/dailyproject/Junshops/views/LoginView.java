package com.dailyproject.Junshops.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Login | Jun-Shops")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    private final LoginForm loginForm = new LoginForm();


    public LoginView() {
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);

        H1 title = new H1("Jun-Shops");
        Paragraph hint = new Paragraph("Default credentials: user1@email.com / 123456");
        hint.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, hint, loginForm);
    }
        @Override
        public void beforeEnter(BeforeEnterEvent event) {
            // Show error message if login fails
            if (event.getLocation()
                    .getQueryParameters()
                    .getParameters()
                    .containsKey("error")) {
                loginForm.setError(true);
            }

    }
}