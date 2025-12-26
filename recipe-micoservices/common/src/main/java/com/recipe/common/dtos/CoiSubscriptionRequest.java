package com.recipe.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for managing user's Communities of Interest subscriptions
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoiSubscriptionRequest {
    private Set<String> communitiesOfInterest;
    private String action; // "ADD" or "REMOVE"
}
