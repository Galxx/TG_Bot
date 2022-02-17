package org.group_gb.tg_bot.repositories;

import org.group_gb.tg_bot.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.telegram.telegrambots.meta.api.objects.Location;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
