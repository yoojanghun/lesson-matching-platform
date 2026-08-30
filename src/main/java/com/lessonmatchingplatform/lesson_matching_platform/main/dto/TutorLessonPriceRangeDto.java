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
}
