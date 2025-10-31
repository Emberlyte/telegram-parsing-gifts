package com.github.emberlyte.telegramparsinggifts.Models;

import com.github.emberlyte.telegramparsinggifts.Enums.GiftEnums;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Gift {
    private String id;
    private String emoji;
    private Integer starCount;
    private Integer remainingCount;
    private Integer totalCount;
    private Integer previousRemainingCount;
    private LocalDateTime firstSeenAt;
    private LocalDateTime lastUpdatedAt;
    private GiftEnums status;

    public String generateId() {
        return emoji + "_" + starCount;
    }

}
