package com.recipe.search_service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.recipe.search_service.MealSummary;
import com.recipe.search_service.RecipeSearchEntity;
import com.recipe.search_service.ElasticService;
import com.recipe.common.clients.AuthServiceClient;

@RestController
public class ElasticController {
    
    @Autowired
    ElasticService elasticService;

    @Autowired
    AuthServiceClient authServiceClient;

    public ElasticController(ElasticService elasticService){
        this.elasticService = elasticService;
    }

    // Search user-created recipes by name (region-filtered for non-admin)
    @GetMapping("search")
    public List<RecipeSearchEntity> search(@RequestParam String query, Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            return Collections.emptyList();
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return elasticService.search(query);
        }
        String region = authServiceClient.getRegionByEmail(authentication.getName());
        if (region == null || region.isBlank()) {
            return Collections.emptyList();
        }
        return elasticService.searchByNameAndRegion(query, region);
    }

    // All recipes in a category (only minimal fields: meal_name, meal_id, category)
    @GetMapping("category/{category}")
    public List<MealSummary> byCategory(@PathVariable String category){
        return elasticService.findMealSummariesByCategory(category);
    }

    // 5 random recipes in a category (only minimal fields)
    @GetMapping("category/{category}/random5")
    public List<MealSummary> randomFiveByCategory(@PathVariable String category){
        return elasticService.randomMealSummariesByCategory(category, 5);
    }

    // Internal endpoints for Kafka consumer (no authentication required)
    @PostMapping("/index")
    public ResponseEntity<RecipeSearchEntity> indexRecipe(@RequestBody RecipeSearchEntity recipe) {
        try {
            RecipeSearchEntity saved = elasticService.save(recipe);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            System.err.println("Error indexing recipe: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/index/{id}")
    public ResponseEntity<String> updateRecipeIndex(@PathVariable Long id, @RequestBody RecipeSearchEntity recipe) {
        try {
            recipe.setId(String.valueOf(id));
            elasticService.save(recipe);
            return ResponseEntity.ok("Recipe updated in index");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating recipe: " + e.getMessage());
        }
    }

    @DeleteMapping("/index/{id}")
    public ResponseEntity<String> deleteFromIndex(@PathVariable Long id) {
        try {
            elasticService.deleteById(id);
            return ResponseEntity.ok("Recipe deleted from index");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting recipe: " + e.getMessage());
        }
    }
}
