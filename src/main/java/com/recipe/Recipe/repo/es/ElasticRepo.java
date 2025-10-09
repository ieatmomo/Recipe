package com.recipe.Recipe.repo.es;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.recipe.Recipe.model.RecipeSearchEntity;

@Repository
public interface ElasticRepo extends ElasticsearchRepository<RecipeSearchEntity, Long> {
    List<RecipeSearchEntity> findByName(String name);

}
