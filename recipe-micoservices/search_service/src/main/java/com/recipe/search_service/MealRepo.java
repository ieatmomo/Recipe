package com.recipe.Recipe.search_service;

import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import com.recipe.Recipe.search_service.MealSearchEntity;

@Repository
public interface MealRepo extends ElasticsearchRepository<MealSearchEntity, String> {
    List<MealSearchEntity> findByCategory(String category);
}