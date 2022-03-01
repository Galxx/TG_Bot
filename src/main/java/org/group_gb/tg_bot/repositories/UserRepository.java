package org.group_gb.tg_bot.repositories;

import org.group_gb.tg_bot.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

//    @Query(value = "SELECT MAX(u.id) from User u where u.chat_id = :chat")
//    long existsByChatId(@Param(value = "chat") final long chatId);
//
//    @Modifying
//    @Query(value = "UPDATE USER u set u = :2 where u.chat_id = :1")
//    void update(long chatId, User user);
}
