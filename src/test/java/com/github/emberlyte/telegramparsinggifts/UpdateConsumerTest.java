package com.github.emberlyte.telegramparsinggifts;

import com.github.emberlyte.telegramparsinggifts.Consumers.UpdateConsumer;
import com.github.emberlyte.telegramparsinggifts.Services.JsonParsingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@SpringBootTest(classes = UpdateConsumer.class)
@TestPropertySource(properties = {
        "TELEGRAM_BOT_TOKEN=test",
        "TELEGRAM_CHAT_ID=987654321"
})
public class UpdateConsumerTest {

    @Autowired
    private UpdateConsumer updateConsumer;

    @MockitoBean
    private JsonParsingService jsonParsingService;

    @MockitoBean
    private TelegramClient telegramClient;

    private final Long TEST_CHAT_ID = 716285L;

    @Test
    void testSendNewGiftMessage_GiftsAvailable() throws Exception {
        ReflectionTestUtils.setField(updateConsumer, "telegramClient", telegramClient);

        ReflectionTestUtils.setField(updateConsumer, "mainChatId", TEST_CHAT_ID);

        String giftMessage = "🎁 Доступные подарки:\nТестовый подарок | ⭐️ 1 | Осталось: 5";
        Mockito.when(jsonParsingService.parsingJson()).thenReturn(giftMessage);

        ReflectionTestUtils.invokeMethod(updateConsumer, "sendNewGiftMessage");

        Mockito.verify(telegramClient, times(1)).execute(any(SendMessage.class));

        SendMessage expectedMessage = SendMessage.builder()
                .text(giftMessage)
                .chatId(TEST_CHAT_ID)
                .build();
        Mockito.verify(telegramClient).execute(expectedMessage);
    }

    @Test
    void testSendNewGiftMessage_NoGifts() throws Exception {
        ReflectionTestUtils.setField(updateConsumer, "telegramClient", telegramClient);

        ReflectionTestUtils.setField(updateConsumer, "mainChatId", TEST_CHAT_ID);

        Mockito.when(jsonParsingService.parsingJson()).thenReturn(null);

        ReflectionTestUtils.invokeMethod(updateConsumer, "sendNewGiftMessage");

        Mockito.verify(telegramClient, never()).execute(any(SendMessage.class));
    }
}