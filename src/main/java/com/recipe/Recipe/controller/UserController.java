package com.recipe.Recipe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recipe.Recipe.model.AuthRequest;
import com.recipe.Recipe.model.UserInfo;
import com.recipe.Recipe.service.sec.JwtService;
import com.recipe.Recipe.service.sec.UserInfoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserInfoService service;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome this endpoint is not secure";
    }

    @PostMapping("/addNewUser")
    public String addNewUser(@RequestBody UserInfo userInfo) {
        return service.addUser(userInfo);
    }

    @PostMapping("/generateToken")
    public String authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );
        if (authentication.isAuthenticated()) {
            String username = authRequest.getUsername();
            String roles = authentication.getAuthorities().stream()
                    .map(grantedAuthority -> grantedAuthority.getAuthority())
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
            String region = service.getRegionByEmail(username);
            return jwtService.generateToken(username, roles, region);
        } else {
            throw new UsernameNotFoundException("Invalid user request!");
        }
    }

    @GetMapping("/getUserByEmail/{email}")
    public String getUserByEmail(@PathVariable("email") String email) {
        String name = service.getUsernameByEmail(email);
        return name != null ? name : "User not found";
    }

    @GetMapping("/region")
    public String getMyRegion(Authentication auth) {
        String email = auth.getName();
        String region = service.getRegionByEmail(email);
        return region != null ? region : "unknown";
    }

}