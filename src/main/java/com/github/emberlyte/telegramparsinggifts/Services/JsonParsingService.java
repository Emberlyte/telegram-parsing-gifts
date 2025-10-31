package com.github.emberlyte.telegramparsinggifts.Services;

import com.github.emberlyte.telegramparsinggifts.Models.ApiResponse;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Log4j2
@RequiredArgsConstructor
public class JsonParsingService {

    @Value("${TELEGRAM_BOT_TOKEN}")
    private String botToken;

    private final GiftProcessingService giftProcessingService;

    public void parsingAndSaveGifts() {
        String api = "https://api.telegram.org/bot" + botToken + "/getAvailableGifts";

        try {
            Gson gson = new Gson();
            RestTemplate restTemplate = new RestTemplate();
            String json = restTemplate.getForObject(api, String.class);

            ApiResponse response = gson.fromJson(json, ApiResponse.class);

            if (response == null || response.getResult() == null) {
                log.warn("Получен пустой ответ от API");
                return;
            }

            log.info("Начинаем парсинг подарков...");

            for (ApiResponse.Gift gift : response.getResult().getGifts()) {
                if (gift.getRemainingCount() != null) {
                    giftProcessingService.processGift(
                            gift.getSticker().getEmoji(),
                            gift.getStarCount(),
                            gift.getRemainingCount(),
                            gift.getTotalCount()
                    );
                }
            }

            log.info("Парсинг завершен успешно");
        } catch (Exception e) {
            log.error("Ошибка парсинга: ", e);
        }
    }

    // Получить сообщение с доступными подарками
    public String getAvailableGiftsMessage() {
        return giftProcessingService.getAvailableGiftsMessage();
    }
}