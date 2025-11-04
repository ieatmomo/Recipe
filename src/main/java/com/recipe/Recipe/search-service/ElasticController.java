package com.recipe.Recipe.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.recipe.Recipe.dto.MealSummary;
import com.recipe.Recipe.model.RecipeSearchEntity;
import com.recipe.Recipe.service.ElasticService;
import com.recipe.Recipe.service.sec.UserInfoService;

@RestController
public class ElasticController {
    
    @Autowired
    ElasticService elasticService;

    @Autowired
    UserInfoService userInfoService;

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
        String region = userInfoService.getRegionByEmail(authentication.getName());
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
}
