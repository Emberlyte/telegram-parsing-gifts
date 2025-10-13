package com.github.emberlyte.telegramparsinggifts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TelegramParsingGiftsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramParsingGiftsApplication.class, args);
    }

}
