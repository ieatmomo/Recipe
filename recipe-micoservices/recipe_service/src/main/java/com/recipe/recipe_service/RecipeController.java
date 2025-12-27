package com.recipe.recipe_service;

import com.recipe.common.entities.RecipeEntity;
import com.recipe.common.clients.AuthServiceClient;
import com.recipe.common.services.AbacService;
import com.recipe.recipe_service.RecipeService;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class RecipeController{
    
    private static final Logger logger = LoggerFactory.getLogger(RecipeController.class);

    private final RecipeService recipeService;
    private final AuthServiceClient authServiceClient;  // Can be null if Feign not configured
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final JwtService jwtService;
    private final AbacService abacService;
    private final NotificationService notificationService;
    private final RecipeRepo recipeRepo;

    @Autowired
    public RecipeController(RecipeService recipeService, 
                          @Autowired(required = false) AuthServiceClient authServiceClient, 
                          KafkaTemplate<String, Object> kafkaTemplate,
                          JwtService jwtService,
                          AbacService abacService,
                          @Autowired(required = false) NotificationService notificationService,
                          RecipeRepo recipeRepo) {
        this.recipeService = recipeService;
        this.authServiceClient = authServiceClient;
        this.kafkaTemplate = kafkaTemplate;
        this.jwtService = jwtService;
        this.abacService = abacService;
        this.notificationService = notificationService;
        this.recipeRepo = recipeRepo;
    }

    @GetMapping ("getAllRecipes")
    public ResponseEntity<List<RecipeEntity>> getAllRecipes(Authentication authentication, HttpServletRequest request){
        logger.info("RecipeController.getAllRecipes: START");
        logger.info("RecipeController: authentication={}", authentication);
        logger.info("RecipeController: authentication.isAuthenticated()={}", 
                   authentication != null ? authentication.isAuthenticated() : "null");
        
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("RecipeController: Returning UNAUTHORIZED - auth is null or not authenticated");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        
        logger.info("RecipeController: authentication.getName()={}", authentication.getName());
        logger.info("RecipeController: authentication.getAuthorities()={}", 
                   authentication.getAuthorities().stream()
                       .map(a -> a.getAuthority())
                       .collect(Collectors.joining(", ")));
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        logger.info("RecipeController: isAdmin={}", isAdmin);
        
        // Extract username/email and region from token
        String region = null;
        String userEmail = null;
        Set<String> userAcgs = new HashSet<>();
        
        try {
            if (authentication instanceof JwtAuthenticationToken) {
                JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
                Jwt jwt = jwtAuth.getToken();
                
                // Extract region claim
                region = jwt.getClaimAsString("region");
                logger.info("RecipeController: Extracted region from OAuth2 JWT: {}", region);
                
                // Extract email/username for fetching ACGs from auth service
                userEmail = jwt.getClaimAsString("preferred_username");
                if (userEmail == null) {
                    userEmail = jwt.getClaimAsString("email");
                }
                if (userEmail == null) {
                    userEmail = jwt.getSubject();
                }
                logger.info("RecipeController: Extracted userEmail from OAuth2 JWT: {}", userEmail);
            } else {
                // Legacy JWT mode - fallback to JwtService
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    region = jwtService.extractRegion(token);
                    userEmail = jwtService.extractUsername(token);
                    logger.info("RecipeController: Extracted region={}, userEmail={} from legacy JWT", region, userEmail);
                }
            }
            
            // Fetch ACGs from auth service (always up-to-date from database)
            if (userEmail != null && authServiceClient != null) {
                userAcgs = authServiceClient.getUserAcgs(userEmail);
                logger.info("RecipeController: Fetched ACGs from auth service for {}: {}", userEmail, userAcgs);
            } else if (userEmail != null) {
                logger.warn("RecipeController: AuthServiceClient not available, ACG filtering will be limited");
            }
        } catch (Exception e) {
            logger.error("RecipeController: Failed to extract claims from token: {}", e.getMessage());
        }
        
        // Get all recipes first
        ResponseEntity<List<RecipeEntity>> allRecipesResponse;
        if (isAdmin) {
            logger.info("RecipeController: User is ADMIN - getting all recipes");
            allRecipesResponse = recipeService.getAllRecipes();
        } else if (region != null && !region.isBlank()) {
            logger.info("RecipeController: User is NOT admin - filtering by region={}", region);
            allRecipesResponse = recipeService.getAllRecipesByRegion(region);
        } else {
            logger.warn("RecipeController: No region found in token, getting all recipes");
            allRecipesResponse = recipeService.getAllRecipes();
        }
        
        // Apply ABAC filtering
        List<RecipeEntity> recipes = allRecipesResponse.getBody();
        if (recipes != null) {
            List<RecipeEntity> filteredRecipes = abacService.filterRecipesByAccess(recipes, userAcgs, isAdmin);
            logger.info("RecipeController: Filtered {} recipes down to {} after ABAC check", 
                       recipes.size(), filteredRecipes.size());
            return ResponseEntity.ok(filteredRecipes);
        }
        
        return allRecipesResponse;
    }

    @GetMapping("/getRecipeById/{id}")
    public ResponseEntity<RecipeEntity> getRecipeById(@PathVariable("id") Long id, 
                                                      Authentication authentication, 
                                                      HttpServletRequest request){
        ResponseEntity<RecipeEntity> response = recipeService.getRecipeById(id);
        RecipeEntity recipe = response.getBody();
        
        if (recipe == null) {
            return response;
        }
        
        // Check ABAC access
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            Set<String> userAcgs = new HashSet<>();
            try {
                if (authentication instanceof JwtAuthenticationToken) {
                    JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
                    Jwt jwt = jwtAuth.getToken();
                    
                    Object acgClaim = jwt.getClaim("acg");
                    if (acgClaim instanceof List) {
                        userAcgs = new HashSet<>((List<String>) acgClaim);
                    } else if (acgClaim instanceof String) {
                        userAcgs = Set.of(((String) acgClaim).split(","));
                    }
                } else {
                    // Legacy JWT mode
                    String authHeader = request.getHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        String acgClaim = jwtService.extractClaim(token, claims -> claims.get("acgs", String.class));
                        if (acgClaim != null && !acgClaim.isBlank()) {
                            userAcgs = Set.of(acgClaim.split(","));
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to extract ACGs from token: {}", e.getMessage());
            }
            
            if (!abacService.canUserAccessRecipe(recipe, userAcgs, isAdmin)) {
                logger.warn("User {} denied access to recipe {} due to ACG restrictions", 
                           authentication.getName(), id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        return response;
    }

    @PostMapping("/addRecipe")
    public ResponseEntity<String> addRecipe(@RequestBody RecipeEntity recipe, Authentication authentication, HttpServletRequest request){
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        
        // Extract user info from JWT token
        String email = authentication.getName();
        String displayName = email;  // Default to email
        
        // Try to get username from authServiceClient if available
        if (authServiceClient != null) {
            try {
                String username = authServiceClient.getUsernameByEmail(email);
                if (username != null && !username.isEmpty()) {
                    displayName = username;
                }
            } catch (Exception e) {
                logger.warn("RecipeController.addRecipe: Failed to get username from auth-service: {}", e.getMessage());
            }
        }
        
        recipe.setAuthor(displayName);
        recipe.setOwnerEmail(email);
        
        // Note: ACG and COI should be set in the request body by the client
        // isRestricted, accessControlGroups, and communityTags fields are accepted from request
        logger.info("Creating recipe with isRestricted={}, ACGs={}, COI tags={}", 
                   recipe.getIsRestricted(), recipe.getAccessControlGroups(), recipe.getCommunityTags());

        // Save recipe first to get the ID
        RecipeEntity savedRecipe = recipeRepo.save(recipe);
        
        // Create notifications for users with matching COIs
        try {
            if (notificationService != null) {
                notificationService.createNotificationsForRecipe(savedRecipe);
            }
        } catch (Exception e) {
            logger.error("Failed to create notifications for recipe {}: {}", savedRecipe.getName(), e.getMessage());
        }
        
        // Publish to Kafka
        kafkaTemplate.send("recipe-created", savedRecipe);
        return ResponseEntity.ok("Recipe created and notifications sent!");
    }

    // Internal endpoint for Kafka consumer - no authentication required
    @PostMapping("/recipes")
    public ResponseEntity<RecipeEntity> addRecipeInternal(@RequestBody RecipeEntity recipe){
        logger.info("RecipeController.addRecipeInternal: Saving recipe from Kafka consumer: {}", recipe.getName());
        ResponseEntity<RecipeEntity> response = recipeService.addRecipe(recipe);
        
        logger.info("RecipeController.addRecipeInternal: Recipe saved, response status: {}", response.getStatusCode());
        logger.info("RecipeController.addRecipeInternal: notificationService null? {}", notificationService == null);
        
        // Create notifications for users with matching COIs
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            logger.info("RecipeController.addRecipeInternal: Attempting to create notifications for recipe: {}", recipe.getName());
            try {
                if (notificationService != null) {
                    logger.info("RecipeController.addRecipeInternal: Calling notificationService.createNotificationsForRecipe()");
                    notificationService.createNotificationsForRecipe(response.getBody());
                    logger.info("RecipeController.addRecipeInternal: Notifications created successfully");
                } else {
                    logger.warn("RecipeController.addRecipeInternal: NotificationService is null");
                }
            } catch (Exception e) {
                logger.error("Failed to create notifications for recipe {}: {}", recipe.getName(), e.getMessage(), e);
            }
        } else {
            logger.warn("RecipeController.addRecipeInternal: Response not 2xx or body is null. Status: {}, Body: {}", 
                response.getStatusCode(), response.getBody());
        }
        
        return response;
    }

    // Internal endpoint for Kafka consumer - no authentication required
    @PutMapping("/recipes/{id}")
    public ResponseEntity<RecipeEntity> updateRecipeInternal(@PathVariable("id") Long id, @RequestBody RecipeEntity recipe){
        logger.info("RecipeController.updateRecipeInternal: Updating recipe {} from Kafka consumer", id);
        return recipeService.updateRecipeById(id, recipe);
    }

    // Internal endpoint for Kafka consumer - no authentication required
    @DeleteMapping("/recipes/{id}")
    public ResponseEntity<String> deleteRecipeInternal(@PathVariable("id") Long id){
        logger.info("RecipeController.deleteRecipeInternal: Deleting recipe {} from Kafka consumer", id);
        return recipeService.deleteRecipeById(id);
    }

    @PutMapping("/updateRecipeById/{id}")
    public ResponseEntity<String> updateRecipeById(@PathVariable("id") long id,
                                                   @RequestBody RecipeEntity newRecipeData,
                                                   Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        ResponseEntity<RecipeEntity> existingResp = recipeService.getRecipeById(id);
        RecipeEntity existing = existingResp.getBody();
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recipe not found");
        }

        boolean isOwner;
        if (existing.getOwnerEmail() != null && !existing.getOwnerEmail().isBlank()) {
            isOwner = authentication.getName().equalsIgnoreCase(existing.getOwnerEmail());
        } else {
            // legacy fallback: compare display name to existing author
            String currentName = authServiceClient.getUsernameByEmail(authentication.getName());
            isOwner = currentName != null && currentName.equalsIgnoreCase(existing.getAuthor());
        }

        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }

        // preserve ownership fields
        newRecipeData.setId(id);
        newRecipeData.setAuthor(existing.getAuthor());
        newRecipeData.setOwnerEmail(existing.getOwnerEmail());

        kafkaTemplate.send("recipe-updated", newRecipeData);
        return ResponseEntity.ok("Recipe Update event published!");
    }

    @DeleteMapping("/deleteRecipeById/{id}")
    public ResponseEntity<String> deleteRecipeById(@PathVariable("id") long id, Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        ResponseEntity<RecipeEntity> existingResp = recipeService.getRecipeById(id);
        RecipeEntity existing = existingResp.getBody();
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recipe not found");
        }

        boolean isOwner;
        if (existing.getOwnerEmail() != null && !existing.getOwnerEmail().isBlank()) {
            isOwner = authentication.getName().equalsIgnoreCase(existing.getOwnerEmail());
        } else {
            String currentName = authServiceClient.getUsernameByEmail(authentication.getName());
            isOwner = currentName != null && currentName.equalsIgnoreCase(existing.getAuthor());
        }

        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }

        kafkaTemplate.send("recipe-deleted", String.valueOf(id));
        return ResponseEntity.ok("Recipe Deletion event published!");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/getStats")
    public ResponseEntity<String> getStats(){
        return recipeService.getStats();
    }
}