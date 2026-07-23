package com.driftstay.infrastructure.cache;

import com.driftstay.property.dto.response.PropertySummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PropertySearchCacheService {

    private final CacheService cacheService;

    public void cacheSearchResults(String queryHash, List<PropertySummaryResponse> results) {
        cacheService.put(CacheKeys.search(queryHash), results, Duration.ofMinutes(10));
    }

    @SuppressWarnings("unchecked")
    public Optional<List<PropertySummaryResponse>> getCachedSearchResults(String queryHash) {
        return cacheService.get(CacheKeys.search(queryHash), List.class)
                .map(list -> (List<PropertySummaryResponse>) list);
    }

    public void evictSearchResults(String queryHash) {
        cacheService.evict(CacheKeys.search(queryHash));
    }

    public void evictAllSearchResults() {
        cacheService.evictByPattern(CacheKeys.search("*"));
    }
}
