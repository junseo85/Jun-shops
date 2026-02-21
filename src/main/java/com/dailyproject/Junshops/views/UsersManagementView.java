package com.dailyproject.Junshops.views;

import com.dailyproject.Junshops.model.Role;
import com.dailyproject.Junshops.model.User;
import com.dailyproject.Junshops.request.CreateUserRequest;
import com.dailyproject.Junshops.request.UserUpdateRequest;
import com.dailyproject.Junshops.service.user.IUserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * UsersManagementView - Admin panel for user management
 *
 * FEATURES:
 * - View all users in a table
 * - Create new users
 * - Edit existing users
 * - Delete users
 * - Search/filter users
 *
 * SECURITY:
 * - Only accessible by ADMIN role
 * - @RolesAllowed annotation enforces this
 *
 * WHY admin only?
 * - Sensitive data (user list)
 * - Critical operations (delete, modify)
 * - Privacy concerns
 */
@Route(value = "admin/users", layout = MainLayout.class)
@PageTitle("Users Management | Jun-Shops")
@RolesAllowed("ADMIN")  // ✅ CRITICAL: Only admins can access
public class UsersManagementView extends VerticalLayout {

    private final IUserService userService;

    private final Grid<User> grid = new Grid<>(User.class, false);
    private final TextField searchField = new TextField();

    public UsersManagementView(IUserService userService) {
        this.userService = userService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        H2 title = new H2("👥 Users Management");
        add(title);

        add(createToolbar());
        configureGrid();
        add(grid);

        loadUsers();
    }

    /**
     * Create toolbar with search and add button
     *
     * FEATURES:
     * - Search field (filter users)
     * - Add User button (opens dialog)
     */
    private HorizontalLayout createToolbar() {
        searchField.setPlaceholder("Search by name or email...");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("300px");
        searchField.addValueChangeListener(e -> filterUsers());

        Button addButton = new Button("Add User", new Icon(VaadinIcon.PLUS));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openCreateUserDialog());

        Button refreshButton = new Button("Refresh", new Icon(VaadinIcon.REFRESH));
        refreshButton.addClickListener(e -> loadUsers());

        HorizontalLayout toolbar = new HorizontalLayout(searchField, addButton, refreshButton);
        toolbar.setAlignItems(Alignment.CENTER);
        return toolbar;
    }

    /**
     * Configure the user grid (table)
     *
     * COLUMNS:
     * - ID
     * - First Name
     * - Last Name
     * - Email
     * - Roles
     * - Actions (Edit, Delete buttons)
     */
    private void configureGrid() {
        grid.addClassName("users-grid");
        grid.setSizeFull();

        grid.addColumn(User::getId)
                .setHeader("ID")
                .setWidth("80px")
                .setFlexGrow(0);

        grid.addColumn(User::getFirstName)
                .setHeader("First Name")
                .setAutoWidth(true);

        grid.addColumn(User::getLastName)
                .setHeader("Last Name")
                .setAutoWidth(true);

        grid.addColumn(User::getEmail)
                .setHeader("Email")
                .setAutoWidth(true);

        // Roles column (show role badges)
        grid.addColumn(new ComponentRenderer<>(user -> {
            HorizontalLayout rolesLayout = new HorizontalLayout();
            rolesLayout.setSpacing(true);

            user.getRoles().forEach(role -> {
                Span badge = new Span(role.getName());
                badge.getElement().getThemeList().add("badge");
                badge.getStyle()
                        .set("background-color", "var(--lumo-contrast-20pct)")
                        .set("padding", "3px 8px")
                        .set("border-radius", "4px")
                        .set("font-size", "var(--lumo-font-size-xs)");
                rolesLayout.add(badge);
            });

            return rolesLayout;
        })).setHeader("Roles").setAutoWidth(true);

        //  Changed "Actions" to "Edit"
        grid.addColumn(new ComponentRenderer<>(user -> {
            Button editButton = new Button("Edit", new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            editButton.addClickListener(e -> openEditUserDialog(user));

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> confirmDelete(user));

            HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
            actions.setSpacing(true);
            return actions;
        })).setHeader("Edit").setWidth("200px").setFlexGrow(0);  // ✅ Changed header
    }

    /**
     * Load all users from database
     *
     * NOTE: In a real system with many users, you'd want:
     * - Pagination
     * - Server-side filtering
     * - Lazy loading
     */
    private void loadUsers() {
        try {
            List<User> users = userService.getAllUsers();
            grid.setItems(users);
        } catch (Exception e) {
            showNotification("Error loading users: " + e.getMessage(),
                    NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Filter users based on search field
     *
     * FILTERS BY:
     * - First name
     * - Last name
     * - Email
     */
    private void filterUsers() {
        String searchTerm = searchField.getValue().toLowerCase();

        if (searchTerm.isEmpty()) {
            loadUsers();
            return;
        }

        List<User> users = userService.getAllUsers();
        List<User> filtered = users.stream()
                .filter(user ->
                        user.getFirstName().toLowerCase().contains(searchTerm) ||
                                user.getLastName().toLowerCase().contains(searchTerm) ||
                                user.getEmail().toLowerCase().contains(searchTerm))
                .toList();

        grid.setItems(filtered);
    }

    /**
     * Open dialog to create new user
     *
     * FORM FIELDS:
     * - First Name
     * - Last Name
     * - Email
     * - Password
     *
     * WHY dialog?
     * - Keeps user on same page
     * - Focus on form
     * - Better UX than separate page
     */
    private void openCreateUserDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Create New User");

        FormLayout formLayout = new FormLayout();

        TextField firstNameField = new TextField("First Name");
        firstNameField.setRequired(true);

        TextField lastNameField = new TextField("Last Name");
        lastNameField.setRequired(true);

        EmailField emailField = new EmailField("Email");
        emailField.setRequired(true);

        PasswordField passwordField = new PasswordField("Password");
        passwordField.setRequired(true);
        passwordField.setHelperText("Minimum 6 characters");

        formLayout.add(firstNameField, lastNameField, emailField, passwordField);

        Button saveButton = new Button("Create User", e -> {
            if (firstNameField.isEmpty() || lastNameField.isEmpty() ||
                    emailField.isEmpty() || passwordField.isEmpty()) {
                showNotification("Please fill all required fields", NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                CreateUserRequest request = new CreateUserRequest();
                request.setFirstName(firstNameField.getValue());
                request.setLastName(lastNameField.getValue());
                request.setEmail(emailField.getValue());
                request.setPassword(passwordField.getValue());

                userService.createUser(request);
                showNotification("User created successfully!", NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                loadUsers();
            } catch (Exception ex) {
                showNotification("Error creating user: " + ex.getMessage(),
                        NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonsLayout.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout dialogLayout = new VerticalLayout(formLayout, buttonsLayout);
        dialog.add(dialogLayout);

        dialog.open();
    }

    /**
     * Open dialog to edit existing user
     *
     * PRE-POPULATED:
     * - Current first name
     * - Current last name
     *
     * NOTE: Email not editable (unique identifier)
     * NOTE: Password not shown (security)
     */
    private void openEditUserDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit User: " + user.getEmail());
        dialog.setWidth("500px");

        FormLayout formLayout = new FormLayout();

        // Name fields
        TextField firstNameField = new TextField("First Name");
        firstNameField.setValue(user.getFirstName());
        firstNameField.setRequired(true);

        TextField lastNameField = new TextField("Last Name");
        lastNameField.setValue(user.getLastName());
        lastNameField.setRequired(true);

        Span emailDisplay = new Span("Email: " + user.getEmail());
        emailDisplay.getStyle().set("color", "var(--lumo-secondary-text-color)");

        formLayout.add(firstNameField, lastNameField, emailDisplay);

        // ✅ NEW: Role management section
        H3 rolesHeader = new H3("User Roles");
        rolesHeader.getStyle().set("margin-top", "20px");

        CheckboxGroup<String> rolesCheckbox = new CheckboxGroup<>();
        rolesCheckbox.setLabel("Assign Roles:");
        rolesCheckbox.setItems("ROLE_USER", "ROLE_ADMIN");
        rolesCheckbox.setItemLabelGenerator(role ->
                role.equals("ROLE_ADMIN") ? "Administrator" : "Regular User"
        );

        // Pre-select current roles
        Set<String> currentRoles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        rolesCheckbox.setValue(currentRoles);

        Span rolesHelper = new Span("⚠️ Admin users have full access to all features");
        rolesHelper.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-error-color)");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.add(formLayout, rolesHeader, rolesCheckbox, rolesHelper);

        Button saveButton = new Button("Save Changes", e -> {
            if (firstNameField.isEmpty() || lastNameField.isEmpty()) {
                showNotification("Please fill all required fields", NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                // Update basic info
                UserUpdateRequest request = new UserUpdateRequest();
                request.setFirstName(firstNameField.getValue());
                request.setLastName(lastNameField.getValue());
                userService.updateUser(request, user.getId());

                // ✅ NEW: Update roles
                Set<String> selectedRoles = rolesCheckbox.getValue();
                userService.updateUserRoles(user.getId(), selectedRoles);

                showNotification("User updated successfully!", NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                loadUsers();
            } catch (Exception ex) {
                showNotification("Error updating user: " + ex.getMessage(),
                        NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonsLayout.setJustifyContentMode(JustifyContentMode.END);

        dialogLayout.add(buttonsLayout);
        dialog.add(dialogLayout);

        dialog.open();
    }

    /**
     * Confirm before deleting user
     *
     * WHY confirmation?
     * - Destructive action
     * - No undo
     * - Prevent accidents
     */
    private void confirmDelete(User user) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirm Delete");

        VerticalLayout content = new VerticalLayout();
        content.add(new Span("Are you sure you want to delete user: " + user.getEmail() + "?"));
        content.add(new Span("This action cannot be undone."));
        content.getStyle().set("color", "var(--lumo-error-color)");

        Button deleteButton = new Button("Delete", e -> {
            try {
                userService.deleteUser(user.getId());
                showNotification("User deleted successfully", NotificationVariant.LUMO_SUCCESS);
                confirmDialog.close();
                loadUsers();
            } catch (Exception ex) {
                showNotification("Error deleting user: " + ex.getMessage(),
                        NotificationVariant.LUMO_ERROR);
            }
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Cancel", e -> confirmDialog.close());

        HorizontalLayout buttons = new HorizontalLayout(deleteButton, cancelButton);
        buttons.setJustifyContentMode(JustifyContentMode.END);

        confirmDialog.add(content, buttons);
        confirmDialog.open();
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000,
                Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(variant);
        notification.open();
    }
}