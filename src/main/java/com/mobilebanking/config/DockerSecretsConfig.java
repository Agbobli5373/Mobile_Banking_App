package com.mobilebanking.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Environment post processor for handling Docker secrets in the Docker Compose
 * environment.
 * This class reads sensitive configuration values from Docker secret files and
 * makes them
 * available as environment properties.
 */
public class DockerSecretsConfig implements EnvironmentPostProcessor {

    // Logger for this class
   private static final Logger logger = LoggerFactory.getLogger(DockerSecretsConfig.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // Only process if docker profile is active
        if (!environment.acceptsProfiles("docker")) {
            return;
        }

        Map<String, Object> secretProperties = new HashMap<>();

        // Read database password from Docker secret file
        String dbPasswordFile = environment.getProperty("DB_PASSWORD_FILE");
        if (dbPasswordFile != null) {
            String dbPassword = readSecretFile(dbPasswordFile);
            if (dbPassword != null) {
                secretProperties.put("spring.datasource.password", dbPassword);
            }
        }

        // Read JWT secret from Docker secret file
        String jwtSecretFile = environment.getProperty("JWT_SECRET_FILE");
        if (jwtSecretFile != null) {
            String jwtSecret = readSecretFile(jwtSecretFile);
            if (jwtSecret != null) {
                secretProperties.put("jwt.secret", jwtSecret);
            }
        }

        // Add the secret properties to the environment
        if (!secretProperties.isEmpty()) {
            PropertySource<?> propertySource = new MapPropertySource("dockerSecrets", secretProperties);
            environment.getPropertySources().addFirst(propertySource);
        }
    }

    /**
     * Reads content from a Docker secret file.
     * 
     * @param filePath the path to the secret file
     * @return the content of the file, or null if the file cannot be read
     */
    private String readSecretFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                return Files.readString(path).trim();
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return null;
    }
}