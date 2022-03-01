package org.group_gb.tg_bot.service;


import org.group_gb.tg_bot.botState.ChatState;
import org.group_gb.tg_bot.botState.ChatStateData;
import org.group_gb.tg_bot.models.User;
import org.group_gb.tg_bot.yandexAPI.YandexAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Service
public class TelegramBotService {

    @Autowired
    private UserService userService;

    private final ChatStateData chatStateData;
    private final YandexAPIService yandexAPIService;


    public TelegramBotService(ChatStateData chatStateData, YandexAPIService yandexAPIService) {
        this.chatStateData = chatStateData;
        this.yandexAPIService = yandexAPIService;
    }

    public SendMessage handleUpdate(Update update) {

        SendMessage message = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        User user = new User();
        user.setChatId(chatId);
        message.setChatId(chatId.toString());

        //Проверим текущий статус чата
        ChatState chatState = getChatState(chatId);

        if (chatState == ChatState.WAITING_COMMAND) {
            if (update.hasMessage() && update.getMessage().hasText()) {

                String messageText = update.getMessage().getText();

                //Проверим новое сообщение, установим соответствующий статус
                if (messageText.equals("/start")) {
                    chatState = ChatState.WAITING_COMMAND;
                } else if (messageText.equals("Будет ли сегодня дождь?")) {
                    chatState = ChatState.WAITING_GEOMARK;
                } else if (messageText.equals("Рекоммендации о перепадах")) {
                    chatState = ChatState.WAITING_RECOMMENDATION_GEOMARK;
                }

                chatStateData.setChatState(chatId, chatState);

                //Сформируем ответ в зависимости от состояния чата
                if (chatState == ChatState.WAITING_COMMAND) {
                    createResponseWAITING_COMMAND(message);
                } else if (chatState == ChatState.WAITING_GEOMARK || chatState == ChatState.WAITING_RECOMMENDATION_GEOMARK) {
                    createResponseWAITING_GEOMARK(message);
                }

            } else {
                createResponseWAITING_COMMAND(message);
            }
        } else if (chatState == ChatState.WAITING_GEOMARK) {
            if (update.hasMessage() && update.getMessage().hasLocation()) {
                Location location = update.getMessage().getLocation();
                createResponseForcast(message, location);

                user.setLatitude(location.getLatitude());
                user.setLongitude(location.getLongitude());
                userService.save(user);

                chatStateData.setChatState(chatId, ChatState.WAITING_COMMAND);
            }
            else {
                createResponseWAITING_GEOMARK(message);
            }

        } else if (chatState == ChatState.WAITING_RECOMMENDATION_GEOMARK) {
            if (update.hasMessage() && update.getMessage().hasLocation()) {
                Location location = update.getMessage().getLocation();
                createWeatherChangeRecommendation(message, location);

                user.setLatitude(location.getLatitude());
                user.setLongitude(location.getLongitude());
                userService.save(user);

                chatStateData.setChatState(chatId, ChatState.WAITING_COMMAND);
            }
            else {
                createResponseWAITING_GEOMARK(message);
            }

        }

        return message;

    }

    private void createResponseForcast(SendMessage message, Location location) {

        //Долгота: 139.73967 Широта: 35.660577
        //message.setText("Долгота: " + longitude.toString() + " Широта : "+latitude.toString());
        Double lat = location.getLatitude();
        Double lon = location.getLongitude();
        message.setText(yandexAPIService.getForcast(lat, lon));

        setMainMenu(message);
    }

    private void createWeatherChangeRecommendation(SendMessage message, Location location) {

        Double lat = location.getLatitude();
        Double lon = location.getLongitude();
        message.setText(yandexAPIService.getWeatherChangeRecommendation(lat, lon));

        setMainMenu(message);
    }

    private void createResponseWAITING_COMMAND(SendMessage message) {

        message.setText("Ожидаю команды");
        setMainMenu(message);

    }

    private void createResponseWAITING_GEOMARK(SendMessage message) {
        message.setText("Отправьте, пожалуйста, геометку");
    }



    private ChatState getChatState(Long chatId) {

        ChatState chatState = chatStateData.getChatState(chatId);
        if (chatState == null) {
            chatState = ChatState.WAITING_COMMAND;
            chatStateData.setChatState(chatId, chatState);
        }

        return chatState;

    }

    private void setMainMenu(SendMessage message) {

        //Установим keyboard
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        //replyKeyboardMarkup.setOneTimeKeyboard(false);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Будет ли сегодня дождь?"));
        keyboard.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("Рекоммендации о перепадах"));
        keyboard.add(row2);
        replyKeyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(replyKeyboardMarkup);

    }

}
