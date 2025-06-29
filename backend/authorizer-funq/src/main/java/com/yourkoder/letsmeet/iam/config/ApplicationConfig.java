package com.yourkoder.letsmeet.iam.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.io.Serializable;
import java.util.Locale;
import java.util.TimeZone;

@ConfigMapping(prefix = "app")
public interface ApplicationConfig extends Serializable {

    LocaleConfig locale();

    @WithDefault("Asia/Kolkata")
    String timezone();

    AWSConfig aws();

    AuthorizerConfig authorizer();

    default Locale getLocale() {
        return this.locale().locale();
    }

    default TimeZone getTimezone() {
        return TimeZone.getTimeZone(timezone());
    }

    String iamTableName();

    String applicationNamespace();
}
