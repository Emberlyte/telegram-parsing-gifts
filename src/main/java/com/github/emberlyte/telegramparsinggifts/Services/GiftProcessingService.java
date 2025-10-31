package com.github.emberlyte.telegramparsinggifts.Services;

import com.github.emberlyte.telegramparsinggifts.Enums.GiftEnums;
import com.github.emberlyte.telegramparsinggifts.Enums.NotificationType;
import com.github.emberlyte.telegramparsinggifts.Models.Gift;
import com.github.emberlyte.telegramparsinggifts.Models.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Log4j2
@RequiredArgsConstructor
public class GiftProcessingService {

    private final RedisGiftService redisGiftService;

    // Обработать подарок из API
    public void processGift(String emoji, Integer starCount, Integer remainingCount, Integer totalCount) {
        String giftId = emoji + "_" + starCount;
        Gift existingGift = redisGiftService.getGift(giftId);

        if (existingGift == null) {
            // Новый подарок
            Gift newGift = createNewGift(emoji, starCount, remainingCount, totalCount);
            redisGiftService.saveGift(newGift);

            createNotification(newGift, NotificationType.NEW_GIFT,
                    String.format("🎁 Новый подарок: %s за %d ⭐️ (Доступно: %d из %d)",
                            emoji, starCount, remainingCount, totalCount));
        } else {
            // Проверяем изменения
            checkAndNotifyChanges(existingGift, remainingCount);
            updateGift(existingGift, remainingCount, totalCount);
        }
    }

    private Gift createNewGift(String emoji, Integer starCount, Integer remainingCount, Integer totalCount) {
        Gift gift = new Gift();
        gift.setId(emoji + "_" + starCount);
        gift.setEmoji(emoji);
        gift.setStarCount(starCount);
        gift.setRemainingCount(remainingCount);
        gift.setPreviousRemainingCount(remainingCount);
        gift.setTotalCount(totalCount);
        gift.setFirstSeenAt(LocalDateTime.now());
        gift.setLastUpdatedAt(LocalDateTime.now());
        gift.setStatus(GiftEnums.AVAILABLE);

        log.info("Создан новый подарок: {} за {} звезд ({} из {})", emoji, starCount, remainingCount, totalCount);
        return gift;
    }

    private void checkAndNotifyChanges(Gift gift, Integer newRemainingCount) {
        Integer oldCount = gift.getRemainingCount();

        if (oldCount == null || oldCount.equals(newRemainingCount)) {
            return; // Нет изменений
        }

        // Подарок распродан
        if (oldCount > 0 && newRemainingCount == 0) {
            createNotification(gift, NotificationType.SOLD_OUT,
                    String.format("🔴 РАСПРОДАНО: %s за %d ⭐️",
                            gift.getEmoji(), gift.getStarCount()));
            gift.setStatus(GiftEnums.SOLD_OUT);
        }
        // Подарок снова в наличии (пополнение)
        else if (oldCount == 0 && newRemainingCount > 0) {
            createNotification(gift, NotificationType.RESTOCKED,
                    String.format("🟢 ПОПОЛНЕНИЕ: %s за %d ⭐️ (Доступно: %d из %d)",
                            gift.getEmoji(), gift.getStarCount(), newRemainingCount, gift.getTotalCount()));
            gift.setStatus(GiftEnums.RESTOCKED);
        }
    }

    private void updateGift(Gift gift, Integer remainingCount, Integer totalCount) {
        gift.setPreviousRemainingCount(gift.getRemainingCount());
        gift.setRemainingCount(remainingCount);
        gift.setTotalCount(totalCount);
        gift.setLastUpdatedAt(LocalDateTime.now());

        if (remainingCount > 0) {
            gift.setStatus(GiftEnums.AVAILABLE);
        }

        redisGiftService.saveGift(gift);
    }

    private void createNotification(Gift gift, NotificationType type, String message) {
        Notification notification = new Notification();
        notification.setGiftId(gift.getId());
        notification.setEmoji(gift.getEmoji());
        notification.setType(type);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setSent(false);

        redisGiftService.saveNotification(notification);
        log.info("Создано уведомление: {}", message);
    }

    // Получить список доступных подарков
    public String getAvailableGiftsMessage() {
        var gifts = redisGiftService.getAllGifts();
        StringBuilder message = new StringBuilder("🎁 Доступные подарки:\n\n");

        boolean hasAvailable = false;
        for (Gift gift : gifts) {
            if (gift.getRemainingCount() != null && gift.getRemainingCount() > 0) {
                hasAvailable = true;
                message.append(String.format("%s | ⭐️ %d | Осталось: %d из %d\n",
                        gift.getEmoji(),
                        gift.getStarCount(),
                        gift.getRemainingCount(),
                        gift.getTotalCount() != null ? gift.getTotalCount() : gift.getRemainingCount()));
            }
        }

        return hasAvailable ? message.toString() : "В данный момент подарки недоступны 😔";
    }
}