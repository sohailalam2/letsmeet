package com.yourkoder.letsmeet.processmanager.config;

import software.amazon.awssdk.regions.Region;

public interface AWSConfig {

    String region();

    S3 s3();

    Bedrock bedrock();

//    Chime chime();

    default Region getRegion() {
        return Region.of(region());
    }

    interface S3 {

        String bucket();
    }

    interface Bedrock {

        String modelId();
    }

}
