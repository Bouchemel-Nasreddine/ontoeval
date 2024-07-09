package fr.uttop.ontoeval;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import fr.uttop.ontoeval.config.AppConfig;

@Import(AppConfig.class)
@SpringBootApplication
public class OntoEvalApp {

    public static void main(String[] args) {
        SpringApplication.run(OntoEvalApp.class, args);
    }


}
