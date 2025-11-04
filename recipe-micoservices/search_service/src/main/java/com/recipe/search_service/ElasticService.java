package com.recipe.Recipe.search_service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.recipe.Recipe.search_service.MealSummary;
import com.recipe.Recipe.search_service.MealSearchEntity;
import com.recipe.Recipe.search_service.RecipeSearchEntity;
import com.recipe.Recipe.search_service.ElasticRepo;
import com.recipe.Recipe.search_service.MealRepo;

@Service
public class ElasticService {
    
    @Autowired
    ElasticRepo elasticRepo;
    
    @Autowired
    MealRepo mealRepo;

    public ElasticService(ElasticRepo elasticRepo, MealRepo mealRepo){
        this.elasticRepo = elasticRepo;
        this.mealRepo = mealRepo;
    }

    // User recipe search
    public List<RecipeSearchEntity> search(String query){ return elasticRepo.findByName(query); }
    public List<RecipeSearchEntity> searchByRegion(String region){ return elasticRepo.findByRegion(region); }
    public List<RecipeSearchEntity> searchByNameAndRegion(String query, String region){ return elasticRepo.findByNameAndRegion(query, region); }

    // User recipe CRUD
    public RecipeSearchEntity save(RecipeSearchEntity recipe){ return elasticRepo.save(recipe); }
    public void deleteById(Long id) { elasticRepo.deleteById(String.valueOf(id)); }

    // External meal dataset queries
    public List<MealSummary> findMealSummariesByCategory(String category){
        List<MealSearchEntity> meals = mealRepo.findByCategory(category);
        return toMealSummaries(meals);
    }

    public List<MealSummary> randomMealSummariesByCategory(String category, int limit){
        List<MealSearchEntity> meals = mealRepo.findByCategory(category);
        List<MealSearchEntity> random = pickRandom(meals, limit);
        return toMealSummaries(random);
    }

    private List<MealSummary> toMealSummaries(List<MealSearchEntity> meals){
        if (meals == null || meals.isEmpty()) return Collections.emptyList();
        List<MealSummary> out = new ArrayList<>();
        for (MealSearchEntity m : meals) {
            if (m.getMealId() != null && m.getMealName() != null && m.getCategory() != null) {
                out.add(new MealSummary(m.getMealId(), m.getMealName(), m.getCategory()));
            }
        }
        return out;
    }

    private <T> List<T> pickRandom(List<T> source, int limit){
        if (source == null || source.isEmpty()) return Collections.emptyList();
        if (source.size() <= limit) return source;
        ArrayList<T> reservoir = new ArrayList<>(limit);
        int i = 0;
        for (; i < limit; i++) reservoir.add(source.get(i));
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (; i < source.size(); i++) {
            int j = rnd.nextInt(i + 1);
            if (j < limit) reservoir.set(j, source.get(i));
        }
        return reservoir;
    }
}
