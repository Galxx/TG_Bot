package org.group_gb.tg_bot.service;

import org.group_gb.tg_bot.TelegramBot;
import org.group_gb.tg_bot.ipgeolocation_api.IpgeolocationAPIService;
import org.group_gb.tg_bot.models.User;
import org.group_gb.tg_bot.yandex_api.YandexAPIService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
public class MailingService {

    @Value("${mailing.hour}")
    Integer mailingHour;

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(MailingService.class);
    private final TelegramBot telegramBot;
    private final IpgeolocationAPIService ipgeolocationAPIService;
    private final YandexAPIService yandexAPIService;

    public MailingService(TelegramBot telegramBot, IpgeolocationAPIService ipgeolocationAPIService, YandexAPIService yandexAPIService) {
        this.telegramBot = telegramBot;
        this.ipgeolocationAPIService = ipgeolocationAPIService;
        this.yandexAPIService = yandexAPIService;
    }

    @Async("threadPoolTaskExecutor")
    public void sendMessage(User user){

        log.info(""+user.getChatId());

       int currentHour = ipgeolocationAPIService.getHour(user.getLatitude(),user.getLongitude());

       if (currentHour == mailingHour){
        SendMessage message = new SendMessage();
        Long chatId = user.getChatId();
        message.setChatId(chatId.toString());
        message.setText(yandexAPIService.getForcast(user.getLatitude(), user.getLongitude()));
        telegramBot.sendMessage(message);
       }

    }

}
