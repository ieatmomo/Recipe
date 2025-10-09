package com.recipe.Recipe.model;

import java.util.List;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import jakarta.persistence.Id;

@Document(indexName = "recipes")
public class RecipeSearchEntity {
    @Id
    private Long id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private String ingredients;


    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }
    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public String getIngreidents(){
        return ingredients;
    }

    public void setIngredients(String ingredients){
        this.ingredients = ingredients;
    }

}
