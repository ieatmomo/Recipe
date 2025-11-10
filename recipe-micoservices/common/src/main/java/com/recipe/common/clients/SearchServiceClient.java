package com.recipe.common.clients;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import com.recipe.common.entities.RecipeSearchEntity;

import java.util.List;

@Component
public class SearchServiceClient {
    
    private final RestTemplate restTemplate;
    private final String searchServiceUrl;

    public SearchServiceClient(RestTemplate restTemplate,
                             @Value("${search.service.url:http://localhost:8083}") String searchServiceUrl) {
        this.restTemplate = restTemplate;
        this.searchServiceUrl = searchServiceUrl;
    }

    public List<RecipeSearchEntity> searchRecipes(String query) {
        try {
            return restTemplate.exchange(
                searchServiceUrl + "/search?query=" + query,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RecipeSearchEntity>>() {}
            ).getBody();
        } catch (Exception e) {
            System.err.println("SearchServiceClient.searchRecipes error: " + e.getMessage());
            return List.of();
        }
    }

    public void indexRecipe(RecipeSearchEntity recipe) {
        try {
            restTemplate.postForObject(
                searchServiceUrl + "/index",
                recipe,
                RecipeSearchEntity.class
            );
        } catch (Exception e) {
            System.err.println("SearchServiceClient.indexRecipe error: " + e.getMessage());
        }
    }

    public void deleteRecipeFromIndex(Long id) {
        try {
            restTemplate.delete(searchServiceUrl + "/index/" + id);
        } catch (Exception e) {
            System.err.println("SearchServiceClient.deleteRecipeFromIndex error: " + e.getMessage());
        }
    }
    
    public void deleteRecipeFromSearch(Long id) {
        deleteRecipeFromIndex(id);
    }
}
