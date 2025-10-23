package com.recipe.Recipe.controller;

import com.recipe.Recipe.model.RecipeEntity;
import com.recipe.Recipe.service.KafkaEventService;
import com.recipe.Recipe.service.RecipeService;
import com.recipe.Recipe.service.sec.UserInfoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RecipeController{

    RecipeService recipeService;
    KafkaEventService kafkaEventService;
    UserInfoService userInfoService;

    @Autowired
    public RecipeController(RecipeService recipeService, KafkaEventService kafkaEventService, UserInfoService userInfoService) {
        this.recipeService = recipeService;
        this.kafkaEventService = kafkaEventService;
        this.userInfoService = userInfoService;
    }

    @GetMapping ("getAllRecipes")
    public ResponseEntity<List<RecipeEntity>> getAllRecipes(Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return recipeService.getAllRecipes();
        }
        String region = userInfoService.getRegionByEmail(authentication.getName());
        if (region == null || region.isBlank()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return recipeService.getAllRecipesByRegion(region);
    }

    @GetMapping("/getRecipeById/{id}")
    public ResponseEntity<RecipeEntity> getRecipeById(@PathVariable("id") Long id){
        return recipeService.getRecipeById(id);
    }

    @PostMapping("/addRecipe")
    public ResponseEntity<String> addRecipe(@RequestBody RecipeEntity recipe, Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        // set display name and stable owner email
        String email = authentication.getName();
        String displayName = userInfoService.getUsernameByEmail(email);
        recipe.setAuthor(displayName != null ? displayName : email);
        recipe.setOwnerEmail(email);

        kafkaEventService.publishRecipeCreatedEvent(recipe);
        return ResponseEntity.ok("Recipe creation event published!");
    }

    @PutMapping("/updateRecipeById/{id}")
    public ResponseEntity<String> updateRecipeById(@PathVariable("id") long id,
                                                   @RequestBody RecipeEntity newRecipeData,
                                                   Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        ResponseEntity<RecipeEntity> existingResp = recipeService.getRecipeById(id);
        RecipeEntity existing = existingResp.getBody();
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recipe not found");
        }

        boolean isOwner;
        if (existing.getOwnerEmail() != null && !existing.getOwnerEmail().isBlank()) {
            isOwner = authentication.getName().equalsIgnoreCase(existing.getOwnerEmail());
        } else {
            // legacy fallback: compare display name to existing author
            String currentName = userInfoService.getUsernameByEmail(authentication.getName());
            isOwner = currentName != null && currentName.equalsIgnoreCase(existing.getAuthor());
        }

        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }

        // preserve ownership fields
        newRecipeData.setId(id);
        newRecipeData.setAuthor(existing.getAuthor());
        newRecipeData.setOwnerEmail(existing.getOwnerEmail());

        kafkaEventService.publishRecipeUpdatedEvent(newRecipeData);
        return ResponseEntity.ok("Recipe Update event published!");
    }

    @DeleteMapping("/deleteRecipeById/{id}")
    public ResponseEntity<String> deleteRecipeById(@PathVariable("id") long id, Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        ResponseEntity<RecipeEntity> existingResp = recipeService.getRecipeById(id);
        RecipeEntity existing = existingResp.getBody();
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recipe not found");
        }

        boolean isOwner;
        if (existing.getOwnerEmail() != null && !existing.getOwnerEmail().isBlank()) {
            isOwner = authentication.getName().equalsIgnoreCase(existing.getOwnerEmail());
        } else {
            String currentName = userInfoService.getUsernameByEmail(authentication.getName());
            isOwner = currentName != null && currentName.equalsIgnoreCase(existing.getAuthor());
        }

        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }

        kafkaEventService.publishRecipeDeletedEvent(id);
        return ResponseEntity.ok("Recipe Deletion event published!");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/getStats")
    public ResponseEntity<String> getStats(){
        return recipeService.getStats();
    }
}