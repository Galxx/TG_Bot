package org.group_gb.tg_bot;

import org.group_gb.tg_bot.bot_state.ChatState;
import org.group_gb.tg_bot.bot_state.ChatStateDataImpl;
import org.group_gb.tg_bot.models.ChatSettings;
import org.group_gb.tg_bot.models.User;
import org.group_gb.tg_bot.service.ChatSettingsService;
import org.group_gb.tg_bot.service.TelegramBotService;
import org.group_gb.tg_bot.service.UserService;
import org.group_gb.tg_bot.yandex_api.YandexAPIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
class TgBotApplicationTests {

    @InjectMocks
    private TelegramBotService telegramBotService;

    @Mock
    ChatStateDataImpl mockСhatStateData;

    @Mock
    UserService mockUserService;

    @Mock
    ChatSettingsService mockChatSettingsService;

    @Mock
    YandexAPIService yandexAPIService;

    Update mockUpdate = mock(Update.class);
    Message mockMessage = mock(Message.class);
    Location mockLocation = mock(Location.class);;

    @Test
    void contextLoads() {

    }

    @BeforeEach
    void init(){
        when(mockUpdate.getMessage()).thenReturn(mockMessage);
        when(mockUpdate.hasMessage()).thenReturn(true);

        when(mockMessage.getChatId()).thenReturn(132L);
        when(mockMessage.getLocation()).thenReturn(mockLocation);

        when(mockLocation.getLatitude()).thenReturn(10D);
        when(mockLocation.getLongitude()).thenReturn(10D);

        doNothing().when(mockChatSettingsService).update(any(ChatSettings.class));
        doNothing().when(mockUserService).save(any(User.class));
    }


    @Test
    @DisplayName("/start")
    void HandleUpdateStart() {
        when(mockMessage.hasText()).thenReturn(true);
        when(mockMessage.getText()).thenReturn("/start");
        assertEquals("Ожидаю команды", telegramBotService.handleUpdate(mockUpdate).getText());
    }

    @Test
    @DisplayName("Подписаться на рассылку о погоде")
    void HandleUpdateScheduleOn() {
        when(mockMessage.hasText()).thenReturn(true);
        when(mockMessage.getText()).thenReturn("Подписаться на рассылку о погоде");
        assertEquals("Вы успешно подписаны на рассылку", telegramBotService.handleUpdate(mockUpdate).getText());
    }

    @Test
    @DisplayName("Отписаться на рассылки о погоде")
    void HandleUpdateScheduleOff() {
        when(mockMessage.hasText()).thenReturn(true);
        when(mockMessage.getText()).thenReturn("Отписаться на рассылки о погоде");
        assertEquals("Вы успешно отписаны от рассылки", telegramBotService.handleUpdate(mockUpdate).getText());
    }

    @Test
    @DisplayName("Погода на неделю")
    void HandleUpdateRainQuestion() {
        when(mockMessage.hasText()).thenReturn(true);
        when(mockMessage.getText()).thenReturn("Погода на неделю");
        assertEquals("Отправьте, пожалуйста, геометку", telegramBotService.handleUpdate(mockUpdate).getText());
    }

    @Test
    @DisplayName("Location")
    void HandleUpdateLocation() {

        when(mockСhatStateData.getChatState(any(Long.class))).thenReturn(ChatState.WAITING_GEOMARK_NOW);
        when(mockMessage.hasLocation()).thenReturn(true);
        when(yandexAPIService.getForcast(null,any(Double.class),any(Double.class))).thenReturn("Ваш прогноз");
        assertEquals("Ваш прогноз", telegramBotService.handleUpdate(mockUpdate).getText());

    }

}
