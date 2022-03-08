package org.group_gb.tg_bot.bot_state;

public interface ChatStateData {

    void setChatState(Long chatId, ChatState chatState);

    ChatState getChatState(Long chatId);

}
