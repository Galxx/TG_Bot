package org.group_gb.tg_bot;


import org.group_gb.tg_bot.service.TelegramBotService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final TelegramBotService telegramBotService;

    @Value("${telegrambot.userName}")
    String userName;

    @Value("${telegrambot.botToken}")
    String token;

    public TelegramBot(TelegramBotService telegramBotService) {
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
        // We check if the update has a message and the message has tex
            try {
                execute(telegramBotService.handleUpdate(update)); // Call method to send the message
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

    }


}
