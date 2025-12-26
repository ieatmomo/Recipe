package com.recipe.recipe_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Get all notifications for the authenticated user
     * GET /notifications
     */
    @GetMapping
    public ResponseEntity<?> getNotifications(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        
        String userEmail = authentication.getName();
        logger.info("Fetching notifications for user: {}", userEmail);
        
        try {
            List<Notification> notifications = notificationService.getUserNotifications(userEmail);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error fetching notifications for user {}: {}", userEmail, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching notifications");
        }
    }
    
    /**
     * Get unread notifications for the authenticated user
     * GET /notifications/unread
     */
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        
        String userEmail = authentication.getName();
        logger.info("Fetching unread notifications for user: {}", userEmail);
        
        try {
            List<Notification> notifications = notificationService.getUnreadNotifications(userEmail);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error fetching unread notifications for user {}: {}", userEmail, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching unread notifications");
        }
    }
    
    /**
     * Get unread notification count
     * GET /notifications/unread/count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadCount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        
        String userEmail = authentication.getName();
        
        try {
            long count = notificationService.getUnreadCount(userEmail);
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching unread count for user {}: {}", userEmail, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching unread count");
        }
    }
    
    /**
     * Mark a notification as read
     * PUT /notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        
        String userEmail = authentication.getName();
        logger.info("Marking notification {} as read for user: {}", id, userEmail);
        
        try {
            boolean success = notificationService.markAsRead(id, userEmail);
            if (success) {
                return ResponseEntity.ok("Notification marked as read");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Notification not found or does not belong to user");
            }
        } catch (Exception e) {
            logger.error("Error marking notification {} as read: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error marking notification as read");
        }
    }
    
    /**
     * Mark all notifications as read
     * PUT /notifications/read-all
     */
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        
        String userEmail = authentication.getName();
        logger.info("Marking all notifications as read for user: {}", userEmail);
        
        try {
            notificationService.markAllAsRead(userEmail);
            return ResponseEntity.ok("All notifications marked as read");
        } catch (Exception e) {
            logger.error("Error marking all notifications as read for user {}: {}", userEmail, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error marking all notifications as read");
        }
    }
    
    /**
     * Delete a notification
     * DELETE /notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        
        String userEmail = authentication.getName();
        logger.info("Deleting notification {} for user: {}", id, userEmail);
        
        try {
            boolean success = notificationService.deleteNotification(id, userEmail);
            if (success) {
                return ResponseEntity.ok("Notification deleted");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Notification not found or does not belong to user");
            }
        } catch (Exception e) {
            logger.error("Error deleting notification {} for user {}: {}", id, userEmail, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting notification");
        }
    }
}
