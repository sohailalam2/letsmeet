package com.yourkoder.letsmeet.processmanager.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.Locale;
import java.util.TimeZone;

@ConfigMapping(prefix = "app")
public interface ApplicationConfig {

    LocaleConfig locale();

    @WithDefault("Asia/Kolkata")
    String timezone();

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

    Slack slack();

    String iamTableName();

    Trello trello();

    interface Slack {

        String channelId();

        String botToken();
    }

    interface Trello {

        String apiKey();

        String defaultListId();

        String defaultBoardId();

        String defaultToken();
    }
}
