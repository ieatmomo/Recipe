-- Migration: Create notifications table for COI-based recipe notifications
-- Version: 3
-- Description: Stores notifications sent to users when recipes matching their COI are created

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_email VARCHAR(255) NOT NULL,
    message VARCHAR(500) NOT NULL,
    recipe_id BIGINT,
    recipe_name VARCHAR(255),
    community_tag VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    
    -- Indexes for performance
    INDEX idx_user_email (user_email),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at),
    INDEX idx_user_unread (user_email, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Note: With spring.jpa.hibernate.ddl-auto=update, this table will be auto-created
-- from the Notification entity. This migration is for manual deployment scenarios.
