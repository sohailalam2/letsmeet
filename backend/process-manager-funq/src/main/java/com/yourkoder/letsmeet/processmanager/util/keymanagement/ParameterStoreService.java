package com.yourkoder.letsmeet.processmanager.util.keymanagement;

import com.yourkoder.letsmeet.processmanager.config.ApplicationConfig;
import com.yourkoder.letsmeet.processmanager.util.keymanagement.exception.ParameterNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.jbosslog.JBossLog;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.SsmException;

@ApplicationScoped
@JBossLog
public class ParameterStoreService {

    private final SsmClient ssmClient;

    public ParameterStoreService(ApplicationConfig config) {
        System.setProperty("software.amazon.awssdk.http.service.impl",
                "software.amazon.awssdk.http.apache.ApacheSdkHttpService");
        System.setProperty("software.amazon.awssdk.http.async.service.impl",
                "software.amazon.awssdk.http.nio.netty.NettySdkAsyncHttpService");
        this.ssmClient = SsmClient.builder()
                .region(config.aws().getRegion())
                .build();
    }

    public String getKey(String keyID) throws ParameterNotFoundException {
        try {
            GetParameterRequest parameterRequest = GetParameterRequest.builder()
                    .name(keyID)
                    .withDecryption(true)
                    .build();

            GetParameterResponse parameterResponse = ssmClient.getParameter(parameterRequest);
            return parameterResponse.parameter().value();

        } catch (SsmException e) {
            LOG.error(e.getMessage(), e);
            throw new ParameterNotFoundException(e.getMessage());
        }
    }
}
