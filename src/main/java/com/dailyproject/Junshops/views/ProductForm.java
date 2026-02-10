package com.dailyproject.Junshops.views;

import com.dailyproject.Junshops.model.Category;
import com.dailyproject.Junshops.model.Product;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;

import java.util.List;

public class ProductForm extends FormLayout {

    private final TextField name = new TextField("Name");
    private final TextField brand = new TextField("Brand");
    private final BigDecimalField price = new BigDecimalField("Price");
    private final IntegerField inventory = new IntegerField("Inventory");
    private final TextField description = new TextField("Description");
    private final ComboBox<Category> category = new ComboBox<>("Category");

    private final Button save = new Button("Save");
    private final Button delete = new Button("Delete");
    private final Button cancel = new Button("Cancel");

    private final Binder<Product> binder = new BeanValidationBinder<>(Product.class);

    public ProductForm(List<Category> categories) {
        addClassName("product-form");
        
        binder.bindInstanceFields(this);

        category.setItems(categories);
        category.setItemLabelGenerator(Category::getName);

        add(name, brand, price, inventory, description, category, createButtonsLayout());
    }

    private Component createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, binder.getBean())));
        cancel.addClickListener(event -> fireEvent(new CloseEvent(this)));

        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));

        return new HorizontalLayout(save, delete, cancel);
    }

    private void validateAndSave() {
        if (binder.isValid()) {
            fireEvent(new SaveEvent(this, binder.getBean()));
        }
    }

    public void setProduct(Product product) {
        binder.setBean(product);
    }

    // Events
    public static abstract class ProductFormEvent extends ComponentEvent<ProductForm> {
        private final Product product;

        protected ProductFormEvent(ProductForm source, Product product) {
            super(source, false);
            this.product = product;
        }

        public Product getProduct() {
            return product;
        }
    }

    public static class SaveEvent extends ProductFormEvent {
        SaveEvent(ProductForm source, Product product) {
            super(source, product);
        }
    }

    public static class DeleteEvent extends ProductFormEvent {
        DeleteEvent(ProductForm source, Product product) {
            super(source, product);
        }
    }

    public static class CloseEvent extends ProductFormEvent {
        CloseEvent(ProductForm source) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                    ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}
