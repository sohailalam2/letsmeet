package com.yourkoder.letsmeet.iam.authorizer.policy.template;

import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import com.yourkoder.letsmeet.iam.authorizer.policy.dtos.response.AuthorizationPolicyResponse;
import lombok.Builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Builder(setterPrefix = "with")
public class AuthorizationPolicyResponseTemplate {

    public static final String EXECUTE_API_INVOKE = "execute-api:Invoke";

    public static final String VERSION_2012_10_17 = "2012-10-17";

    public static final String ALLOW = "Allow";

    public static final String DENY = "Deny";

    private String principalId;

    private String resource;

    private boolean allowed;

    private Map<String, String> context;

    public AuthorizationPolicyResponse getAuthorizationPolicyResponse() {
        return AuthorizationPolicyResponse.builder()
                .principalId(this.principalId)
                .policyDocument(this.getAuthorizationPolicyDocument())
                .context(context != null ? context : new HashMap<>())
                .build();
    }

    private Map<String, Object> getAuthorizationPolicyDocument() {
        IamPolicyResponse.Statement authorizationStatement = this.createAuthorizationStatement();

        Map<String, Object> serializablePolicy = new HashMap<>();
        serializablePolicy.put("Version", VERSION_2012_10_17);

        Map<String, Object> serializableStatement = new HashMap<>();
        serializableStatement.put("Effect", authorizationStatement.getEffect());
        serializableStatement.put("Action", authorizationStatement.getAction());
        serializableStatement.put("Resource", authorizationStatement.getResource().toArray(new String[0]));

        if (authorizationStatement.getCondition() != null) {
            serializableStatement.put("Condition", authorizationStatement.getCondition());
        }

        Map<String, Object>[] serializableStatementArray = new Map[1];
        serializableStatementArray[0] = serializableStatement;
        serializablePolicy.put("Statement", serializableStatementArray);
        return serializablePolicy;
    }

    private IamPolicyResponse.Statement createAuthorizationStatement() {
        if (this.allowed) {
            return allowStatement(this.resource);
        } else {
            return denyStatement(this.resource);
        }
    }

    private static IamPolicyResponse.Statement allowStatement(String resource) {
        return IamPolicyResponse.Statement.builder()
                .withEffect(ALLOW)
                .withResource(Collections.singletonList(resource))
                .withAction(EXECUTE_API_INVOKE)
                .build();
    }

    private static IamPolicyResponse.Statement denyStatement(String resource) {
        return IamPolicyResponse.Statement.builder()
                .withEffect(DENY)
                .withResource(Collections.singletonList(resource))
                .withAction(EXECUTE_API_INVOKE)
                .build();
    }
}
