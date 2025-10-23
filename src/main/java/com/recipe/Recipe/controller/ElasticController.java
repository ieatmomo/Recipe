package com.recipe.Recipe.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.recipe.Recipe.dto.MealSummary;
import com.recipe.Recipe.service.ElasticService;

@RestController
public class ElasticController {
    
    @Autowired
    ElasticService elasticService;

    public ElasticController(ElasticService elasticService){
        this.elasticService = elasticService;
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
}
