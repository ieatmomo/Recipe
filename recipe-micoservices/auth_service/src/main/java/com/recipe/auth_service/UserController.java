package com.recipe.auth_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import java.util.List;
import java.util.Set;

import com.recipe.auth_service.AuthRequest;
import com.recipe.auth_service.UserInfo;
import com.recipe.auth_service.JwtService;
import com.recipe.auth_service.UserInfoService;
import com.recipe.common.dtos.CoiSubscriptionRequest;

import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.Set;

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
            Set<String> acgs = service.getUserAcgs(username);
            return jwtService.generateToken(username, roles, region, acgs);
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
        // Check if authentication is null
        if (auth == null || auth.getName() == null) {
            return "unknown";
        }
        String email = auth.getName();
        String region = service.getRegionByEmail(email);
        return region != null ? region : "unknown";
    }

    // New endpoints for inter-service communication
    @GetMapping("/region/{email}")
    public String getRegionByEmail(@PathVariable("email") String email) {
        String region = service.getRegionByEmail(email);
        return region != null ? region : "";
    }

    @GetMapping("/username/{email}")
    public String getUsernameByEmail(@PathVariable("email") String email) {
        String name = service.getUsernameByEmail(email);
        return name != null ? name : "";
    }

    // New ABAC endpoints for ACG and COI
    @GetMapping("/user/{email}/acg")
    public ResponseEntity<Set<String>> getUserAcgs(@PathVariable("email") String email) {
        Set<String> acgs = service.getUserAcgs(email);
        return ResponseEntity.ok(acgs);
    }

    @GetMapping("/user/{email}/coi")
    public ResponseEntity<Set<String>> getUserCois(@PathVariable("email") String email) {
        Set<String> cois = service.getUserCois(email);
        return ResponseEntity.ok(cois);
    }
    
    /**
     * Get all user emails that have a specific COI
     * Public endpoint for notification system
     */
    @GetMapping("/users/coi/{coi}")
    public ResponseEntity<List<String>> getUsersWithCoi(@PathVariable("coi") String coi) {
        List<String> userEmails = service.getUserEmailsWithCoi(coi);
        return ResponseEntity.ok(userEmails);
    }

    // User self-service endpoint to manage their own COI subscriptions
    @PostMapping("/my/coi")
    public ResponseEntity<String> manageOwnCoi(
            @RequestBody CoiSubscriptionRequest request,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String email = authentication.getName();
        Optional<UserInfo> userOpt = service.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        UserInfo user = userOpt.get();
        Set<String> currentCois = user.getCommunitiesOfInterest();

        if ("ADD".equalsIgnoreCase(request.getAction())) {
            currentCois.addAll(request.getCommunitiesOfInterest());
        } else if ("REMOVE".equalsIgnoreCase(request.getAction())) {
            currentCois.removeAll(request.getCommunitiesOfInterest());
        } else {
            return ResponseEntity.badRequest().body("Invalid action. Use 'ADD' or 'REMOVE'");
        }

        user.setCommunitiesOfInterest(currentCois);
        service.saveUser(user);

        return ResponseEntity.ok("COI subscriptions updated. You are now subscribed to: " + currentCois);
    }
}
