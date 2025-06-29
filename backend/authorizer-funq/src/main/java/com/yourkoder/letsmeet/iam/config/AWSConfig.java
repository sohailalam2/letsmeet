package com.yourkoder.letsmeet.iam.config;

import software.amazon.awssdk.regions.Region;

public interface AWSConfig {
    String region();

    default Region getRegion() {
        return Region.of(region());
    }
}
