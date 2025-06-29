package com.yourkoder.letsmeet.iam.sdk.http.context;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
@Builder
public final class IAMAwsProxyRequestContext {
    private String resourceId;
    private String apiId;
    private String resourcePath;
    private String httpMethod;
    private String requestId;
    private String extendedRequestId;
    private String accountId;
    private IAMApiGatewayAuthorizerContext authorizer;
    private String stage;
    private String path;
    private String protocol;
    private String requestTime;
    private long requestTimeEpoch;
}
