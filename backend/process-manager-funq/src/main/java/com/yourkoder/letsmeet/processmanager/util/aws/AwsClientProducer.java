package com.yourkoder.letsmeet.processmanager.util.aws;

import com.yourkoder.letsmeet.processmanager.config.ApplicationConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import lombok.extern.jbosslog.JBossLog;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.chimesdkmediapipelines.ChimeSdkMediaPipelinesClient;
import software.amazon.awssdk.services.chimesdkmeetings.ChimeSdkMeetingsClient;
import software.amazon.awssdk.services.s3.S3Client;

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

    @ApplicationScoped
    public BedrockRuntimeClient bedrockRuntimeClient() {
        LOG.debugf("Creating bedrock runtime SDK meeting client  in region: [%s]", config.aws().getRegion().id());
        return BedrockRuntimeClient.builder()
                .region(config.aws().getRegion())
                .build();
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
