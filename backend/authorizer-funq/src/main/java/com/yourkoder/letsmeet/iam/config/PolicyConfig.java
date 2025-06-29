package com.yourkoder.letsmeet.iam.config;

import io.smallrye.config.WithDefault;

public interface PolicyConfig {

    String NO_USAGE_PLAN_IDENTIFIER_KEY = "none";

    String unauthorizedPrincipal();

    /**
     * If the API uses a usage plan (the apiKeySource is set to AUTHORIZER),
     * the Lambda authorizer function must return one of the usage plan's API keys
     * as the usageIdentifierKey property value.
     */
    @WithDefault(NO_USAGE_PLAN_IDENTIFIER_KEY)
    String usageIdentifierKey();

}
