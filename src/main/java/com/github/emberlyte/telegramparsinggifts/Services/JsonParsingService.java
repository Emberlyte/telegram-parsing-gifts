package com.github.emberlyte.telegramparsinggifts.Services;

import com.github.emberlyte.telegramparsinggifts.Models.ApiResponse;
import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Log4j2
public class JsonParsingService {

    @Value("${TELEGRAM_BOT_TOKEN}")
    private String botToken;

    public String parsingJson() {
        String api = "https://api.telegram.org/bot" + botToken + "/getAvailableGifts";

        try {
            Gson gson = new Gson();
            RestTemplate restTemplate = new RestTemplate();
            String json = restTemplate.getForObject(api, String.class);

            ApiResponse response = gson.fromJson(json, ApiResponse.class);

            if (response == null || response.getResult() == null) {
                return null;
            }

            StringBuilder message = new StringBuilder("🎁 Доступные подарки:\n\n");
            boolean hasGifts = false;

            for (ApiResponse.Gift gift : response.getResult().getGifts()) {
                if (gift.getRemainingCount() != null) {
                    hasGifts = true;
                    message.append(String.format("%s | ⭐️ %d | Осталось: %d\n",
                            gift.getSticker().getEmoji(),
                            gift.getStarCount(),
                            gift.getRemainingCount()));

                    log.info("Эмодзи: {} | Цена: {} | Осталось: {}",
                            gift.getSticker().getEmoji(),
                            gift.getStarCount(),
                            gift.getRemainingCount());
                }
            }

            return hasGifts ? message.toString() : null;
        } catch (Exception e) {
            log.error("Ошибка парсинга: ", e);
            return null;
        }
    }
}