package com.driftstay.infrastructure.cache;

import com.driftstay.property.dto.response.PropertySummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeaturedPropertiesCacheService {

    private final CacheService cacheService;

    public void cacheFeaturedProperties(List<PropertySummaryResponse> properties) {
        cacheService.put(CacheKeys.featured(), properties, Duration.ofHours(1));
    }

    @SuppressWarnings("unchecked")
    public Optional<List<PropertySummaryResponse>> getCachedFeaturedProperties() {
        return cacheService.get(CacheKeys.featured(), List.class)
                .map(list -> (List<PropertySummaryResponse>) list);
    }

    public void evictFeaturedProperties() {
        cacheService.evict(CacheKeys.featured());
    }
}
