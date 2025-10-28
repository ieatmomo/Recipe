package com.recipe.Recipe;

import com.recipe.Recipe.repo.es.ElasticRepo;
import com.recipe.Recipe.repo.es.MealRepo;
import com.recipe.Recipe.service.ElasticService;
import com.recipe.Recipe.service.KafkaEventService;
import com.recipe.Recipe.service.RecipeService;
import com.recipe.Recipe.service.sec.UserInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class RecipeApplicationTests {

    // Mock all beans that depend on external infrastructure
    @MockBean
    private ElasticService elasticService;

    @MockBean
    private ElasticRepo elasticRepo;

    @MockBean
    private MealRepo mealRepo;

    @MockBean
    private KafkaEventService kafkaEventService;

    @MockBean
    private RecipeService recipeService;

    @MockBean
    private UserInfoService userInfoService;

    @Test
    void contextLoads() {
        // Verifies Spring context starts successfully with test config
    }
}
