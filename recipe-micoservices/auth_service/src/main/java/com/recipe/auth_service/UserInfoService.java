package com.recipe.auth_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.recipe.auth_service.UserInfo;
import com.recipe.auth_service.UserInfoRepository;

import java.util.Optional;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

@Service
public class UserInfoService implements UserDetailsService {

    private final UserInfoRepository repository;
    private final PasswordEncoder encoder; // no circular bean here

    @Autowired
    public UserInfoService(UserInfoRepository repository, @Lazy PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    private static final Set<String> ALLOWED_REGIONS = Set.of("ASIA", "EU", "AFRICA");

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByEmail(username)
                .map(UserInfoDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    public String addUser(UserInfo userInfo) {
        // validate region
        String region = userInfo.getRegion();
        if (region == null || region.isBlank()) {
            return "Region is required. Allowed: ASIA, EU, AFRICA";
        }
        String normalized = region.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_REGIONS.contains(normalized)) {
            return "Invalid region. Allowed: ASIA, EU, AFRICA";
        }
        userInfo.setRegion(normalized);

        // save user
        userInfo.setPassword(encoder.encode(userInfo.getPassword()));
        repository.save(userInfo);
        return "User added successfully!";
    }

    public String getUsernameByEmail(String email) {
        return repository.findByEmail(email).map(UserInfo::getName).orElse(null);
    }

    public String getRegionByEmail(String email) {
        return repository.findByEmail(email).map(UserInfo::getRegion).orElse(null);
    }

    public Optional<UserInfo> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public UserInfo saveUser(UserInfo user) {
        return repository.save(user);
    }

    public Set<String> getUserAcgs(String email) {
        return repository.findByEmail(email).map(UserInfo::getAccessControlGroups).orElse(Set.of());
    }

    public Set<String> getUserCois(String email) {
        return repository.findByEmail(email).map(UserInfo::getCommunitiesOfInterest).orElse(Set.of());
    }
    
    /**
     * Get all user emails that have a specific COI
     */
    public List<String> getUserEmailsWithCoi(String coi) {
        List<String> userEmails = new ArrayList<>();
        Iterable<UserInfo> allUsers = repository.findAll();
        
        for (UserInfo user : allUsers) {
            if (user.getCommunitiesOfInterest() != null && 
                user.getCommunitiesOfInterest().contains(coi)) {
                userEmails.add(user.getEmail());
            }
        }
        
        return userEmails;
    }

    public Iterable<UserInfo> findAll() {
        return repository.findAll();
    }
}
