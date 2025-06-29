package com.yourkoder.letsmeet.config;

import io.smallrye.config.WithDefault;

import java.util.Locale;

public interface LocaleConfig {

    @WithDefault("IN")
    String country();

    @WithDefault("en")
    String language();

    default Locale locale() {
        return Locale.of(language(), country());
    }
}
