package com.dailyproject.Junshops;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.page.AppShellConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@CssImport("./themes/junshops/styles.css")
public class JunShopsApplication  implements AppShellConfigurator {

	public static void main(String[] args) {
		SpringApplication.run(JunShopsApplication.class, args);
	}

}
