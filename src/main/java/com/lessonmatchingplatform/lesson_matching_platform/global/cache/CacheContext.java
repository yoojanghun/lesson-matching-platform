package com.lessonmatchingplatform.lesson_matching_platform.global.cache;

/**
 * 현재 요청 스레드의 캐시 상태(HIT, STALE, MISS)를 전달하기 위한 컨텍스트
 */
public class CacheContext {

    private static final ThreadLocal<String> CACHE_STATUS = new ThreadLocal<>();

    public static void setStatus(String status) {
        CACHE_STATUS.set(status);
    }

    public static String getStatus() {
        return CACHE_STATUS.get();
    }

    public static String getAndClearStatus() {
        String status = CACHE_STATUS.get();
        CACHE_STATUS.remove();
        return status;
    }

    public static void clear() {
        CACHE_STATUS.remove();
    }
}
