package com.dailyproject.Junshops.views;

import com.dailyproject.Junshops.model.Category;
import com.dailyproject.Junshops.model.Product;
import com.dailyproject.Junshops.service.category.ICategoryService;
import com.dailyproject.Junshops.service.product.IProductService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "", layout = MainLayout.class)
@PermitAll
public class ProductsView extends VerticalLayout {

    private final IProductService productService;
    private final ICategoryService categoryService;
    
    private final Grid<Product> grid = new Grid<>(Product.class, false);
    private final TextField filterText = new TextField();
    private ProductForm form;

    public ProductsView(IProductService productService, ICategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;

        addClassName("products-view");
        setSizeFull();

        configureGrid();
        configureForm();

        add(getToolbar(), getContent());
        updateList();
        closeEditor();
    }

    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }

    private void configureForm() {
        List<Category> categories = categoryService.getAllCategories();
        form = new ProductForm(categories);
        form.setWidth("25em");
        form.addListener(ProductForm.SaveEvent.class, this::saveProduct);
        form.addListener(ProductForm.DeleteEvent.class, this::deleteProduct);
        form.addListener(ProductForm.CloseEvent.class, e -> closeEditor());
    }

    private void saveProduct(ProductForm.SaveEvent event) {
        try {
            Product product = event.getProduct();
            if (product.getId() == null) {
                // This is a new product
                com.dailyproject.Junshops.request.AddProductRequest request = 
                    new com.dailyproject.Junshops.request.AddProductRequest();
                request.setName(product.getName());
                request.setBrand(product.getBrand());
                request.setPrice(product.getPrice());
                request.setInventory(product.getInventory());
                request.setDescription(product.getDescription());
                request.setCategory(product.getCategory());
                productService.addProduct(request);
            } else {
                // Update existing product
                com.dailyproject.Junshops.request.ProductUpdateRequest request = 
                    new com.dailyproject.Junshops.request.ProductUpdateRequest();
                request.setName(product.getName());
                request.setBrand(product.getBrand());
                request.setPrice(product.getPrice());
                request.setInventory(product.getInventory());
                request.setDescription(product.getDescription());
                request.setCategory(product.getCategory());
                productService.updateProduct(request, product.getId());
            }
            updateList();
            closeEditor();
            Notification.show("Product saved successfully")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Error saving product: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void deleteProduct(ProductForm.DeleteEvent event) {
        try {
            productService.deleteProductById(event.getProduct().getId());
            updateList();
            closeEditor();
            Notification.show("Product deleted successfully")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Error deleting product: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void configureGrid() {
        grid.addClassNames("product-grid");
        grid.setSizeFull();
        grid.addColumn(Product::getId).setHeader("ID").setSortable(true);
        grid.addColumn(Product::getName).setHeader("Name").setSortable(true);
        grid.addColumn(Product::getBrand).setHeader("Brand").setSortable(true);
        grid.addColumn(Product::getPrice).setHeader("Price").setSortable(true);
        grid.addColumn(Product::getInventory).setHeader("Stock").setSortable(true);
        grid.addColumn(product -> product.getCategory() != null ? product.getCategory().getName() : "")
                .setHeader("Category").setSortable(true);
        
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.asSingleSelect().addValueChangeListener(event -> editProduct(event.getValue()));
    }

    private Component getToolbar() {
        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        Button addProductButton = new Button("Add Product");
        addProductButton.addClickListener(click -> addProduct());

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addProductButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void updateList() {
        String filter = filterText.getValue();
        if (filter == null || filter.isEmpty()) {
            grid.setItems(productService.getAllProducts());
        } else {
            grid.setItems(productService.getProductsByName(filter));
        }
    }

    private void closeEditor() {
        form.setProduct(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void addProduct() {
        grid.asSingleSelect().clear();
        editProduct(new Product());
    }

    private void editProduct(Product product) {
        if (product == null) {
            closeEditor();
        } else {
            form.setProduct(product);
            form.setVisible(true);
            addClassName("editing");
        }
    }
}
