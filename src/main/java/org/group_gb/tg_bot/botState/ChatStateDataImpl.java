package org.group_gb.tg_bot.botState;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ChatStateDataImpl implements ChatStateData{

    private Map<Long, ChatState> chatsStates = new HashMap<>();


    @Override
    public void setChatState(Long chatId, ChatState chatState) {
        chatsStates.put(chatId, chatState);
    }

    @Override
    public ChatState getChatState(Long chatId) {
        ChatState chatState = chatsStates.get(chatId);

        return chatState;
    }
}
