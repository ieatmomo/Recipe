package com.recipe.Recipe.service.sec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.recipe.Recipe.model.UserInfo;
import com.recipe.Recipe.repo.sec.UserInfoRepository;

import java.util.Optional;
import java.util.Locale;
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
}
