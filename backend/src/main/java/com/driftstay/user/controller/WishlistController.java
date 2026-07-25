package com.driftstay.user.controller;

import com.driftstay.user.entity.Wishlist;
import com.driftstay.user.service.WishlistService;
import com.driftstay.security.filter.JwtUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping
    public ResponseEntity<Wishlist> addToWishlist(
            @RequestParam Long propertyId,
            @AuthenticationPrincipal JwtUser currentUser) {
        Wishlist item = wishlistService.addToWishlist(currentUser.getUserId(), propertyId);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<Void> removeFromWishlist(
            @PathVariable Long propertyId,
            @AuthenticationPrincipal JwtUser currentUser) {
        wishlistService.removeFromWishlist(currentUser.getUserId(), propertyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Wishlist>> getWishlist(
            @AuthenticationPrincipal JwtUser currentUser) {
        return ResponseEntity.ok(wishlistService.getWishlist(currentUser.getUserId()));
    }
}
