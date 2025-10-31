package com.github.emberlyte.telegramparsinggifts.Models;

import com.github.emberlyte.telegramparsinggifts.Enums.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Notification {
    private String id;
    private String giftId;
    private String emoji;
    private NotificationType type;
    private String message;
    private LocalDateTime createdAt;
    private boolean sent;

}
