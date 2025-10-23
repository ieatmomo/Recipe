package com.recipe.Recipe.repo.es;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.recipe.Recipe.model.RecipeSearchEntity;

@Repository
public interface ElasticRepo extends ElasticsearchRepository<RecipeSearchEntity, String> {
    List<RecipeSearchEntity> findByName(String name);
    List<RecipeSearchEntity> findByRegion(String region);
    List<RecipeSearchEntity> findByNameAndRegion(String name, String region);
    List<RecipeSearchEntity> findByCategory(String category);
    List<RecipeSearchEntity> findByCategoryAndRegion(String category, String region);
}
