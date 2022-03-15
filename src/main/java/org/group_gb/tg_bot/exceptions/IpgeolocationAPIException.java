package org.group_gb.tg_bot.exceptions;

public class IpgeolocationAPIException extends RuntimeException {

        private final String message;

        public IpgeolocationAPIException(String message) {
            this.message = message;
        }

        @Override
        public String getMessage() {
            return this.message;
        }


}
