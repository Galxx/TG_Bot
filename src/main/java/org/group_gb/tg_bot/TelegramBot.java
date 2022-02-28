package org.group_gb.tg_bot;


import lombok.extern.slf4j.Slf4j;
import org.group_gb.tg_bot.service.TelegramBotService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final TelegramBotService telegramBotService;

    @Value("${telegrambot.userName}")
    String userName;

    @Value("${telegrambot.botToken}")
    String token;

    public TelegramBot(TelegramBotService telegramBotService,DefaultBotOptions  botOptions) {
        super(botOptions);
        this.telegramBotService = telegramBotService;
    }

    @Override
    public String getBotUsername() {
        return userName;
    }

    @Override
    public String getBotToken() {
        return token;
    }


    @Override
    public void onUpdateReceived(Update update) {

//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    execute(telegramBotService.handleUpdate(update)); // Call method to send the message
//                } catch (TelegramApiException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        thread.start();

        try {
            execute(telegramBotService.handleUpdate(update)); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }




}
