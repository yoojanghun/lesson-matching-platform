package com.lessonmatchingplatform.lesson_matching_platform.main.dto;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorLessonPrice;

import java.util.List;

public record TutorLessonPriceRangeDto(
        Integer minPrice,
        Integer maxPrice,
        String priceDisplay
) {
    public static TutorLessonPriceRangeDto from(List<TutorLessonPrice> priceList) {
        if (priceList == null || priceList.isEmpty()) {
            return new TutorLessonPriceRangeDto(0, 0, "가격 협의");
        }
        int minPrice = priceList.stream().mapToInt(TutorLessonPrice::getPrice).min().orElse(0);
        int maxPrice = priceList.stream().mapToInt(TutorLessonPrice::getPrice).max().orElse(0);

        String priceDisplay = minPrice == maxPrice
                ? String.format("%,d원", minPrice)
                : String.format("%,d~%,d원", minPrice, maxPrice);

        return new TutorLessonPriceRangeDto(
                minPrice,
                maxPrice,
                priceDisplay
        );
    }

    public static TutorLessonPriceRangeDto of(Integer minPrice, Integer maxPrice) {
        if (minPrice == null && maxPrice == null) {
            return new TutorLessonPriceRangeDto(0, 0, "가격 협의");
        }
        int min = minPrice != null ? minPrice : 0;
        int max = maxPrice != null ? maxPrice : 0;

        String priceDisplay;
        if (minPrice != null && maxPrice != null) {
            priceDisplay = min == max
                    ? String.format("%,d원", min)
                    : String.format("%,d~%,d원", min, max);
        } else if (minPrice != null) {
            priceDisplay = String.format("%,d원~", min);
        } else {
            priceDisplay = String.format("~%,d원", max);
        }

        return new TutorLessonPriceRangeDto(
                min,
                max,
                priceDisplay
        );
    }
}
