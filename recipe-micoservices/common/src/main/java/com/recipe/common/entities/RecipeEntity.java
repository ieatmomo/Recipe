package com.recipe.common.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="Recipes")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class RecipeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String author;
    private String ownerEmail;
    private String description;
    private String ingredients;
    private String region;
    private String category;

    // ABAC: Indicates if this recipe has access restrictions
    @Column(name = "is_restricted")
    private Boolean isRestricted = false;

    // ABAC: Access Control Groups required to view this recipe
    // If empty and isRestricted=false, recipe is public
    // If populated and isRestricted=true, only users with matching ACG can access
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_acg", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "acg")
    private Set<String> accessControlGroups = new HashSet<>();

    // COI: Communities of Interest tags for notification purposes
    // When recipe is created with COI tags, users subscribed to those COIs get notified
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_coi", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "coi")
    private Set<String> communityTags = new HashSet<>();
}
