package org.group_gb.tg_bot;


import lombok.extern.slf4j.Slf4j;
import org.group_gb.tg_bot.service.TelegramBotService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final TelegramBotService telegramBotService;

    @Value("${telegrambot.userName}")
    String userName;

    @Value("${telegrambot.botToken}")
    String token;
    private final ExecutorService exe;

    public TelegramBot(TelegramBotService telegramBotService,DefaultBotOptions  botOptions) {
        super(botOptions);
        this.telegramBotService = telegramBotService;
        this.exe =  Executors.newFixedThreadPool(10);
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

        exe.submit(() -> {
            try {
                MDC.put("updateId", update.getUpdateId().toString());
                MDC.put("chatId", update.getMessage().getChatId().toString());
                log.info("update");
                SendMessage message = telegramBotService.handleUpdate(update);
                log.info("Response:" + message.getText());
                execute(message); // Call method to send the message
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        });

    }

    public void sendMessage(SendMessage message) {

        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

}
