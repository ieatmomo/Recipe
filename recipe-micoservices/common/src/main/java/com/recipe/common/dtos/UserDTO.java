package com.recipe.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private int id;
    private String name;
    private String email;
    private String roles;
    private String region;
    private Set<String> accessControlGroups;
    private Set<String> communitiesOfInterest;
}
