package com.recipe.Recipe.controller;


import com.recipe.Recipe.model.RecipeEntity;

import com.recipe.Recipe.service.RecipeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
public class RecipeController{

    RecipeService recipeService;

    @Autowired
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping ("getAllRecipes")
    public ResponseEntity<List<RecipeEntity>> getAllRecipes(){

        return recipeService.getAllRecipes();
    }

    @GetMapping("/getRecipeById/{id}")
    public ResponseEntity<RecipeEntity> getRecipeById(@PathVariable("id") Long id){
        

        return recipeService.getRecipeById(id);
    }

    @PostMapping("/addRecipe")
    public ResponseEntity<RecipeEntity> addRecipe(@RequestBody RecipeEntity recipe){
    
        return recipeService.addRecipe(recipe);
    }

    @PutMapping("/updateRecipeById/{id}")
    public ResponseEntity<RecipeEntity> updateRecipeById(@PathVariable("id") long id, @RequestBody RecipeEntity newRecipeData){
        
        return recipeService.updateRecipeById(id, newRecipeData);
    }

    @DeleteMapping("/deleteRecipeById/{id}")
    public ResponseEntity<HttpStatus> deleteRecipeById(@PathVariable("id") long id){
    
        return recipeService.deleteRecipeById(id);
    }

}