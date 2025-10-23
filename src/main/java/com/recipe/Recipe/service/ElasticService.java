package com.recipe.Recipe.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.recipe.Recipe.dto.MealSummary;
import com.recipe.Recipe.model.RecipeSearchEntity;
import com.recipe.Recipe.repo.es.ElasticRepo;

@Service
public class ElasticService {
    
    @Autowired
    ElasticRepo elasticRepo;

    public ElasticService(ElasticRepo elasticRepo){
        this.elasticRepo = elasticRepo;
    }

    public List<RecipeSearchEntity> search(String query){ return elasticRepo.findByName(query); }
    public List<RecipeSearchEntity> searchByRegion(String region){ return elasticRepo.findByRegion(region); }
    public List<RecipeSearchEntity> searchByNameAndRegion(String query, String region){ return elasticRepo.findByNameAndRegion(query, region); }
    public List<RecipeSearchEntity> findByCategory(String category){ return elasticRepo.findByCategory(category); }
    public List<RecipeSearchEntity> findByCategoryAndRegion(String category, String region){ return elasticRepo.findByCategoryAndRegion(category, region); }
    public List<RecipeSearchEntity> randomByCategory(String category, int limit){ return pickRandom(elasticRepo.findByCategory(category), limit); }
    public List<RecipeSearchEntity> randomByCategoryAndRegion(String category, String region, int limit){ return pickRandom(elasticRepo.findByCategoryAndRegion(category, region), limit); }

    public RecipeSearchEntity save(RecipeSearchEntity recipe){ return elasticRepo.save(recipe); }

    public void deleteById(Long id) { elasticRepo.deleteById(String.valueOf(id)); }

    public List<MealSummary> findMealSummariesByCategory(String category){
        List<RecipeSearchEntity> docs = elasticRepo.findByCategory(category);
        return toMealSummaries(docs);
    }

    public List<MealSummary> randomMealSummariesByCategory(String category, int limit){
        List<RecipeSearchEntity> docs = elasticRepo.findByCategory(category);
        List<RecipeSearchEntity> random = pickRandom(docs, limit);
        return toMealSummaries(random);
    }

    private List<MealSummary> toMealSummaries(List<RecipeSearchEntity> docs){
        if (docs == null || docs.isEmpty()) return Collections.emptyList();
        List<MealSummary> out = new ArrayList<>();
        for (RecipeSearchEntity d : docs) {
            // only include items that have the external fields we need
            if (d.getMealId() != null && d.getMealName() != null && d.getCategory() != null) {
                out.add(new MealSummary(d.getMealId(), d.getMealName(), d.getCategory()));
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
