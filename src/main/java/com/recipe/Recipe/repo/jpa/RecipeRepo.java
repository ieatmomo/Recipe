package com.recipe.Recipe.repo.jpa;

import com.recipe.Recipe.model.RecipeEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepo extends JpaRepository<RecipeEntity, Long>{
    
}