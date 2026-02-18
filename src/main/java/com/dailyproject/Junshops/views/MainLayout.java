package com.dailyproject.Junshops.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout {



    public MainLayout() {


        createHeader();
        createDrawer();
    }

    /**
     * Creates styled header with logo and navigation toggle
     */
    private void createHeader() {
        H1 logo = new H1("Jun-Shops");
        logo.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.MEDIUM
        );
        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames(
                LumoUtility.Padding.Vertical.NONE,
                LumoUtility.Padding.Horizontal.LARGE
        );
        addToNavbar(header);




    }

    /**
     * Adds styled navigation links to application drawer
     */
    private void createDrawer() {
        VerticalLayout menu = new VerticalLayout();

        // Public routes
        menu.add(new RouterLink("Home", HomeView.class));
        menu.add(new RouterLink("Products", ProductListView.class));
        menu.add(new RouterLink("Categories", CategoryView.class));



        addToDrawer(menu);
    }
}