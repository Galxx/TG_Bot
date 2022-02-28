package org.group_gb.tg_bot.botState;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatStateDataImpl implements ChatStateData{

    private Map<Long, ChatState> chatsStates =  new ConcurrentHashMap<>();


    @Override
    public void setChatState(Long chatId, ChatState chatState) {
        chatsStates.put(chatId, chatState);
    }

    @Override
    public ChatState getChatState(Long chatId) {
        return chatsStates.get(chatId);
    }
}
