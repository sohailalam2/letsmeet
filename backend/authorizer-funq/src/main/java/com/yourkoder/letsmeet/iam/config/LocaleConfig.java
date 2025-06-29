package com.yourkoder.letsmeet.iam.config;

import io.smallrye.config.WithDefault;
import java.util.Locale;

public interface LocaleConfig {

    @WithDefault("IN")
    String country();

    @WithDefault("en")
    String language();

    default Locale locale() {
        return new Locale(language(), country());
    }
}
