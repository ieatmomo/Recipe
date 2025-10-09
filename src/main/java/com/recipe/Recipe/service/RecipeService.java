package com.recipe.Recipe.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.recipe.Recipe.model.RecipeEntity;
import com.recipe.Recipe.model.RecipeSearchEntity;
import com.recipe.Recipe.repo.es.ElasticRepo;
import com.recipe.Recipe.repo.jpa.RecipeRepo;
import com.recipe.Recipe.service.ElasticService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {
    
    RecipeRepo recipeRepo;
    ElasticService elasticService; 

    @Autowired
    public RecipeService(RecipeRepo recipeRepo, ElasticService elasticService){
        this.recipeRepo = recipeRepo;
        this.elasticService = elasticService;
    }

    public ResponseEntity<List<RecipeEntity>> getAllRecipes(){
        try{
            List<RecipeEntity> recipeList = new ArrayList<>();
            recipeRepo.findAll().forEach(recipeList::add);

            if (recipeList.isEmpty()){
                return new ResponseEntity<>( HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(recipeList, HttpStatus.OK);
        } catch(Exception ex){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<RecipeEntity> getRecipeById(@PathVariable("id") Long id){
        Optional<RecipeEntity> recipeData = recipeRepo.findById(id);

        if (recipeData.isPresent()){
            return new ResponseEntity<>(recipeData.get(), HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<RecipeEntity> addRecipe(@RequestBody RecipeEntity recipe){
        RecipeEntity recipeObj = recipeRepo.save(recipe);
        
        RecipeSearchEntity searchEntity = new RecipeSearchEntity();
        searchEntity.setId(recipeObj.getId());
        searchEntity.setName(recipeObj.getName());
        searchEntity.setDescription(recipeObj.getDescription());
        searchEntity.setIngredients(recipeObj.getIngredients());
        
        elasticService.save(searchEntity);
        
        return new ResponseEntity<>(recipeObj, HttpStatus.OK);
    }

    public ResponseEntity<RecipeEntity> updateRecipeById(@PathVariable("id") Long id, @RequestBody RecipeEntity newRecipeData){
        Optional<RecipeEntity> oldRecipeData = recipeRepo.findById(id);

        if (oldRecipeData.isPresent()){
            RecipeEntity updatedRecipeData = oldRecipeData.get();
            updatedRecipeData.setName(newRecipeData.getName());
            updatedRecipeData.setAuthor(newRecipeData.getAuthor());
            updatedRecipeData.setDescription(newRecipeData.getDescription());
            updatedRecipeData.setIngredients(newRecipeData.getIngredients());


            RecipeEntity recipeObj = recipeRepo.save(updatedRecipeData);
            
            RecipeSearchEntity searchEntity = new RecipeSearchEntity();
            searchEntity.setId(recipeObj.getId());
            searchEntity.setName(recipeObj.getName());
            searchEntity.setDescription(recipeObj.getDescription());
            searchEntity.setIngredients(recipeObj.getIngredients());
            elasticService.save(searchEntity);
            
            return new ResponseEntity<>(recipeObj, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<HttpStatus> deleteRecipeById(@PathVariable("id") Long id){
        recipeRepo.deleteById(id);
        elasticService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostConstruct
    public void syncDataToElasticsearch() {
        List<RecipeEntity> allRecipes = recipeRepo.findAll();
        for (RecipeEntity recipe : allRecipes) {
            RecipeSearchEntity searchEntity = new RecipeSearchEntity();
            searchEntity.setId(recipe.getId());
            searchEntity.setName(recipe.getName());
            searchEntity.setDescription(recipe.getDescription());
            searchEntity.setIngredients(recipe.getIngredients());
            elasticService.save(searchEntity);
        }
    }
}
