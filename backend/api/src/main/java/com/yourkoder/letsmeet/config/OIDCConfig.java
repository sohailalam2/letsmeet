package com.yourkoder.letsmeet.config;

public interface OIDCConfig {

    String token();

    String authorization();

    String userInfo();

    String clientId();

    String clientSecret();

    String grantType();

    String scope();

    String redirectUri();
}
