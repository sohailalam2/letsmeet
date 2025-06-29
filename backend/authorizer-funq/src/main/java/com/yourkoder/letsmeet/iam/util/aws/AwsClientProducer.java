package com.yourkoder.letsmeet.iam.util.aws;

import com.yourkoder.letsmeet.iam.config.ApplicationConfig;
import com.yourkoder.letsmeet.iam.sdk.util.keymanagement.ParameterStoreService;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import lombok.extern.jbosslog.JBossLog;

@ApplicationScoped
@JBossLog
public class AwsClientProducer {

    @Inject
    ApplicationConfig config;

    @Produces
    @ApplicationScoped
    @Alternative
    @Priority(1)
    public ParameterStoreService produceParameterStoreService() {
        LOG.debugf("Creating parameter store client  in region: [%s]", config.aws().getRegion().id());
        return new ParameterStoreService(config.aws().getRegion());
    }
}
