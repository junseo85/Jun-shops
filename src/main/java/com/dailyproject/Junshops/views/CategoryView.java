package com.dailyproject.Junshops.views;

import com.dailyproject.Junshops.model.Category;
import com.dailyproject.Junshops.service.category.ICategoryService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "categories", layout = MainLayout.class)
@PageTitle("Categories | Jun-Shops")
@AnonymousAllowed
public class CategoryView extends VerticalLayout {

    private final ICategoryService categoryService;
    private final Grid<Category> grid = new Grid<>(Category.class);

    @Autowired
    public CategoryView(ICategoryService categoryService) {
        this.categoryService = categoryService;
        addClassName("category-view");
        setSizeFull();

        configureGrid();
        add(getToolbar(), grid);
        updateList();
    }

    private void configureGrid() {
        grid.addClassName("category-grid");
        grid.setSizeFull();
        grid.setColumns("id", "name");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private HorizontalLayout getToolbar() {
        Button refreshButton = new Button("Refresh");
        refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshButton.addClickListener(e -> updateList());

        HorizontalLayout toolbar = new HorizontalLayout(refreshButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void updateList() {
        grid.setItems(categoryService.getAllCategories());
    }
}