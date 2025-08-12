package com.gsu25se05.itellispeak.utils;

import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {
    private static final ConcurrentHashMap<String, Long> cooldownMap = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 5000;

    public static boolean isCoolingDown(String ip) {
        Long lastTime = cooldownMap.get(ip);
        long now = System.currentTimeMillis();
        return lastTime != null && now - lastTime < COOLDOWN_MS;
    }

    public static long getRemaining(String ip) {
        Long lastTime = cooldownMap.get(ip);
        if (lastTime == null) return 0;
        return Math.max(0, (COOLDOWN_MS - (System.currentTimeMillis() - lastTime)) / 1000);
    }

    public static void update(String ip) {
        cooldownMap.put(ip, System.currentTimeMillis());
    }
}
