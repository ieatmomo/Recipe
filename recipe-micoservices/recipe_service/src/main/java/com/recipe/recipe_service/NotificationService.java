package com.recipe.recipe_service;

import com.recipe.common.clients.AuthServiceClient;
import com.recipe.common.clients.KeycloakClient;
import com.recipe.common.entities.RecipeEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired(required = false)
    private AuthServiceClient authServiceClient;
    
    @Autowired(required = false)
    private KeycloakClient keycloakClient;
    
    @Value("${auth.mode:keycloak}")
    private String authMode;
    
    /**
     * Create notifications for users interested in the recipe's community tags
     */
    public void createNotificationsForRecipe(RecipeEntity recipe) {
        if (recipe.getCommunityTags() == null || recipe.getCommunityTags().isEmpty()) {
            logger.info("No community tags on recipe {}, skipping notifications", recipe.getName());
            return;
        }
        
        // Select the appropriate client based on auth mode
        boolean useKeycloak = "keycloak".equalsIgnoreCase(authMode);
        
        if (useKeycloak && keycloakClient == null) {
            logger.warn("Keycloak mode enabled but KeycloakClient not available, cannot create notifications");
            return;
        }
        
        if (!useKeycloak && authServiceClient == null) {
            logger.warn("JWT mode enabled but AuthServiceClient not available, cannot create notifications");
            return;
        }
        
        logger.info("Creating notifications for recipe: {} with tags: {} (using {})", 
            recipe.getName(), recipe.getCommunityTags(), useKeycloak ? "Keycloak" : "JWT");
        
        // For each community tag, find users with matching COI
        for (String tag : recipe.getCommunityTags()) {
            try {
                // Get users with this COI from appropriate auth service
                List<String> userEmails = useKeycloak 
                    ? keycloakClient.getUsersWithCOI(tag)
                    : authServiceClient.getUsersWithCOI(tag);
                
                logger.info("Found {} users interested in {}", userEmails.size(), tag);
                
                // Create notification for each user
                for (String userEmail : userEmails) {
                    // Don't notify the recipe creator
                    if (!userEmail.equals(recipe.getOwnerEmail())) {
                        String message = String.format("New %s recipe added: %s", tag, recipe.getName());
                        Notification notification = new Notification(
                            userEmail,
                            message,
                            recipe.getId(),
                            recipe.getName(),
                            tag
                        );
                        notificationRepository.save(notification);
                        logger.info("Created notification for user {} about recipe {}", userEmail, recipe.getName());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to create notifications for tag {}: {}", tag, e.getMessage(), e);
            }
        }
    }
    
    /**
     * Get all notifications for a user
     */
    public List<Notification> getUserNotifications(String userEmail) {
        return notificationRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
    }
    
    /**
     * Get unread notifications for a user
     */
    public List<Notification> getUnreadNotifications(String userEmail) {
        return notificationRepository.findByUserEmailAndIsReadFalseOrderByCreatedAtDesc(userEmail);
    }
    
    /**
     * Get count of unread notifications
     */
    public long getUnreadCount(String userEmail) {
        return notificationRepository.countByUserEmailAndIsReadFalse(userEmail);
    }
    
    /**
     * Mark notification as read
     */
    public boolean markAsRead(Long notificationId, String userEmail) {
        return notificationRepository.findById(notificationId)
            .map(notification -> {
                // Verify the notification belongs to the user
                if (notification.getUserEmail().equals(userEmail)) {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                    logger.info("Marked notification {} as read for user {}", notificationId, userEmail);
                    return true;
                }
                return false;
            })
            .orElse(false);
    }
    
    /**
     * Mark all notifications as read for a user
     */
    public void markAllAsRead(String userEmail) {
        List<Notification> unreadNotifications = getUnreadNotifications(userEmail);
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
        logger.info("Marked {} notifications as read for user {}", unreadNotifications.size(), userEmail);
    }
    
    /**
     * Delete a notification
     */
    public boolean deleteNotification(Long notificationId, String userEmail) {
        return notificationRepository.findById(notificationId)
            .map(notification -> {
                // Verify the notification belongs to the user
                if (notification.getUserEmail().equals(userEmail)) {
                    notificationRepository.delete(notification);
                    logger.info("Deleted notification {} for user {}", notificationId, userEmail);
                    return true;
                }
                return false;
            })
            .orElse(false);
    }
}
