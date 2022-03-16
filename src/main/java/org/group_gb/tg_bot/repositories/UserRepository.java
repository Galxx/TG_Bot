package org.group_gb.tg_bot.repositories;

import org.group_gb.tg_bot.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByChatId(long chatId);

    void deleteByChatId(long chatId);
}
