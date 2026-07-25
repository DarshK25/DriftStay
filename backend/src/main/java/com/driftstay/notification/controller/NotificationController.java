package com.driftstay.notification.controller;

import com.driftstay.notification.entity.Notification;
import com.driftstay.notification.service.NotificationService;
import com.driftstay.security.filter.JwtUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(
            @AuthenticationPrincipal JwtUser currentUser) {
        // TODO: Implement notification list
        return ResponseEntity.ok(List.of());
    }
}
