package org.group_gb.tg_bot.service;


import org.group_gb.tg_bot.bot_state.ChatState;
import org.group_gb.tg_bot.bot_state.ChatStateData;
import org.group_gb.tg_bot.exceptions.YandexApiException;
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

import java.util.*;

@Service
public class TelegramBotService {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TelegramBotService.class);

    private final UserService userService;
    private final YandexAPIService yandexAPIService;
    private final ChatSettingsService chatSettingsService;
    private final ChatStateData chatStateData;
    private final Map<String,Integer> mapCommandGeoMark;

    public TelegramBotService(YandexAPIService yandexAPIService, ChatSettingsService chatSettingsService, ChatStateData chatStateData, UserService userService,Map<String,Integer> mapCommandGeoMark) {
        this.yandexAPIService = yandexAPIService;
        this.chatSettingsService = chatSettingsService;
        this.chatStateData = chatStateData;
        this.userService = userService;
        this.mapCommandGeoMark = mapCommandGeoMark;
    }

    public SendMessage handleUpdate(Update update) {

        SendMessage message = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        message.setChatId(chatId.toString());
        //Проверим текущий статус чата
        ChatState chatState = getChatState(chatId);

        if(chatState == ChatState.WAITING_COMMAND){
            if (update.hasMessage() && update.getMessage().hasText()) {

                String messageText = update.getMessage().getText();
                log.info(messageText);

                if(messageText.equals("/start")){
                    createResponseWAITING_COMMAND(message,chatId);
                    return message;
                }else if(messageText.equals("Подписаться на рассылку")){
                    createResponseSchedule(message,chatId,true);
                    return message;
                }else if(messageText.equals("Отписаться на рассылки")) {
                    createResponseSchedule(message, chatId, false);
                    return message;
                }else if(messageText.equals("Удалить геометку")){
                    userService.deleteByChatId(chatId);
                    log.info("Delete geomark in base " + chatId);
                    createResponseDeleteGeomark(message, chatId);
                    return message;
                }else if(mapCommandGeoMark.get(messageText) != null){
                    Optional<User> optionalUser = userService.findByChatId(chatId);

                    if(optionalUser.isPresent()){

                        Double lon = optionalUser.get().getLongitude();
                        Double lat = optionalUser.get().getLatitude();
                        createResponseForcast(message,  mapCommandGeoMark.get(messageText), lat, lon, chatId);

                        return message;

                    }else{
                        switch (messageText){

                            case "Будет ли сегодня дождь?":
                                chatState = ChatState.WAITING_GEOMARK;
                                break;
                            case "Рекоммендации о перепадах":
                                chatState = ChatState.WAITING_RECOMMENDATION_GEOMARK;
                                break;
                            case "Погода сейчас":
                                chatState = ChatState.WAITING_GEOMARK_NOW;
                                break;
                            case "Погода сегодня":
                                chatState = ChatState.WAITING_GEOMARK_TODAY;
                                break;
                            case "Погода на 2 дня":
                                chatState = ChatState.WAITING_GEOMARK_2DAYS;
                                break;
                            case "Погода на неделю":
                                chatState = ChatState.WAITING_GEOMARK_WEEK;
                                break;
                        }

                        chatStateData.setChatState(chatId, chatState);
                        log.info("set chatState:" +  chatState);

                        createResponseWAITING_GEOMARK(message);

                        return message;

                    }

                }else{
                    createResponseWAITING_COMMAND(message,chatId);
                    return message;
                }
        }} else if (chatState == ChatState.WAITING_GEOMARK) {
            if (update.hasMessage() && update.getMessage().hasLocation()) {
                Location location = update.getMessage().getLocation();
                handleUpdateGeomark(message,mapCommandGeoMark.get("Будет ли сегодня дождь?"), location,chatId);
            }
            else {
                createResponseWAITING_GEOMARK(message);
            }

        } else if (chatState == ChatState.WAITING_RECOMMENDATION_GEOMARK) {
            if (update.hasMessage() && update.getMessage().hasLocation()) {
                Location location = update.getMessage().getLocation();
                handleUpdateGeomark(message,mapCommandGeoMark.get("Рекоммендации о перепадах"), location,chatId);
            } else {
                createResponseWAITING_GEOMARK(message);
            }
        }else if (chatState == ChatState.WAITING_GEOMARK_NOW){ //Погода сейчас
            if (update.hasMessage() && update.getMessage().hasLocation()) {
                Location location = update.getMessage().getLocation();
                Integer i = mapCommandGeoMark.get("Погода сейчас");
                handleUpdateGeomark(message,i, location,chatId);
            } else {
                createResponseWAITING_GEOMARK(message);
            }
        }else if (chatState == ChatState.WAITING_GEOMARK_TODAY){ //Погода сегодня
            if (update.hasMessage() && update.getMessage().hasLocation()) {
                Location location = update.getMessage().getLocation();
                handleUpdateGeomark(message,mapCommandGeoMark.get("Погода сегодня"), location,chatId);
            } else {
                createResponseWAITING_GEOMARK(message);
            }
        }else if (chatState == ChatState.WAITING_GEOMARK_2DAYS){ //Погода на 2 дня
            if (update.hasMessage() && update.getMessage().hasLocation()) {
                Location location = update.getMessage().getLocation();
                handleUpdateGeomark(message,mapCommandGeoMark.get("Погода на 2 дня"), location,chatId);
            } else {
                createResponseWAITING_GEOMARK(message);
            }
        }else if (chatState == ChatState.WAITING_GEOMARK_WEEK){ //Погода на неделю.
            if (update.hasMessage() && update.getMessage().hasLocation()) {
                Location location = update.getMessage().getLocation();
                handleUpdateGeomark(message,mapCommandGeoMark.get("Погода на неделю"), location,chatId);
            } else {
                createResponseWAITING_GEOMARK(message);
            }
        }

        return message;

    }

    private  void handleUpdateGeomark(SendMessage message, Integer numberCommand, Location location,Long chatId){

        Double lat = location.getLatitude();
        Double lon = location.getLongitude();

        User user = new User();
        user.setChatId(chatId);
        user.setLatitude(lat);
        user.setLongitude(lon);
        userService.saveOrUpdate(user);
        log.info("Save user in base" + user);

        createResponseForcast(message,  numberCommand, lat, lon, chatId);

        chatStateData.setChatState(chatId, ChatState.WAITING_COMMAND);
        log.info("set chatState:" +  ChatState.WAITING_COMMAND);
    }

    private void createResponseForcast(SendMessage message, Integer numberCommand, Double lat, Double lon,Long chatId) {
        try {
            message.setText(yandexAPIService.getForcast(numberCommand, lat, lon));
        } catch (YandexApiException e) {
            message.setText("Ошибка при получении прогноза от Яндекса");
            log.error(e.getMessage(), e);
        }
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
        log.info("Update in base:" + chatSettings);
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
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Прогноз по часам"));
        row1.add(new KeyboardButton("Рекоммендации о перепадах"));
        keyboard.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("Погода сейчас"));
        row2.add(new KeyboardButton("Погода сегодня"));

        keyboard.add(row2);

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("Погода на 2 дня"));
        row3.add(new KeyboardButton("Погода на неделю"));
        keyboard.add(row3);

        KeyboardRow row5 = new KeyboardRow();
        if (chatSettingsService.findByChatIdAndMailingIsTrue(chatId).isPresent()) {
            row5.add(new KeyboardButton("Отписаться на рассылки"));
        }else{
            row5.add(new KeyboardButton("Подписаться на рассылку"));
        }

        if (userService.findByChatId(chatId).isPresent()){
            row5.add(new KeyboardButton("Удалить геометку"));
        }

        keyboard.add(row5);

        replyKeyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(replyKeyboardMarkup);

    }

    private void createResponseDeleteGeomark(SendMessage message, Long chatId) {
        message.setText("Геометка удалена");
        setMainMenu(message,chatId);
    }
}
