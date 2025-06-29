package com.yourkoder.letsmeet.config;

public interface JwsConfig {

    Long accessTokenExpiry();

    Long refreshTokenExpiry();

    String privateSigningKeyPath();

    String publicSigningKeyPath();

}
