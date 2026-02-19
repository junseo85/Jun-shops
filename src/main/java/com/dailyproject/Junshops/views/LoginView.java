package com.dailyproject.Junshops.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
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

        // Configure login form
        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);

        // Title and hint
        H1 title = new H1("Jun-Shops 🛍️");
        Paragraph hint = new Paragraph("Default credentials: user1@email.com / 123456");
        hint.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // Register link
        Span registerLink = new Span("Don't have an account? ");
        RouterLink registerRouterLink = new RouterLink("Register here", RegisterView.class);
        registerLink.add(registerRouterLink);
        registerLink.getStyle()
                .set("margin-top", "20px")
                .set("font-size", "var(--lumo-font-size-m)");

        // Add all components to layout
        add(title, hint, loginForm, registerLink);
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