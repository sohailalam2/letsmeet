package com.yourkoder.letsmeet.iam.sdk.http.filter;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourkoder.letsmeet.iam.sdk.http.context.IAMApiGatewayAuthorizerContext;
import com.yourkoder.letsmeet.iam.sdk.http.context.IAMAwsProxyRequestContext;
import com.yourkoder.letsmeet.iam.sdk.http.context.IAMSubject;
import com.yourkoder.letsmeet.iam.sdk.http.context.exceptions.InvalidSubjectException;
import com.yourkoder.letsmeet.iam.sdk.http.context.exceptions.NoRequestContextFoundException;
import com.yourkoder.letsmeet.iam.sdk.security.IAMSecurityContext;
import jakarta.annotation.Priority;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.core.ResteasyContext;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

@Priority(Priorities.AUTHORIZATION)
@JBossLog
@Provider
public class PublicEndpointSecurityAuthorizer implements ContainerRequestFilter {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Context
    ResourceInfo resourceInfo;

    @Context
    UriInfo uriInfo;

    public void filter(ContainerRequestContext requestContext) {
        LOG.debug("RBAC Security is enabled.");
        final IAMAwsProxyRequestContext contextData;

        try {
            contextData = createContextData();

            LOG.debug("Created IAM Aws Proxy Request Context.");
        } catch (NoRequestContextFoundException e) {
            LOG.error("No context data was returned by Authorizer. Can not proceed with authorization.");
            throw new InternalServerErrorException();
        }

        LOG.debugf("Context Data: %s", contextData);

        Method resourceMethod = resourceInfo.getResourceMethod();
        Class<?> resourceClass = resourceInfo.getResourceClass();
        validateDenyAll(resourceMethod, resourceClass);
        final PermitAll permitAllAnnotation = validatePermitAll(resourceMethod, resourceClass);
        final RolesAllowed rolesAllowedAnnotation = validateRolesAllowed(resourceMethod, resourceClass);

        if (contextData == null && (rolesAllowedAnnotation == null || permitAllAnnotation != null)) {
            return;
        } else if (contextData == null) {
            LOG.error("No request context data was found and API is protected by: [%s]."
                    .formatted(rolesAllowedAnnotation.getClass().getName()));
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        IAMSecurityContext iamSecurityContext = createSecurityContext(
                contextData.getAuthorizer()
        );

        if (iamSecurityContext == null) {
            if (rolesAllowedAnnotation != null) {
                LOG.error("No security context data found. Api is protected. Can not authorize request.");
                throw new WebApplicationException(Response.Status.FORBIDDEN);
            }
            LOG.debug("Security Context not created.");
        } else {
            // If security context is created and "RolesAllowed" annotation is present, then roles are always checked"
            if (rolesAllowedAnnotation != null) {
                LOG.debug("Access is role-protected.");
            } else if (permitAllAnnotation != null) {
                LOG.info("Access is permitted for all.");
            } else {
                LOG.debugf(
                        "No Authorization annotations: [%s; %s; %s] found. Access is permitted for all.",
                        PermitAll.class.getTypeName(),
                        DenyAll.class.getTypeName(),
                        RolesAllowed.class.getTypeName()
                );
            }
            requestContext.setSecurityContext(iamSecurityContext);
            ResteasyContext.pushContext(IAMSecurityContext.class, iamSecurityContext);
            LOG.debug("Security Context created.");
        }
    }

    private static RolesAllowed validateRolesAllowed(Method resourceMethod, Class<?> resourceClass) {
        final RolesAllowed rolesAllowedAnnotation;
        if (resourceMethod.isAnnotationPresent(RolesAllowed.class)) {
            rolesAllowedAnnotation = resourceMethod.getAnnotation(RolesAllowed.class);
        } else {
            rolesAllowedAnnotation = resourceClass.getAnnotation(RolesAllowed.class);
        }
        return rolesAllowedAnnotation;
    }

    private static PermitAll validatePermitAll(Method resourceMethod, Class<?> resourceClass) {
        final PermitAll permitAllAnnotation;

        if (resourceMethod.isAnnotationPresent(PermitAll.class)) {
            permitAllAnnotation = resourceMethod.getAnnotation(PermitAll.class);
        } else {
            permitAllAnnotation = resourceClass.getAnnotation(PermitAll.class);
        }
        return permitAllAnnotation;
    }

    private static DenyAll validateDenyAll(Method resourceMethod, Class<?> resourceClass) {
        final DenyAll denyAllAnnotation;
        if (resourceMethod.isAnnotationPresent(DenyAll.class)) {
            denyAllAnnotation = resourceMethod.getAnnotation(DenyAll.class);
        } else {
            denyAllAnnotation = resourceClass.getAnnotation(DenyAll.class);
        }
        if (denyAllAnnotation != null) {
            LOG.error("Access is denied for all.");
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        return denyAllAnnotation;
    }

    private IAMSecurityContext createSecurityContext(
            IAMApiGatewayAuthorizerContext authorizerContext
    ) {
        if (authorizerContext == null) {
            LOG.errorf("Security authorizer context is null.");
            throw new InternalServerErrorException();
        }
        if (authorizerContext.getContextProperties().isEmpty()) {
            LOG.errorf("Security authorizer context has no context properties.");
            throw new InternalServerErrorException();
        }

        authorizerContext.getContextProperties()
                .forEach((key, value) -> LOG.debugf("Claim: { %s : %s }", key, value));

        final IAMSubject subject;
        try {
            subject = new IAMSubject(authorizerContext);
        } catch (InvalidSubjectException e) {
            LOG.error(e.getMessage(), e);
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        if (subject.getValue().equals("Anonymous")) {
            return null;
        }

        return new IAMSecurityContext(
                uriInfo,
                subject.getValue(),
                authorizerContext.getContextProperties()
        );
    }

    protected IAMAwsProxyRequestContext createContextData() throws NoRequestContextFoundException {

        Map<Class<?>, Object> resteasyContextDataMap = ResteasyContext.getContextDataMap();

        Optional<APIGatewayV2HTTPEvent> optionalContextObject
                = Optional.ofNullable(
                (APIGatewayV2HTTPEvent) resteasyContextDataMap.get(APIGatewayV2HTTPEvent.class)
        );

        if (optionalContextObject.isEmpty()) {

            // if the authorizer was not enabled or if it was badly configured to not return any context at all then
            // we might not want to allow the request to proceed due to security concerns.
            // we need to ensure that the authorizer returns valid claims to the rest endpoint
            throw new NoRequestContextFoundException();
        }

        // TODO instead of serde cast the context object directly to AwsProxyRequestContext
        APIGatewayV2HTTPEvent contextObject = optionalContextObject.get();

        if (contextObject.getRequestContext().getAuthorizer() == null) {
            return null;
        }
        IAMApiGatewayAuthorizerContext authorizer = IAMApiGatewayAuthorizerContext.builder().build();
        Map<String, Object> lambda = contextObject.getRequestContext().getAuthorizer().getLambda();
        for (Map.Entry<String, Object> entry: lambda.entrySet()) {
            String key = entry.getKey();
            try {
                String value;
                Object valueObject = entry.getValue();
                if (valueObject instanceof String stringValue) {
                    LOG.debugf("Received string valued claim: { %s: %s }", key, stringValue);
                    value = stringValue;
                } else {
                    LOG.debugf("Received non-string valued claim: { %s: %s } of type: [%s]",
                            key,
                            valueObject,
                            valueObject.getClass().getTypeName()
                    );
                    value = OBJECT_MAPPER.writeValueAsString(valueObject);
                }
                authorizer.setContextValue(key, value);
            } catch (JsonProcessingException e) {
                throw new NoRequestContextFoundException(
                        "Invalid request context found. could not load context data.",
                        e
                );

            }

        }

        LOG.debug("Creating IAM Aws Proxy Request Context.");
        authorizer.setPrincipalId(String.valueOf(lambda.get("sub")));
        return IAMAwsProxyRequestContext.builder()
                .accountId(contextObject.getRequestContext().getAccountId())
                .apiId(contextObject.getRequestContext().getApiId())
                .httpMethod(contextObject.getRequestContext().getHttp().getMethod())
                .path(contextObject.getRequestContext().getHttp().getPath())
                .protocol(contextObject.getRequestContext().getHttp().getProtocol())
                .requestId(contextObject.getRequestContext().getRequestId())
                .resourcePath(contextObject.getRequestContext().getHttp().getPath())
                .stage(contextObject.getRequestContext().getStage())
                .requestTime(contextObject.getRequestContext().getTime())
                .requestTimeEpoch(contextObject.getRequestContext().getTimeEpoch())
                .authorizer(authorizer)
                .build();
    }
}