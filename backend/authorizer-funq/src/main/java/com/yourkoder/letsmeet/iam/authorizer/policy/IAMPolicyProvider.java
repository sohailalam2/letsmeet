package com.yourkoder.letsmeet.iam.authorizer.policy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourkoder.letsmeet.iam.authorizer.policy.dtos.response.AuthorizationPolicyResponse;
import com.yourkoder.letsmeet.iam.authorizer.policy.exception.PolicyGenerationFailedException;
import com.yourkoder.letsmeet.iam.authorizer.policy.template.AuthorizationPolicyResponseTemplate;
import com.yourkoder.letsmeet.iam.authorizer.token.exception.JwtClaimsNotApplicableException;
import com.yourkoder.letsmeet.iam.authorizer.token.valueobject.AuthenticationStatus;
import com.yourkoder.letsmeet.iam.config.ApplicationConfig;
import com.yourkoder.letsmeet.iam.config.PolicyConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

@ApplicationScoped
@JBossLog
public class IAMPolicyProvider {
    @Inject
    ObjectMapper objectMapper;

    private final PolicyConfig policyConfig;

    public IAMPolicyProvider(ApplicationConfig applicationConfig) {
        this.policyConfig = applicationConfig.authorizer().policy();
    }

    public AuthorizationPolicyResponse getAuthorizationPolicyDocument(
            AuthenticationStatus authenticationStatus,
            String methodArn
    ) throws PolicyGenerationFailedException {

        AuthorizationPolicyResponseTemplate.AuthorizationPolicyResponseTemplateBuilder builder
                = AuthorizationPolicyResponseTemplate.builder();

        try {
            switch (authenticationStatus) {
                case AUTHENTICATED:
                    LOG.debug(authenticationStatus);

                    builder.withPrincipalId(
                                    authenticationStatus.getJwtClaimsSet().getSubject()
                            ).withAllowed(true)
                            .withContext(getContextFromJwtPayloadClaims(
                                    authenticationStatus.getJwtClaimsSet().getClaims())
                            );
                    break;
                case UNAUTHENTICATED:
                default:
                    LOG.debug(authenticationStatus);

                    builder.withPrincipalId(
                            this.policyConfig.unauthorizedPrincipal()
                    ).withAllowed(false);
            }

            String resource = getResourceFromMethodArn(methodArn);

            return builder
                    .withResource(resource)
                    .build().getAuthorizationPolicyResponse();
        } catch (JwtClaimsNotApplicableException e) {
            throw new PolicyGenerationFailedException();
        }
    }

    public Map<String, String> getContextFromJwtPayloadClaims(
            Map<String, Object> claims
    ) throws PolicyGenerationFailedException {
        final Map<String, String> context = new HashMap<>(claims.size());

        for (Map.Entry<String, Object> claim: claims.entrySet()) {
            try {
                context.put(
                        claim.getKey(),
                        claim.getValue() instanceof String stringClaim
                                ? stringClaim : objectMapper.writeValueAsString(claim.getValue())
                );
            } catch (JsonProcessingException e) {
                LOG.error(e.getMessage());
                throw new PolicyGenerationFailedException();
            }

        }
        return context;
    }

    public static String getResourceFromMethodArn(String methodArn) {
        return format("%s/*/*", methodArn.split("/")[0]);
    }

}
