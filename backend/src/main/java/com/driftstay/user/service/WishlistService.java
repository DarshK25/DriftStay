package com.driftstay.user.service;

import com.driftstay.user.entity.User;
import com.driftstay.user.entity.Wishlist;
import com.driftstay.user.repository.WishlistRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final EntityManager entityManager;

    @Transactional
    public Wishlist addToWishlist(Long userId, Long propertyId) {
        if (wishlistRepository.existsByUserIdAndPropertyId(userId, propertyId)) {
            throw new IllegalArgumentException("Property already in wishlist");
        }

        User user = entityManager.getReference(User.class, userId);
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setPropertyId(propertyId);
        Wishlist saved = wishlistRepository.save(wishlist);
        log.info("Added to wishlist: user={}, property={}", userId, propertyId);
        return saved;
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long propertyId) {
        wishlistRepository.findByUserIdAndPropertyId(userId, propertyId)
                .ifPresent(wishlistRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<Wishlist> getWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId);
    }
}
