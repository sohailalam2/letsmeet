package com.yourkoder.letsmeet.iam.sdk.util.aws;

import com.yourkoder.letsmeet.iam.sdk.util.keymanagement.ParameterStoreService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.ws.rs.Produces;
import lombok.extern.jbosslog.JBossLog;
import software.amazon.awssdk.regions.Region;

@ApplicationScoped
@JBossLog
public class DefaultAwsClientProducer {

    @Produces
    @ApplicationScoped
    @Default
    public ParameterStoreService produceParameterStoreService() {
        return new ParameterStoreService(Region.AP_SOUTH_1);
    }
}
