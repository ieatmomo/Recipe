package com.recipe.Recipe.recipe_service;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.recipe.common.entities.RecipeEntity;

@Repository
public interface RecipeRepo extends JpaRepository<RecipeEntity, Long>{
    List<RecipeEntity> findByRegionIgnoreCase(String region);
}