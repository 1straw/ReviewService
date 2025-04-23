package se.reviewservice.h2db.h2config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// JPA-konfiguration
@Configuration
@EnableJpaRepositories(
        basePackages = "se.reviewservice.h2db.repository",
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
public class H2Config {
    // konfigurera ev. JPA här
}
