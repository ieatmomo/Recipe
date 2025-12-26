package com.recipe.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for assigning Access Control Groups to users or recipes
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcgAssignmentRequest {
    private Set<String> accessControlGroups;
    private String action; // "ADD" or "REMOVE"
}
