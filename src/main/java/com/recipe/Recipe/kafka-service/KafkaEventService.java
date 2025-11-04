package com.recipe.Recipe.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.recipe.Recipe.model.RecipeEntity;

@Service
public class KafkaEventService {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishRecipeCreatedEvent(RecipeEntity recipe) {
        kafkaTemplate.send("recipe-created", recipe);
    }

    public void publishRecipeUpdatedEvent(RecipeEntity recipe) {
        kafkaTemplate.send("recipe-updated", recipe);
    }

    public void publishRecipeDeletedEvent(Long id) {
        kafkaTemplate.send("recipe-deleted", id);
    }
}
