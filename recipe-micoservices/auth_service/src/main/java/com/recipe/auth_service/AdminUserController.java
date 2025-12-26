package com.recipe.auth_service;

import com.recipe.common.dtos.AcgAssignmentRequest;
import com.recipe.common.dtos.CoiSubscriptionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;

/**
 * Admin endpoints for managing Access Control Groups and Communities of Interest
 * ADMIN-only access required
 */
@RestController
@RequestMapping("/auth/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserInfoService userInfoService;

    /**
     * Assign or remove ACGs to/from a user
     * Only ADMINs can manage user ACG memberships
     */
    @PostMapping("/user/{email}/acg")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> manageUserAcg(
            @PathVariable String email,
            @RequestBody AcgAssignmentRequest request) {
        
        Optional<UserInfo> userOpt = userInfoService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        UserInfo user = userOpt.get();
        Set<String> currentAcgs = user.getAccessControlGroups();

        if ("ADD".equalsIgnoreCase(request.getAction())) {
            currentAcgs.addAll(request.getAccessControlGroups());
        } else if ("REMOVE".equalsIgnoreCase(request.getAction())) {
            currentAcgs.removeAll(request.getAccessControlGroups());
        } else {
            return ResponseEntity.badRequest().body("Invalid action. Use 'ADD' or 'REMOVE'");
        }

        user.setAccessControlGroups(currentAcgs);
        userInfoService.saveUser(user);

        return ResponseEntity.ok("User ACGs updated successfully. User now has ACGs: " + currentAcgs);
    }

    /**
     * Subscribe or unsubscribe user to/from COIs
     * Users can manage their own COI subscriptions via a different endpoint
     * This is for ADMIN bulk management
     */
    @PostMapping("/user/{email}/coi")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> manageUserCoi(
            @PathVariable String email,
            @RequestBody CoiSubscriptionRequest request) {
        
        Optional<UserInfo> userOpt = userInfoService.findByEmail(email);
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
        userInfoService.saveUser(user);

        return ResponseEntity.ok("User COIs updated successfully. User now subscribed to: " + currentCois);
    }

    /**
     * Get user's ACGs
     */
    @GetMapping("/user/{email}/acg")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Set<String>> getUserAcgs(@PathVariable String email) {
        Optional<UserInfo> userOpt = userInfoService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(userOpt.get().getAccessControlGroups());
    }

    /**
     * Get user's COIs
     */
    @GetMapping("/user/{email}/coi")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Set<String>> getUserCois(@PathVariable String email) {
        Optional<UserInfo> userOpt = userInfoService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(userOpt.get().getCommunitiesOfInterest());
    }

    /**
     * Get all users (admin only)
     * Returns list of all users without passwords
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userInfoService.findAll());
    }
}
