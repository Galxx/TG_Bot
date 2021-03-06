package org.group_gb.tg_bot.service;

import org.group_gb.tg_bot.models.ChatSettings;
import org.group_gb.tg_bot.repositories.ChatSettingsRepository;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class ChatSettingsService {

    private final ChatSettingsRepository chatSettingsRepository;

    public ChatSettingsService(ChatSettingsRepository chatSettingsRepository) {
        this.chatSettingsRepository = chatSettingsRepository;
    }

    public Optional<ChatSettings> findByChatIdAndMailingIsTrue(Long chatId){
        return chatSettingsRepository.findByChatIdAndMailingIsTrue(chatId);
    }

    @Transactional
    public void update(ChatSettings chatSettings){

        chatSettingsRepository.deleteByChatId(chatSettings.getChatId());
        chatSettingsRepository.save(chatSettings);

    }

}
