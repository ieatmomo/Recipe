package com.recipe.Recipe.controller;

import com.recipe.Recipe.model.RecipeSearchEntity;
import com.recipe.Recipe.service.ElasticService;
import com.recipe.Recipe.service.sec.UserInfoService;
import com.recipe.Recipe.service.KafkaEventService;
import com.recipe.Recipe.service.RecipeService;
import com.recipe.Recipe.repo.es.ElasticRepo;
import com.recipe.Recipe.repo.es.MealRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ElasticControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ElasticService elasticService;

    @MockBean
    private UserInfoService userInfoService;

    @MockBean
    private KafkaEventService kafkaEventService;

    @MockBean
    private RecipeService recipeService;

    @MockBean
    private ElasticRepo elasticRepo;

    @MockBean
    private MealRepo mealRepo;

    // Test 1: Search with admin user
    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void testSearch_AdminUser_ReturnsAllResults() throws Exception {
        // Arrange
        RecipeSearchEntity recipe = new RecipeSearchEntity();
        recipe.setId("1");
        recipe.setName("Chicken Curry");
        recipe.setRegion("ASIA");
        when(elasticService.search("chicken")).thenReturn(Arrays.asList(recipe));

        // Act & Assert
        mockMvc.perform(get("/search").param("query", "chicken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Chicken Curry"))
                .andExpect(jsonPath("$[0].region").value("ASIA"));
    }

    // Test 2: Search with regular user (region-filtered)
    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void testSearch_RegularUser_ReturnsRegionFiltered() throws Exception {
        // Arrange
        RecipeSearchEntity recipe = new RecipeSearchEntity();
        recipe.setId("2");
        recipe.setName("Pasta");
        recipe.setRegion("EU");
        when(userInfoService.getRegionByEmail("user@test.com")).thenReturn("EU");
        when(elasticService.searchByNameAndRegion("pasta", "EU")).thenReturn(Arrays.asList(recipe));

        // Act & Assert
        mockMvc.perform(get("/search").param("query", "pasta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Pasta"))
                .andExpect(jsonPath("$[0].region").value("EU"));
    }

    // Test 3: Search with user lacking region
    @Test
    @WithMockUser(username = "noregion@test.com", roles = {"USER"})
    void testSearch_UserWithoutRegion_ReturnsEmpty() throws Exception {
        // Arrange
        when(userInfoService.getRegionByEmail("noregion@test.com")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/search").param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}