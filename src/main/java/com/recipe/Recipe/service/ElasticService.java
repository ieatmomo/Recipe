package com.recipe.Recipe.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.recipe.Recipe.model.RecipeEntity;
import com.recipe.Recipe.model.RecipeSearchEntity;
import com.recipe.Recipe.repo.es.ElasticRepo;


@Service
public class ElasticService {
    
    @Autowired
    ElasticRepo elasticRepo;

    public ElasticService(ElasticRepo elasticRepo){
        this.elasticRepo = elasticRepo;
    }

    public List<RecipeSearchEntity> search(String query){
        return elasticRepo.findByName(query);
    }

    public RecipeSearchEntity save(RecipeSearchEntity recipe){
        return elasticRepo.save(recipe);
    }

    public void deleteById(Long id) {
        elasticRepo.deleteById(id);
    }
}
