package com.yourkoder.letsmeet.config;

import software.amazon.awssdk.regions.Region;

public interface AWSConfig {

    String region();

    S3 s3();

//    Chime chime();

    default Region getRegion() {
        return Region.of(region());
    }

    interface S3 {

        String bucket();
    }
//
//    interface Chime {
//
//        String transcriptionConfigArn();
//    }

}
