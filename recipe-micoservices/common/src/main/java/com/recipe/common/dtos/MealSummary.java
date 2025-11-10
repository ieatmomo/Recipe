package com.recipe.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MealSummary {
    private String mealId;
    private String mealName;
    private String category;
}
