package com.recipe.search_service;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "user_recipes")
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
}
