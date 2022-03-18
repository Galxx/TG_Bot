package org.group_gb.tg_bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CommandConfig {


    @Bean
    public Map<String, Integer> getMapCommandGeoMark() {

        Map<String,Integer> mapCommandGeoMark = new HashMap<>();
        mapCommandGeoMark.put("Погода сейчас",-3);
        mapCommandGeoMark.put("Рекоммендации о перепадах",-2);
        mapCommandGeoMark.put("Будет ли сегодня дождь?",-1);
        mapCommandGeoMark.put("Погода сегодня",1);
        mapCommandGeoMark.put("Погода на 2 дня",2);
        mapCommandGeoMark.put("Погода на неделю",7);

        return mapCommandGeoMark;
    }

}
