package com.recipe.common.entities;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="user_info")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;

    @Column(unique = true, nullable = false)
    private String email;
    private String password;
    private String roles;
    private String region;

    // ABAC: Access Control Groups - Groups user belongs to for accessing restricted recipes
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_acg", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "acg")
    private Set<String> accessControlGroups = new HashSet<>();

    // COI: Communities of Interest - Topics user is interested in for notifications
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_coi", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "coi")
    private Set<String> communitiesOfInterest = new HashSet<>();

    public String getRoles(){
        return roles;
    }
}
