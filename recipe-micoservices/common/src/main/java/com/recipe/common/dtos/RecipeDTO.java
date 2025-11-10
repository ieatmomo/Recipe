package com.recipe.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeDTO {
    private Long id;
    private String name;
    private String author;
    private String ownerEmail;
    private String description;
    private String ingredients;
    private String region;
    private String category;
}
