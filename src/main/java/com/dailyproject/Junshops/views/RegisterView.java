package com.dailyproject.Junshops.views;

import com.dailyproject.Junshops.request.CreateUserRequest;
import com.dailyproject.Junshops.service.user.IUserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("register")
@PageTitle("Register | Jun-Shops")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private final IUserService userService;

    private final Binder<CreateUserRequest> binder = new Binder<>(CreateUserRequest.class);

    private final TextField firstNameField = new TextField("First Name");
    private final TextField lastNameField = new TextField("Last Name");
    private final EmailField emailField = new EmailField("Email");
    private final PasswordField passwordField = new PasswordField("Password");
    private final PasswordField confirmPasswordField = new PasswordField("Confirm Password");
    private final Button registerButton = new Button("Register");

    public RegisterView(IUserService userService) {
        this.userService = userService;

        addClassName("register-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        configureForm();
        configureBinder();

        H1 title = new H1("Create Account 📝");
        Paragraph subtitle = new Paragraph("Join Jun-Shops and start shopping!");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span loginLink = new Span("Already have an account? ");
        RouterLink loginRouterLink = new RouterLink("Login here", LoginView.class);
        loginLink.add(loginRouterLink);
        loginLink.getStyle().set("margin-top", "20px");

        add(title, subtitle, firstNameField, lastNameField, emailField,
                passwordField, confirmPasswordField, registerButton, loginLink);
    }

    private void configureForm() {
        firstNameField.setWidth("350px");
        firstNameField.setPlaceholder("John");
        firstNameField.setRequired(true);
        firstNameField.setClearButtonVisible(true);
        firstNameField.setAutofocus(true);

        lastNameField.setWidth("350px");
        lastNameField.setPlaceholder("Doe");
        lastNameField.setRequired(true);
        lastNameField.setClearButtonVisible(true);

        emailField.setWidth("350px");
        emailField.setPlaceholder("john.doe@email.com");
        emailField.setRequired(true);
        emailField.setClearButtonVisible(true);
        emailField.setErrorMessage("Please enter a valid email address");

        passwordField.setWidth("350px");
        passwordField.setPlaceholder("Enter password");
        passwordField.setRequired(true);
        passwordField.setHelperText("Password must be at least 6 characters");

        confirmPasswordField.setWidth("350px");
        confirmPasswordField.setPlaceholder("Confirm password");
        confirmPasswordField.setRequired(true);

        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        registerButton.setWidth("350px");
        registerButton.addClickListener(e -> handleRegister());
    }

    private void configureBinder() {
        binder.forField(firstNameField)
                .asRequired("First name is required")
                .withValidator(name -> name.length() >= 2, "First name must be at least 2 characters")
                .bind(CreateUserRequest::getFirstName, CreateUserRequest::setFirstName);

        binder.forField(lastNameField)
                .asRequired("Last name is required")
                .withValidator(name -> name.length() >= 2, "Last name must be at least 2 characters")
                .bind(CreateUserRequest::getLastName, CreateUserRequest::setLastName);

        binder.forField(emailField)
                .asRequired("Email is required")
                .withValidator(new EmailValidator("Please enter a valid email address"))
                .bind(CreateUserRequest::getEmail, CreateUserRequest::setEmail);

        binder.forField(passwordField)
                .asRequired("Password is required")
                .withValidator(password -> password.length() >= 6,
                        "Password must be at least 6 characters")
                .bind(CreateUserRequest::getPassword, CreateUserRequest::setPassword);

        binder.forField(confirmPasswordField)
                .asRequired("Please confirm your password")
                .withValidator(confirmPassword -> confirmPassword.equals(passwordField.getValue()),
                        "Passwords do not match")
                .bind(user -> passwordField.getValue(), (user, value) -> {});
    }

    private void handleRegister() {
        try {
            CreateUserRequest request = new CreateUserRequest();
            binder.writeBean(request);

            if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
                showError("Passwords do not match");
                return;
            }

            userService.createUser(request);

            showSuccess("Registration successful! Redirecting to login...");

            getUI().ifPresent(ui -> {
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        ui.access(() -> ui.navigate(LoginView.class));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            });

        } catch (ValidationException e) {
            showError("Please fix the errors in the form");
        } catch (Exception e) {
            showError("Registration failed: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Notification notification = new Notification(message, 4000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.open();
    }

    private void showSuccess(String message) {
        Notification notification = new Notification(message, 3000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.open();
    }
}