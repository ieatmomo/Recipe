package com.recipe.Recipe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {
    "com.recipe.Recipe.repo.jpa",
    "com.recipe.Recipe.repo.sec"
})
@EnableElasticsearchRepositories(basePackages = "com.recipe.Recipe.repo.es")
@EntityScan(basePackages = "com.recipe.Recipe.model")
public class RecipeApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecipeApplication.class, args);
    }
}

