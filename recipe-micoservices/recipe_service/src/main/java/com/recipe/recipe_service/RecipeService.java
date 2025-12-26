package com.recipe.recipe_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.recipe.common.entities.RecipeEntity;
import com.recipe.recipe_service.RecipeRepo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {
    private final RecipeRepo recipeRepo;

    @Autowired
    public RecipeService(RecipeRepo recipeRepo){
        this.recipeRepo = recipeRepo;
    }

    public ResponseEntity<List<RecipeEntity>> getAllRecipes(){
        try{
            List<RecipeEntity> recipeList = new ArrayList<>();
            recipeRepo.findAll().forEach(recipeList::add);
            // Always return 200 OK with the list (even if empty) so frontend can parse JSON
            return new ResponseEntity<>(recipeList, HttpStatus.OK);
        } catch(Exception ex){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<List<RecipeEntity>> getAllRecipesByRegion(String region){
        try{
            List<RecipeEntity> recipeList = recipeRepo.findByRegionIgnoreCase(region);
            // Always return 200 OK with the list (even if empty) so frontend can parse JSON
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
        // Elasticsearch indexing will be handled by Kafka consumer
        return new ResponseEntity<>(recipeObj, HttpStatus.OK);
    }

    public ResponseEntity<RecipeEntity> updateRecipeById(@PathVariable("id") Long id, @RequestBody RecipeEntity newRecipeData){
        Optional<RecipeEntity> oldRecipeData = recipeRepo.findById(id);
        if (oldRecipeData.isPresent()){
            RecipeEntity updatedRecipeData = oldRecipeData.get();
            updatedRecipeData.setName(newRecipeData.getName());
            updatedRecipeData.setDescription(newRecipeData.getDescription());
            updatedRecipeData.setIngredients(newRecipeData.getIngredients());
            updatedRecipeData.setCategory(newRecipeData.getCategory());

            RecipeEntity recipeObj = recipeRepo.save(updatedRecipeData);
            // Elasticsearch indexing will be handled by Kafka consumer
            return new ResponseEntity<>(recipeObj, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<String> deleteRecipeById(Long id) {
        try {
            if (!recipeRepo.existsById(id)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            recipeRepo.deleteById(id);
            // Elasticsearch deletion will be handled by Kafka consumer
            return ResponseEntity.ok("Recipe deleted");
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<String> getStats(){
        ResponseEntity<List<RecipeEntity>> all = getAllRecipes();
        int recipeCount = all.getBody() != null ? all.getBody().size() : 0;
        String stat = "Recipe Count: " + recipeCount;
        return ResponseEntity.ok(stat);
    }
}
