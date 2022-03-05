package org.group_gb.tg_bot.service;


import org.group_gb.tg_bot.bot_state.ChatState;
import org.group_gb.tg_bot.bot_state.ChatStateData;
import org.group_gb.tg_bot.models.ChatSettings;
import org.group_gb.tg_bot.models.User;
import org.group_gb.tg_bot.yandex_api.YandexAPIService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TelegramBotService {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TelegramBotService.class);


    private final UserService userService;

    private final YandexAPIService yandexAPIService;
    private final ChatSettingsService chatSettingsService;
    private final ChatStateData chatStateData;

    public TelegramBotService( YandexAPIService yandexAPIService, ChatSettingsService chatSettingsService, ChatStateData chatStateData, UserService userService) {
        this.yandexAPIService = yandexAPIService;
        this.chatSettingsService = chatSettingsService;
        this.chatStateData = chatStateData;
        this.userService = userService;

    }


    public SendMessage handleUpdate(Update update) {

        SendMessage message = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        User user = new User();
        user.setChatId(chatId);
        message.setChatId(chatId.toString());

        log.info(chatId.toString());

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
                }

                chatStateData.setChatState(chatId, chatState);

                //Обработаем команды, которые не требуют изменения статуса
                if (messageText.equals("Подписаться на рассылку о погоде")){
                    createResponseSchedule(message,chatId,true);
                    return message;
                }else if(messageText.equals("Отписаться на рассылки о погоде")){
                    createResponseSchedule(message,chatId,false);
                    return message;
                }

                //Сформируем ответ в зависимости от состояния чата
                if (chatState == ChatState.WAITING_COMMAND) {
                    createResponseWAITING_COMMAND(message, chatId);
                } else if (chatState == ChatState.WAITING_GEOMARK) {
                    createResponseWAITING_GEOMARK(message);
                }

            } else {
                createResponseWAITING_COMMAND(message,chatId);
            }
        } else if (chatState == ChatState.WAITING_GEOMARK) {
            if (update.hasMessage() && update.getMessage().hasLocation()) {
                Location location = update.getMessage().getLocation();
                createResponseForcast(message, location,chatId);

                user.setLatitude(location.getLatitude());
                user.setLongitude(location.getLongitude());
                userService.save(user);

                chatStateData.setChatState(chatId, ChatState.WAITING_COMMAND);
            } else {
                createResponseWAITING_GEOMARK(message);
            }

        }

        return message;

    }

    private void createResponseForcast(SendMessage message, Location location,Long chatId) {
        Double lat = location.getLatitude();
        Double lon = location.getLongitude();
        message.setText(yandexAPIService.getForcast(lat, lon));
        setMainMenu(message, chatId);
    }

    private void createResponseWAITING_COMMAND(SendMessage message,Long chatId) {

        message.setText("Ожидаю команды");
        setMainMenu(message,chatId);

    }

    private void createResponseWAITING_GEOMARK(SendMessage message) {

        message.setText("Отправьте, пожалуйста, геометку");

    }

    private void createResponseSchedule(SendMessage message, Long chatId, boolean schedule){

        ChatSettings chatSettings = new ChatSettings();
        chatSettings.setChatId(chatId);
        chatSettings.setMailing(schedule);
        chatSettingsService.update(chatSettings);

        if(schedule) {
            message.setText("Вы успешно подписаны на рассылку");
        }else{
            message.setText("Вы успешно отписаны от рассылки");
        }

        setMainMenu(message,chatId);
    }

    private ChatState getChatState(Long chatId) {

        ChatState chatState = chatStateData.getChatState(chatId);

        if (chatState == null) {
            chatState = ChatState.WAITING_COMMAND;
            chatStateData.setChatState(chatId, chatState);
        }

        return chatState;

    }

    private void setMainMenu(SendMessage message, Long chatId) {

        //Установим keyboard
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        //replyKeyboardMarkup.setOneTimeKeyboard(false);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Будет ли сегодня дождь?"));

        KeyboardRow row2 = new KeyboardRow();
        Optional<ChatSettings> chatSettings = chatSettingsService.findByChatIdAndMailingIsTrue(chatId);
        if (chatSettings.isPresent()) {
            row2.add(new KeyboardButton("Отписаться на рассылки о погоде"));
        }else{
            row2.add(new KeyboardButton("Подписаться на рассылку о погоде"));
        }

        keyboard.add(row1);
        keyboard.add(row2);
        replyKeyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(replyKeyboardMarkup);

    }



}
