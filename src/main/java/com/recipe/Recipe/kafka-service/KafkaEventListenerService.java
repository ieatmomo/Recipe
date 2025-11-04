package com.recipe.Recipe.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.recipe.Recipe.model.RecipeEntity;

@Service
public class KafkaEventListenerService {

    @Autowired
    private RecipeService recipeService;

    @KafkaListener(topics = "recipe-created", groupId = "recipe-group")
    public void handleRecipeCreated(RecipeEntity recipe) {
        recipeService.addRecipe(recipe);
    }

    @KafkaListener(topics = "recipe-updated", groupId = "recipe-group")
    public void handleRecipeUpdated(RecipeEntity recipe) {
        recipeService.updateRecipeById(recipe.getId(), recipe);
    }

    @KafkaListener(topics = "recipe-deleted", groupId = "recipe-group")
    public void handleRecipeDeleted(Long id) {
        recipeService.deleteRecipeById(id);
    }
}
