package com.lessonmatchingplatform.lesson_matching_platform.account.dto;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.Location;

public record LocationDto(
        Long locationId,
        String name
) {

    public static LocationDto of(Location location) {
        return new LocationDto(
                location.getLocationId(),
                location.getName()
        );
    }
}
