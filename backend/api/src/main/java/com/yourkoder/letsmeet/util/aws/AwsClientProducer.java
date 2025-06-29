package com.yourkoder.letsmeet.util.aws;

import com.yourkoder.letsmeet.config.ApplicationConfig;
import com.yourkoder.letsmeet.iam.sdk.util.keymanagement.ParameterStoreService;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import lombok.extern.jbosslog.JBossLog;
import software.amazon.awssdk.services.chimesdkmediapipelines.ChimeSdkMediaPipelinesClient;
import software.amazon.awssdk.services.chimesdkmeetings.ChimeSdkMeetingsClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@ApplicationScoped
@JBossLog
public class AwsClientProducer {

    @Inject
    ApplicationConfig config;

    @Produces
    @ApplicationScoped
    public S3Client produceS3Client() {
        LOG.debugf("Creating S3 client in region: [%s]", config.aws().getRegion().id());
        return S3Client.builder()
                .region(config.aws().getRegion())
                .build();
    }

    @Produces
    @ApplicationScoped
    public S3Presigner produceS3Presigner() {
        LOG.debugf("Creating S3 presginer in region: [%s]", config.aws().getRegion().id());
        return S3Presigner.builder()
                .region(config.aws().getRegion())
                .build();
    }

    @Produces
    @ApplicationScoped
    @Alternative
    @Priority(1)
    public ParameterStoreService produceParameterStoreService() {
        LOG.debugf("Creating parameter store client  in region: [%s]", config.aws().getRegion().id());
        return new ParameterStoreService(config.aws().getRegion());
    }

    @Produces
    @ApplicationScoped
    public ChimeSdkMeetingsClient produceMeetingsClient() {
        LOG.debugf("Creating chime SDK meeting client  in region: [%s]", config.aws().getRegion().id());
        return ChimeSdkMeetingsClient.builder().region(config.aws().getRegion()).build();
    }

    @Produces
    @ApplicationScoped
    public ChimeSdkMediaPipelinesClient produceMediaPipelinesClient() {
        LOG.debugf("Creating chime SDK pipelines client  in region: [%s]", config.aws().getRegion().id());
        return ChimeSdkMediaPipelinesClient.builder().region(config.aws().getRegion()).build();
    }
}
