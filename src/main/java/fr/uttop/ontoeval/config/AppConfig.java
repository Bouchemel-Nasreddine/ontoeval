package fr.uttop.ontoeval.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"fr.uttop.ontoeval", "fr.uttop.normalization"})
public class AppConfig {
    // Additional configurations if needed

    @Value("${ontologies.folder}")
    private String uploadDir;

    public String getUploadDir() {
        return uploadDir;
    }
}
