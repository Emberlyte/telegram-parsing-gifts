package com.github.emberlyte.telegramparsinggifts.Models;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class ApiResponse {
    @SerializedName("result")
    private Result result;

    @Data
    public static class Result {
        @SerializedName("gifts")
        private List<Gift> gifts;
    }

    @Data
    public static class Gift {
        @SerializedName("sticker")
        private Sticker sticker;

        @SerializedName("star_count")
        private int starCount;

        @SerializedName("remaining_count")
        private Integer remainingCount;

        @SerializedName("total_count")
        private Integer totalCount;
    }

    @Data
    public static class Sticker {
        @SerializedName("emoji")
        private String emoji;
    }
}