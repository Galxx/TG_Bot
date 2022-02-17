package org.group_gb.tg_bot.service;

import org.group_gb.tg_bot.models.User;
import org.group_gb.tg_bot.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Location;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void save(User user) {
        userRepository.save(user);
    }


    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User returnCurrentUserWithLocationWithoutSavingToDB(User user, Location location) {
        user.setLongitude(location.getLongitude());
        user.setLatitude(location.getLatitude());
        return user;
    }



}
