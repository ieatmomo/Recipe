package com.recipe.recipe_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = {
    ElasticsearchDataAutoConfiguration.class,
    ElasticsearchRestClientAutoConfiguration.class,
    UserDetailsServiceAutoConfiguration.class
})
@EntityScan(basePackages = {"com.recipe.common.entities", "com.recipe.recipe_service"})
@EnableJpaRepositories(basePackages = "com.recipe.recipe_service")
@ComponentScan(basePackages = {"com.recipe.recipe_service", "com.recipe.common.services", "com.recipe.common.clients"})
@EnableFeignClients(basePackages = "com.recipe.common.clients")
public class RecipeApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecipeApplication.class, args);
    }
}
