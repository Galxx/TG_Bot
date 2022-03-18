package org.group_gb.tg_bot.service;

import org.group_gb.tg_bot.TelegramBot;
import org.group_gb.tg_bot.exceptions.IpgeolocationAPIException;
import org.group_gb.tg_bot.exceptions.YandexApiException;
import org.group_gb.tg_bot.ipgeolocation_api.IpgeolocationAPIService;
import org.group_gb.tg_bot.models.User;
import org.group_gb.tg_bot.yandex_api.YandexAPIService;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Map;

@Service
public class MailingService {

    @Value("${mailing.hour}")
    Integer mailingHour;

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(MailingService.class);
    private final TelegramBot telegramBot;
    private final IpgeolocationAPIService ipgeolocationAPIService;
    private final YandexAPIService yandexAPIService;
    private final Map<String,Integer> mapCommandGeoMark;

    public MailingService(TelegramBot telegramBot, IpgeolocationAPIService ipgeolocationAPIService, YandexAPIService yandexAPIService,Map<String,Integer> mapCommandGeoMark) {
        this.telegramBot = telegramBot;
        this.ipgeolocationAPIService = ipgeolocationAPIService;
        this.yandexAPIService = yandexAPIService;
        this.mapCommandGeoMark = mapCommandGeoMark;
    }

    @Async("threadPoolTaskExecutor")
    public void sendMessage(User user){

        MDC.put("chatId",user.getChatId().toString());
        log.info("Send message");

        try {
            int currentHour = ipgeolocationAPIService.getHour(user.getLatitude(),user.getLongitude());

            if (currentHour == mailingHour){
                SendMessage message = new SendMessage();
                Long chatId = user.getChatId();
                message.setChatId(chatId.toString());
                message.setText(yandexAPIService.getForcast(mapCommandGeoMark.get("Будет ли сегодня дождь?"),user.getLatitude(), user.getLongitude()));
                telegramBot.sendMessage(message);
            }
        }catch (YandexApiException | IpgeolocationAPIException e){
            log.error(e.getMessage(),e);
        }
    }

}
