package se.reviewservice.mongoDB.mongoConfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

// Mongo-konfiguration
@Configuration
@EnableMongoRepositories(
        basePackages = "se.reviewservice.mongoDB.repository",
        mongoTemplateRef = "mongoTemplate"
)
public class MongoConfig {
    // konfigurera ev. MongoTemplate om du har flera
}
