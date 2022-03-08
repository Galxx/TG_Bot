package org.group_gb.tg_bot.service;

import org.group_gb.tg_bot.models.User;
import org.group_gb.tg_bot.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Location;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void save(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void saveOrUpdate(User user) {
        User user1 = userRepository.findByChatId(user.getChatId());
        if (user1 != null) {
            userRepository.deleteById(user1.getId());
        }
        save(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User findByChatId(long chatId) {
        return userRepository.findByChatId(chatId);
    }

}
