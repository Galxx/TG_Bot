package org.group_gb.tg_bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Configuration
public class BotConfig {

    @Bean
    public DefaultBotOptions getDefaultBotOptions(){
        DefaultBotOptions defaultBotOptions = new DefaultBotOptions();
        defaultBotOptions.setMaxThreads(10);
        return defaultBotOptions;
    }

}
