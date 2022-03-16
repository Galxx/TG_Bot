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

    public TelegramBotService(YandexAPIService yandexAPIService, ChatSettingsService chatSettingsService, ChatStateData chatStateData, UserService userService) {
        this.yandexAPIService = yandexAPIService;
        this.chatSettingsService = chatSettingsService;
        this.chatStateData = chatStateData;
        this.userService = userService;
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

                //Проверим новое сообщение, установим соответствующий статус
                switch (update.getMessage().getText()){
                    case "/start":
                        chatState = ChatState.WAITING_COMMAND;
                        break;
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

                //Обработаем команды, которые не требуют изменения статуса
                if (messageText.equals("Подписаться на рассылку")){
                    createResponseSchedule(message,chatId,true);
                    return message;
                }else if(messageText.equals("Отписаться на рассылки")){
                    createResponseSchedule(message,chatId,false);
                    return message;
                }

                //Сформируем ответ в зависимости от состояния чата
                switch (getChatState(chatId)){
                    case WAITING_COMMAND:
                        createResponseWAITING_COMMAND(message,chatId);
                        break;
                    case WAITING_GEOMARK:
                        createResponseWAITING_GEOMARK(message);
                        break;
                    case  WAITING_RECOMMENDATION_GEOMARK:
                        createResponseWAITING_GEOMARK(message);
                        break;
                    case WAITING_GEOMARK_NOW:
                        createResponseWAITING_GEOMARK(message);
                        break;
                    case WAITING_GEOMARK_TODAY:
                        createResponseWAITING_GEOMARK(message);
                        break;
                    case WAITING_GEOMARK_2DAYS:
                        createResponseWAITING_GEOMARK(message);
                        break;
                    case WAITING_GEOMARK_WEEK:
                        createResponseWAITING_GEOMARK(message);
                        break;
                    default:  createResponseWAITING_COMMAND(message,chatId);}


        }} else if (chatState == ChatState.WAITING_GEOMARK) {
            if (update.hasMessage() && update.getMessage().hasLocation()) {
                Location location = update.getMessage().getLocation();
                createResponseForcast(message, -1,location,chatId);

                User user = new User();
                user.setChatId(chatId);
                user.setLatitude(location.getLatitude());
                user.setLongitude(location.getLongitude());
                log.info("Save user in base" + user);
                userService.saveOrUpdate(user);

                chatStateData.setChatState(chatId, ChatState.WAITING_COMMAND);
                log.info("set chatState:" +  ChatState.WAITING_COMMAND);
            }
            else {
                createResponseWAITING_GEOMARK(message);
            }

        } else if (chatState == ChatState.WAITING_RECOMMENDATION_GEOMARK) {
            if (update.hasMessage() && update.getMessage().hasLocation()) {
                Location location = update.getMessage().getLocation();
                createWeatherChangeRecommendation(message, location,chatId);

                User user = new User();
                user.setChatId(chatId);
                user.setLatitude(location.getLatitude());
                user.setLongitude(location.getLongitude());
                log.info("Save user in base" + user);
                userService.saveOrUpdate(user);

                chatStateData.setChatState(chatId, ChatState.WAITING_COMMAND);
                log.info("set chatState:" +  ChatState.WAITING_COMMAND);
            } else {
                createResponseWAITING_GEOMARK(message);
            }
        }else if (chatState == ChatState.WAITING_GEOMARK_NOW){ //Погода сейчас
            if (update.hasMessage() && update.getMessage().hasLocation()) {
                Location location = update.getMessage().getLocation();

                createResponseForcast(message, null, location, chatId);

                User user = new User();
                user.setChatId(chatId);
                user.setLatitude(location.getLatitude());
                user.setLongitude(location.getLongitude());
                log.info("Save user in base" + user);
                userService.saveOrUpdate(user);

                chatStateData.setChatState(chatId, ChatState.WAITING_COMMAND);
                log.info("set chatState:" +  ChatState.WAITING_COMMAND);
            } else {
                createResponseWAITING_GEOMARK(message);
            }
        }else if (chatState == ChatState.WAITING_GEOMARK_TODAY){ //Погода сегодня
            if (update.hasMessage() && update.getMessage().hasLocation()) {
                Location location = update.getMessage().getLocation();
                createResponseForcast(message, 1, location, chatId);

                User user = new User();
                user.setChatId(chatId);
                user.setLatitude(location.getLatitude());
                user.setLongitude(location.getLongitude());
                log.info("Save user in base" + user);
                userService.saveOrUpdate(user);

                chatStateData.setChatState(chatId, ChatState.WAITING_COMMAND);
                log.info("set chatState:" +  ChatState.WAITING_COMMAND);

            } else {
                createResponseWAITING_GEOMARK(message);
            }
        }else if (chatState == ChatState.WAITING_GEOMARK_2DAYS){ //Погода на 2 дня
            if (update.hasMessage() && update.getMessage().hasLocation()) {
                Location location = update.getMessage().getLocation();
                createResponseForcast(message, 2, location, chatId);

                User user = new User();
                user.setChatId(chatId);
                user.setLatitude(location.getLatitude());
                user.setLongitude(location.getLongitude());
                log.info("Save user in base" + user);
                userService.saveOrUpdate(user);

                chatStateData.setChatState(chatId, ChatState.WAITING_COMMAND);
                log.info("set chatState:" +  ChatState.WAITING_COMMAND);

            } else {
                createResponseWAITING_GEOMARK(message);
            }
        }else if (chatState == ChatState.WAITING_GEOMARK_WEEK){ //Погода на неделю.
            if (update.hasMessage() && update.getMessage().hasLocation()) {
                Location location = update.getMessage().getLocation();
                createResponseForcast(message, 7, location, chatId);

                User user = new User();
                user.setChatId(chatId);
                user.setLatitude(location.getLatitude());
                user.setLongitude(location.getLongitude());
                log.info("Save user in base" + user);
                userService.saveOrUpdate(user);

                chatStateData.setChatState(chatId, ChatState.WAITING_COMMAND);
                log.info("set chatState:" +  ChatState.WAITING_COMMAND);

            } else {
                createResponseWAITING_GEOMARK(message);
            }
        }

        return message;

    }


    private void createResponseForcast(SendMessage message, Integer daysForecast, Location location,Long chatId) {
        Double lat = location.getLatitude();
        Double lon = location.getLongitude();
        try {
            message.setText(yandexAPIService.getForcast(daysForecast, lat, lon));
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
        log.info("Update in base:" + chatSettings.toString());
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

    private void createWeatherChangeRecommendation(SendMessage message, Location location, Long chatId) {

        Double lat = location.getLatitude();
        Double lon = location.getLongitude();
        message.setText(yandexAPIService.getWeatherChangeRecommendation(2,lat, lon));

        setMainMenu(message,chatId);
    }

    private void setMainMenu(SendMessage message, Long chatId) {
        //Установим keyboard
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Будет ли сегодня дождь?"));
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

//        KeyboardRow row4 = new KeyboardRow();
//        row4.add(new KeyboardButton("Рекоммендации о перепадах"));
//        keyboard.add(row4);

        KeyboardRow row5 = new KeyboardRow();
        Optional<ChatSettings> chatSettings = chatSettingsService.findByChatIdAndMailingIsTrue(chatId);
        if (chatSettings.isPresent()) {
            row5.add(new KeyboardButton("Отписаться на рассылки"));
        }else{
            row5.add(new KeyboardButton("Подписаться на рассылку"));
        }
        keyboard.add(row5);

        replyKeyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(replyKeyboardMarkup);



    }
}
