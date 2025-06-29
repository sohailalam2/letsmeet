package com.yourkoder.letsmeet.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.Locale;
import java.util.TimeZone;

@ConfigMapping(prefix = "app")
public interface ApplicationConfig {

    LocaleConfig locale();

    @WithDefault("Asia/Kolkata")
    String timezone();

    OIDCConfig oidc();

    JwsConfig jws();

    AWSConfig aws();

    default Locale getLocale() {
        return this.locale().locale();
    }

    default TimeZone getTimezone() {
        return TimeZone.getTimeZone(timezone());
    }

    @WithDefault("letsmeet")
    String applicationNamespace();

    String meetingTableName();

    String iamTableName();

    Trello trello();

    Features features();

    interface Trello {

        String apiKey();
    }

    interface Features {

        @WithDefault("false")
        boolean switchHostsOnLeave();
    }
}
