package org.group_gb.tg_bot.botState;

public interface ChatStateData {

    void setChatState(Long chatId, ChatState chatState);

    ChatState getChatState(Long chatId);

}
