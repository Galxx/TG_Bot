package org.group_gb.tg_bot.bot_state;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatStateDataImpl implements ChatStateData {

    private Map<Long, ChatState> chatsStates;

    public ChatStateDataImpl() {
        chatsStates = new ConcurrentHashMap<>();
    }

    @Override
    public void setChatState(Long chatId, ChatState chatState) {
        chatsStates.put(chatId, chatState);
    }

    @Override
    public ChatState getChatState(Long chatId) {
        return chatsStates.get(chatId);
    }
}
