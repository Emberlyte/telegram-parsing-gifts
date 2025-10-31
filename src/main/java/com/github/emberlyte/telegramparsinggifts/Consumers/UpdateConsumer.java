package com.github.emberlyte.telegramparsinggifts.Consumers;

import com.github.emberlyte.telegramparsinggifts.Models.Notification;
import com.github.emberlyte.telegramparsinggifts.Services.JsonParsingService;
import com.github.emberlyte.telegramparsinggifts.Services.RedisGiftService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Objects;

@Service
@Log4j2
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    @Value("${TELEGRAM_CHAT_ID}")
    private Long mainChatId;

    private final TelegramClient telegramClient;
    private final JsonParsingService jsonParsingService;
    private final RedisGiftService redisGiftService;

    public UpdateConsumer(@Value("${TELEGRAM_BOT_TOKEN}") String token,
                          JsonParsingService jsonParsingService,
                          RedisGiftService redisGiftService) {
        this.telegramClient = new OkHttpTelegramClient(token);
        this.jsonParsingService = jsonParsingService;
        this.redisGiftService = redisGiftService;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessageText(update);
        }
    }

    private void handleMessageText(Update update) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        if (!isAllowedChat(chatId)) {
            sendMessage("Бот только для @exikra", chatId);
            return;
        }

        if (text.equals("/start")) {
            String message = jsonParsingService.getAvailableGiftsMessage();
            sendMessage(message, chatId);
        }
    }

    // Парсинг каждые 5 минут (300000 мс)
    @Scheduled(fixedRate = 1000)
    private void scheduledParsing() {
        log.info("Запуск scheduled парсинга...");
        jsonParsingService.parsingAndSaveGifts();
    }

    // Отправка уведомлений каждые 10 секунд
    @Scheduled(fixedRate = 10000)
    private void sendPendingNotifications() {
        List<Notification> notifications = redisGiftService.getUnsentNotifications();

        if (notifications.isEmpty()) {
            return;
        }

        log.info("Найдено {} неотправленных уведомлений", notifications.size());

        for (Notification notification : notifications) {
            try {
                sendMessage(notification.getMessage(), mainChatId);
                redisGiftService.markNotificationAsSent(notification.getId());
                log.info("Уведомление отправлено: {}", notification.getMessage());

                // Небольшая задержка между сообщениями
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("Ошибка отправки уведомления: ", e);
            }
        }
    }

    private boolean isAllowedChat(Long chatId) {
        return Objects.equals(chatId, mainChatId);
    }

    private void sendMessage(String text, Long chatId) {
        if (text == null || text.isEmpty()) {
            log.warn("Попытка отправить пустое или NULL сообщение в чат {}", chatId);
            return;
        }

        SendMessage message = SendMessage
                .builder()
                .text(text)
                .chatId(chatId)
                .build();

        try {
            telegramClient.execute(message);
        } catch (Exception e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }
}