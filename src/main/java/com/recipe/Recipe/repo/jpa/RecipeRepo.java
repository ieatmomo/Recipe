package com.recipe.Recipe.repo.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.recipe.Recipe.model.RecipeEntity;

@Repository
public interface RecipeRepo extends JpaRepository<RecipeEntity, Long>{
    List<RecipeEntity> findByRegionIgnoreCase(String region);
}