package com.recipe.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for COI-based recipe notifications
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private String userEmail;
    private String recipeName;
    private Long recipeId;
    private Set<String> matchingCommunities;
    private LocalDateTime timestamp;
    private String message;
}
