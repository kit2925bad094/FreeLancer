package com.freelancehub.controller;

import com.freelancehub.dto.ApiResponse;
import com.freelancehub.model.Notification;
import com.freelancehub.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<ArrayList<Notification>>> getByUserId(@PathVariable Long userId) {
        ArrayList<Notification> list = notificationService.getNotificationsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", list));
    }

    @GetMapping("/{userId}/unread-count")
    public ResponseEntity<ApiResponse<HashMap<String, Integer>>> getUnreadCount(@PathVariable Long userId) {
        int count = notificationService.getUnreadCount(userId);
        HashMap<String, Integer> map = new HashMap<>();
        map.put("unreadCount", count);
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved", map));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }
}
