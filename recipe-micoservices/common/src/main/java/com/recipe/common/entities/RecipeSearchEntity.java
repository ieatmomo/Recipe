package com.recipe.common.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.HashSet;
import java.util.Set;

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

    // ABAC: Indicates if recipe is access-restricted
    @Field(type = FieldType.Boolean)
    private Boolean isRestricted = false;

    // ABAC: Access Control Groups for this recipe
    @Field(type = FieldType.Keyword)
    private Set<String> accessControlGroups = new HashSet<>();

    // COI: Community of Interest tags
    @Field(type = FieldType.Keyword)
    private Set<String> communityTags = new HashSet<>();

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
    public Boolean getIsRestricted(){ return isRestricted; }
    public void setIsRestricted(Boolean isRestricted){ this.isRestricted = isRestricted; }
    public Set<String> getAccessControlGroups(){ return accessControlGroups; }
    public void setAccessControlGroups(Set<String> accessControlGroups){ this.accessControlGroups = accessControlGroups; }
    public Set<String> getCommunityTags(){ return communityTags; }
    public void setCommunityTags(Set<String> communityTags){ this.communityTags = communityTags; }
}
