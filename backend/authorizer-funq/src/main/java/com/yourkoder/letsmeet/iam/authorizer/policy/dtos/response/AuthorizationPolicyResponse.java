package com.yourkoder.letsmeet.iam.authorizer.policy.dtos.response;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@RegisterForReflection
public class AuthorizationPolicyResponse {
    private String principalId;

    private Map<String, Object> policyDocument;

    private Map<String, String> context;
}
