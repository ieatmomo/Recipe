package com.recipe.Recipe.repo.es;

import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import com.recipe.Recipe.model.MealSearchEntity;

@Repository
public interface MealRepo extends ElasticsearchRepository<MealSearchEntity, String> {
    List<MealSearchEntity> findByCategory(String category);
}