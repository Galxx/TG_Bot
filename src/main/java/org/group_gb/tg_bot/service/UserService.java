package org.group_gb.tg_bot.service;

import org.group_gb.tg_bot.models.User;
import org.group_gb.tg_bot.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void save(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void saveOrUpdate(User user) {
        Optional<User> optionalUser = userRepository.findByChatId(user.getChatId());
        if (optionalUser.isPresent()) {
            user.setId(optionalUser.get().getId());
        }

        save(user);
    }

    @Transactional
    public void deleteByChatId(Long chatId){
        userRepository.deleteByChatId(chatId);
    }

    public Optional<User> findByChatId(Long chatId){
        return userRepository.findByChatId(chatId);
    }

}
