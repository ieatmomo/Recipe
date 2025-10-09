package com.recipe.Recipe.controller;

import java.util.List;

import org.apache.catalina.connector.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.recipe.Recipe.model.RecipeEntity;
import com.recipe.Recipe.model.RecipeSearchEntity;
import com.recipe.Recipe.service.ElasticService;

@RestController
public class ElasticController {
    
    @Autowired
    ElasticService elasticService;

    public ElasticController(ElasticService elasticService){
        this.elasticService = elasticService;
    }

    @GetMapping("search")
    public List<RecipeSearchEntity> search(@RequestParam String query){
        return elasticService.search(query);
    }

    @PostMapping("save")
    public RecipeSearchEntity save(@RequestBody RecipeSearchEntity recipe){
        return elasticService.save(recipe);
    }
}
