package com.driftstay.auth.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Map<String, BlacklistedEntry> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String tokenJti, long userId, Instant expiresAt) {
        blacklist.put(tokenJti, new BlacklistedEntry(userId, Instant.now(), expiresAt));
    }

    public boolean isBlacklisted(String tokenJti) {
        BlacklistedEntry entry = blacklist.get(tokenJti);
        if (entry == null) return false;
        if (Instant.now().isAfter(entry.expiresAt())) {
            blacklist.remove(tokenJti);
            return false;
        }
        return true;
    }

    public void removeExpired() {
        blacklist.entrySet().removeIf(e -> Instant.now().isAfter(e.getValue().expiresAt()));
    }

    private record BlacklistedEntry(long userId, Instant blacklistedAt, Instant expiresAt) {}
}
