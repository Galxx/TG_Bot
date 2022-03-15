package org.group_gb.tg_bot.exceptions;

public class YandexApiException extends RuntimeException {

    private final String message;

    public YandexApiException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

}
