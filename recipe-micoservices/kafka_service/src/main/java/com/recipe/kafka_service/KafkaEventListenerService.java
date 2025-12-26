package com.recipe.kafka_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.recipe.common.entities.RecipeEntity;
import com.recipe.common.entities.RecipeSearchEntity;
import com.recipe.common.clients.RecipeServiceClient;
import com.recipe.common.clients.SearchServiceClient;

@Service
public class KafkaEventListenerService {

    @Autowired
    private RecipeServiceClient recipeServiceClient;
    
    @Autowired
    private SearchServiceClient searchServiceClient;

    @KafkaListener(topics = "recipe-created", groupId = "recipe-group")
    public void handleRecipeCreated(RecipeEntity recipe) {
        recipeServiceClient.addRecipe(recipe);
        searchServiceClient.indexRecipe(convertToSearchEntity(recipe));
    }

    @KafkaListener(topics = "recipe-updated", groupId = "recipe-group")
    public void handleRecipeUpdated(RecipeEntity recipe) {
        recipeServiceClient.updateRecipeById(recipe.getId(), recipe);
        searchServiceClient.indexRecipe(convertToSearchEntity(recipe));
    }

    @KafkaListener(topics = "recipe-deleted", groupId = "recipe-group")
    public void handleRecipeDeleted(String id) {
        Long recipeId = Long.parseLong(id);
        recipeServiceClient.deleteRecipeById(recipeId);
        searchServiceClient.deleteRecipeFromSearch(recipeId);
    }

    private RecipeSearchEntity convertToSearchEntity(RecipeEntity recipe) {
        RecipeSearchEntity searchEntity = new RecipeSearchEntity();
        searchEntity.setId(String.valueOf(recipe.getId()));
        searchEntity.setName(recipe.getName());
        searchEntity.setDescription(recipe.getDescription());
        searchEntity.setIngredients(recipe.getIngredients());
        searchEntity.setRegion(recipe.getRegion());
        searchEntity.setCategory(recipe.getCategory());
        searchEntity.setAuthor(recipe.getAuthor());
        return searchEntity;
    }
}

