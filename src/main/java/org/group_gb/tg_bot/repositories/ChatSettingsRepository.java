package org.group_gb.tg_bot.repositories;

import org.group_gb.tg_bot.models.ChatSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface ChatSettingsRepository extends JpaRepository<ChatSettings, Long> {

    @Transactional
    void deleteByChatId(Long chatId);

    Optional<ChatSettings> findByChatIdAndMailingIsTrue(Long chatId);

}
