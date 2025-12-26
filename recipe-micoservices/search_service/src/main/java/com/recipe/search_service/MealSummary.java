package com.recipe.search_service;

public class MealSummary {
    private String mealId;     // from ES: meal_id
    private String mealName;   // from ES: meal_name
    private String category;   // from ES: category

    public MealSummary() {}
    public MealSummary(String mealId, String mealName, String category) {
        this.mealId = mealId;
        this.mealName = mealName;
        this.category = category;
    }

    public String getMealId() { return mealId; }
    public void setMealId(String mealId) { this.mealId = mealId; }
    public String getMealName() { return mealName; }
    public void setMealName(String mealName) { this.mealName = mealName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}