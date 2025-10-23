package com.recipe.Recipe.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "recipes")
public class RecipeSearchEntity {
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Text)
    private String author;

    @Field(type = FieldType.Text)
    private String ingredients;

    @Field(type = FieldType.Keyword)
    private String region;

    @Field(type = FieldType.Keyword)
    private String category;

    // External dataset fields
    @Field(type = FieldType.Text, name = "meal_name")
    private String mealName;

    @Field(type = FieldType.Text, name = "meal_id")
    private String mealId;

    // Optional: external area/instructions exist, but not needed here

    public String getId(){ return id; }
    public void setId(String id){ this.id = id; }
    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }
    public String getDescription(){ return description; }
    public void setDescription(String description){ this.description = description; }
    public String getAuthor(){ return author; }
    public void setAuthor(String author){ this.author = author; }
    public String getIngredients(){ return ingredients; }
    public void setIngredients(String ingredients){ this.ingredients = ingredients; }
    public String getRegion(){ return region; }
    public void setRegion(String region){ this.region = region; }
    public String getCategory(){ return category; }
    public void setCategory(String category){ this.category = category; }
    public String getMealName() { return mealName; }
    public void setMealName(String mealName) { this.mealName = mealName; }
    public String getMealId() { return mealId; }
    public void setMealId(String mealId) { this.mealId = mealId; }
}
