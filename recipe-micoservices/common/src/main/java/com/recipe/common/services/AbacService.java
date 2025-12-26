package com.recipe.common.services;

import com.recipe.common.entities.RecipeEntity;
import com.recipe.common.entities.RecipeSearchEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ABAC (Attribute-Based Access Control) Service
 * Implements Access Control Group (ACG) based filtering logic
 */
@Service
public class AbacService {

    /**
     * Check if a user can access a recipe based on ACG membership
     * @param recipe The recipe to check access for
     * @param userAcgs The user's ACG memberships
     * @param isAdmin Whether the user has ADMIN role
     * @return true if user can access the recipe
     */
    public boolean canUserAccessRecipe(RecipeEntity recipe, Set<String> userAcgs, boolean isAdmin) {
        // Admins can access everything
        if (isAdmin) {
            return true;
        }

        // If recipe is not restricted, everyone can access
        if (recipe.getIsRestricted() == null || !recipe.getIsRestricted()) {
            return true;
        }

        // If restricted but has no ACGs set, treat as admin-only
        if (recipe.getAccessControlGroups() == null || recipe.getAccessControlGroups().isEmpty()) {
            return false;
        }

        // Check if user has at least one matching ACG
        if (userAcgs == null || userAcgs.isEmpty()) {
            return false;
        }

        return recipe.getAccessControlGroups().stream()
                .anyMatch(userAcgs::contains);
    }

    /**
     * Check if a user can access a recipe search entity
     */
    public boolean canUserAccessRecipe(RecipeSearchEntity recipe, Set<String> userAcgs, boolean isAdmin) {
        if (isAdmin) {
            return true;
        }

        if (recipe.getIsRestricted() == null || !recipe.getIsRestricted()) {
            return true;
        }

        if (recipe.getAccessControlGroups() == null || recipe.getAccessControlGroups().isEmpty()) {
            return false;
        }

        if (userAcgs == null || userAcgs.isEmpty()) {
            return false;
        }

        return recipe.getAccessControlGroups().stream()
                .anyMatch(userAcgs::contains);
    }

    /**
     * Filter a list of recipes based on user's ACG access
     */
    public List<RecipeEntity> filterRecipesByAccess(List<RecipeEntity> recipes, Set<String> userAcgs, boolean isAdmin) {
        return recipes.stream()
                .filter(recipe -> canUserAccessRecipe(recipe, userAcgs, isAdmin))
                .collect(Collectors.toList());
    }

    /**
     * Filter a list of recipe search entities based on user's ACG access
     */
    public List<RecipeSearchEntity> filterSearchRecipesByAccess(List<RecipeSearchEntity> recipes, Set<String> userAcgs, boolean isAdmin) {
        return recipes.stream()
                .filter(recipe -> canUserAccessRecipe(recipe, userAcgs, isAdmin))
                .collect(Collectors.toList());
    }

    /**
     * Check if user has matching Communities of Interest with recipe
     */
    public boolean hasMatchingCoi(Set<String> recipeCommunityTags, Set<String> userCois) {
        if (recipeCommunityTags == null || recipeCommunityTags.isEmpty()) {
            return false;
        }
        if (userCois == null || userCois.isEmpty()) {
            return false;
        }
        return recipeCommunityTags.stream().anyMatch(userCois::contains);
    }

    /**
     * Get the communities that match between recipe and user
     */
    public Set<String> getMatchingCommunities(Set<String> recipeCommunityTags, Set<String> userCois) {
        if (recipeCommunityTags == null || userCois == null) {
            return Set.of();
        }
        return recipeCommunityTags.stream()
                .filter(userCois::contains)
                .collect(Collectors.toSet());
    }
}
