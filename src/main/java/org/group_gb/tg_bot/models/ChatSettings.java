package org.group_gb.tg_bot.models;

import javax.persistence.*;

@Entity
@Table(name = "chat_settings")
public class ChatSettings{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "chat_id")
    private long chatId;

    @Column(name = "mailing")
    private boolean mailing;


    public long getId() {
        return this.id;
    }

    public long getChatId() {
        return this.chatId;
    }

    public boolean isMailing() {
        return this.mailing;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public void setMailing(boolean mailing) {
        this.mailing = mailing;
    }
}
