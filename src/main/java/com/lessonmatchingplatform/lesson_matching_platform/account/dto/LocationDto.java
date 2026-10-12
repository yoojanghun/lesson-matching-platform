package com.lessonmatchingplatform.lesson_matching_platform.account.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.Location;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public record LocationDto(
        Long locationId,
        Long parentId,
        String name
) {

    public LocationDto(Long locationId, String name) {
        this(locationId, null, name);
    }

    public static LocationDto of(Location location) {
        return new LocationDto(
                location.getLocationId(),
                location.getParent() != null ? location.getParent().getLocationId() : null,
                location.getName()
        );
    }
}
