package com.yourkoder.letsmeet.processmanager.util.database;

import com.yourkoder.letsmeet.processmanager.config.ApplicationConfig;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@ApplicationScoped
public class DynamoDbClientProducer {

    @Inject
    ApplicationConfig config;

    @Produces
    @ApplicationScoped
    @Alternative
    @Priority(1)
    public DynamoDbEnhancedClient produce() {
        DynamoDbClient ddb = DynamoDbClient.builder().region(config.aws().getRegion()).build();
        return DynamoDbEnhancedClient.builder().dynamoDbClient(ddb).build();
    }
}
