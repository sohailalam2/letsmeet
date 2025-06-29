package com.yourkoder.letsmeet.iam.sdk.util.keymanagement;

import jakarta.enterprise.inject.Vetoed;
import lombok.extern.jbosslog.JBossLog;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.SsmException;

@Vetoed
@JBossLog
public class ParameterStoreService {

    private final Region region;

    private final SsmClient ssmClient;

    public ParameterStoreService(Region region) {
        this(region, SsmClient.builder()
                .region(region)
                .build());
    }

    public ParameterStoreService(Region region, SsmClient ssmClient) {
        System.setProperty("software.amazon.awssdk.http.service.impl",
                "software.amazon.awssdk.http.apache.ApacheSdkHttpService");
        System.setProperty("software.amazon.awssdk.http.async.service.impl",
                "software.amazon.awssdk.http.nio.netty.NettySdkAsyncHttpService");
        this.region = region;
        this.ssmClient = ssmClient;
    }

    public void setKey(String keyID, String keyValue, boolean overwrite) {
        PutParameterRequest putParameterRequest = PutParameterRequest.builder()
                .name(keyID)
                .type("String")
                .dataType("text")
                .overwrite(true)
                .value(keyValue)
                .overwrite(overwrite)
                .build();

        ssmClient.putParameter(putParameterRequest);
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
