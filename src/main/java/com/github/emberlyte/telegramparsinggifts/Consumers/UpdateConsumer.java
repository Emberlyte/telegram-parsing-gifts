package com.github.emberlyte.telegramparsinggifts.Consumers;

import com.github.emberlyte.telegramparsinggifts.Services.JsonParsingService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Objects;

@Service
@Log4j2
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    @Value("${TELEGRAM_CHAT_ID}")
    private Long mainChatId;

    private final TelegramClient telegramClient;
    private final JsonParsingService jsonParsingService;

    public UpdateConsumer(@Value("${TELEGRAM_BOT_TOKEN}") String token, JsonParsingService jsonParsingService) {
        this.telegramClient = new OkHttpTelegramClient(token);
        this.jsonParsingService = jsonParsingService;
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

        if ("/start".equals(text)) {
            String jsonMessage = jsonParsingService.parsingJson();

            if (jsonMessage != null && !jsonMessage.isEmpty()) {
                sendMessage(jsonMessage, chatId);
            } else {
                sendMessage("Не удалось получить информацию о подарках или доступные подарки отсутствуют.", chatId);
            }
        }
    }

    @Scheduled(fixedRate = 300)
    private void sendNewGiftMessage() {
        String message = jsonParsingService.parsingJson();
        if (message != null && !message.isEmpty()) {
            sendMessage(message, mainChatId);
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
            log.error("Error while send message", e);
        }
    }
}