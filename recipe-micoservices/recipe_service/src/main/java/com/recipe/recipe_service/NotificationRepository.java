package com.recipe.recipe_service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserEmailOrderByCreatedAtDesc(String userEmail);
    
    List<Notification> findByUserEmailAndIsReadFalseOrderByCreatedAtDesc(String userEmail);
    
    long countByUserEmailAndIsReadFalse(String userEmail);
}
