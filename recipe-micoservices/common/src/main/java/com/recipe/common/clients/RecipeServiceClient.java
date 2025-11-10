package com.recipe.common.clients;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import com.recipe.common.dtos.RecipeDTO;
import com.recipe.common.entities.RecipeEntity;

import java.util.List;

@Component
public class RecipeServiceClient {
    
    private final RestTemplate restTemplate;
    private final String recipeServiceUrl;

    public RecipeServiceClient(RestTemplate restTemplate,
                             @Value("${recipe.service.url:http://localhost:8082}") String recipeServiceUrl) {
        this.restTemplate = restTemplate;
        this.recipeServiceUrl = recipeServiceUrl;
    }

    public RecipeEntity getRecipeById(Long id) {
        try {
            ResponseEntity<RecipeEntity> response = restTemplate.getForEntity(
                recipeServiceUrl + "/recipes/" + id,
                RecipeEntity.class
            );
            return response.getBody();
        } catch (Exception e) {
            System.err.println("RecipeServiceClient.getRecipeById error: " + e.getMessage());
            return null;
        }
    }

    public void addRecipe(RecipeEntity recipe) {
        try {
            restTemplate.postForObject(
                recipeServiceUrl + "/recipes",
                recipe,
                RecipeEntity.class
            );
        } catch (Exception e) {
            System.err.println("RecipeServiceClient.addRecipe error: " + e.getMessage());
        }
    }

    public void updateRecipe(Long id, RecipeEntity recipe) {
        try {
            restTemplate.put(
                recipeServiceUrl + "/recipes/" + id,
                recipe
            );
        } catch (Exception e) {
            System.err.println("RecipeServiceClient.updateRecipe error: " + e.getMessage());
        }
    }

    public void deleteRecipe(Long id) {
        try {
            restTemplate.delete(recipeServiceUrl + "/recipes/" + id);
        } catch (Exception e) {
            System.err.println("RecipeServiceClient.deleteRecipe error: " + e.getMessage());
        }
    }
    
    public void updateRecipeById(Long id, RecipeEntity recipe) {
        updateRecipe(id, recipe);
    }
    
    public void deleteRecipeById(Long id) {
        deleteRecipe(id);
    }
}
