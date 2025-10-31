package com.github.emberlyte.telegramparsinggifts.Services;

import com.github.emberlyte.telegramparsinggifts.Models.Gift;
import com.github.emberlyte.telegramparsinggifts.Models.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
@RequiredArgsConstructor
public class RedisGiftService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String GIFT_KEY_PREFIX = "gift:";
    private static final String NOTIFICATION_KEY_PREFIX = "notification:";
    private static final String ALL_GIFTS_KEY = "gifts:all";
    private static final String UNSENT_NOTIFICATIONS_KEY = "notifications:unsent";

    // Сохранить подарок
    public void saveGift(Gift gift) {
        String key = GIFT_KEY_PREFIX + gift.getId();
        redisTemplate.opsForValue().set(key, gift);

        // Добавляем ID в список всех подарков
        redisTemplate.opsForSet().add(ALL_GIFTS_KEY, gift.getId());

        log.info("Подарок сохранен в Redis: {}", gift.getEmoji());
    }

    // Получить подарок по ID
    public Gift getGift(String giftId) {
        String key = GIFT_KEY_PREFIX + giftId;
        return (Gift) redisTemplate.opsForValue().get(key);
    }

    // Получить все подарки
    public List<Gift> getAllGifts() {
        Set<Object> giftIds = redisTemplate.opsForSet().members(ALL_GIFTS_KEY);
        List<Gift> gifts = new ArrayList<>();

        if (giftIds != null) {
            for (Object giftId : giftIds) {
                Gift gift = getGift(giftId.toString());
                if (gift != null) {
                    gifts.add(gift);
                }
            }
        }

        return gifts;
    }

    // Сохранить уведомление
    public void saveNotification(Notification notification) {
        notification.setId(UUID.randomUUID().toString());
        String key = NOTIFICATION_KEY_PREFIX + notification.getId();

        redisTemplate.opsForValue().set(key, notification, 7, TimeUnit.DAYS);

        // Добавляем в список неотправленных
        if (!notification.isSent()) {
            redisTemplate.opsForList().rightPush(UNSENT_NOTIFICATIONS_KEY, notification.getId());
        }

        log.info("Уведомление сохранено: {}", notification.getMessage());
    }

    // Получить неотправленные уведомления
    public List<Notification> getUnsentNotifications() {
        List<Object> notificationIds = redisTemplate.opsForList()
                .range(UNSENT_NOTIFICATIONS_KEY, 0, -1);

        List<Notification> notifications = new ArrayList<>();

        if (notificationIds != null) {
            for (Object id : notificationIds) {
                String key = NOTIFICATION_KEY_PREFIX + id.toString();
                Notification notification = (Notification) redisTemplate.opsForValue().get(key);
                if (notification != null && !notification.isSent()) {
                    notifications.add(notification);
                }
            }
        }

        return notifications;
    }

    // Пометить уведомление как отправленное
    public void markNotificationAsSent(String notificationId) {
        String key = NOTIFICATION_KEY_PREFIX + notificationId;
        Notification notification = (Notification) redisTemplate.opsForValue().get(key);

        if (notification != null) {
            notification.setSent(true);
            redisTemplate.opsForValue().set(key, notification, 7, TimeUnit.DAYS);

            // Удаляем из списка неотправленных
            redisTemplate.opsForList().remove(UNSENT_NOTIFICATIONS_KEY, 1, notificationId);
        }
    }
}