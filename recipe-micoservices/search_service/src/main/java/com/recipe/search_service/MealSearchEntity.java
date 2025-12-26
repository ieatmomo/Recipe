package com.recipe.search_service;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "recipes_system")
public class MealSearchEntity {
    @Id
    private String id;

    @Field(type = FieldType.Text, name = "meal_name")
    private String mealName;

    @Field(type = FieldType.Text, name = "meal_id")
    private String mealId;

    @Field(type = FieldType.Keyword)
    private String category;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMealName() { return mealName; }
    public void setMealName(String mealName) { this.mealName = mealName; }
    public String getMealId() { return mealId; }
    public void setMealId(String mealId) { this.mealId = mealId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}