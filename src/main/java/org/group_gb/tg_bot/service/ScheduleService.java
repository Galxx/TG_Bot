package org.group_gb.tg_bot.service;

import org.group_gb.tg_bot.models.User;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

@Service
public class ScheduleService {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ScheduleService.class);

    private final EntityManagerFactory em;
    private final MailingService mailingService;

    public ScheduleService(EntityManagerFactory em, MailingService mailingService) {
        this.em = em;
        this.mailingService = mailingService;
    }

    @Scheduled(cron = "@hourly")
    public void sendMessages() {

        EntityManager entityManager = em.createEntityManager();
        List<User> users = entityManager.createQuery("select u from User as u join ChatSettings as cs on u.chatId = cs.chatId where cs.mailing = true ")
                .getResultList();

        for (User user:users) {
            mailingService.sendMessage(user);
        }

    }
}
