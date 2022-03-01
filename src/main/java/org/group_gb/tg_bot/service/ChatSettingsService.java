package org.group_gb.tg_bot.service;

import org.group_gb.tg_bot.models.ChatSettings;
import org.group_gb.tg_bot.repositories.ChatSettingsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatSettingsService {

    private final ChatSettingsRepository chatSettingsRepository;

    public ChatSettingsService(ChatSettingsRepository chatSettingsRepository) {
        this.chatSettingsRepository = chatSettingsRepository;
    }

    public void save(ChatSettings chatSettings){

        chatSettingsRepository.save(chatSettings);

    }

    public List<ChatSettings> findChatSettings() {
        return chatSettingsRepository.findAll();
    }

    public Optional<ChatSettings> findByChatIdAndMailingIsTrue(Long chatId){
        return chatSettingsRepository.findByChatIdAndMailingIsTrue(chatId);
    }

    public void update(ChatSettings chatSettings){

        chatSettingsRepository.deleteByChatId(chatSettings.getChatId());
        chatSettingsRepository.save(chatSettings);

    }

}
