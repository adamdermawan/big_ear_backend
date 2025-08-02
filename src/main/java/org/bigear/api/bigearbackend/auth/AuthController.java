package org.bigear.api.bigearbackend.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.bigear.api.bigearbackend.dto.ChangePasswordRequest;
import org.bigear.api.bigearbackend.dto.UpdateProfileRequest;
import org.bigear.api.bigearbackend.googleAuth.GoogleAuthRequest;
import org.bigear.api.bigearbackend.googleAuth.GoogleTokenVerifier;
import org.bigear.api.bigearbackend.users.User;
import org.bigear.api.bigearbackend.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Add this for CORS
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private GoogleTokenVerifier googleTokenVerifier;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("🔄 Login attempt for email: " + loginRequest.email());

            // Validate input
            if (loginRequest.email() == null || loginRequest.email().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Email is required"));
            }
            if (loginRequest.password() == null || loginRequest.password().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Password is required"));
            }

            // Check if user exists first
            if (!userService.getUserByEmail(loginRequest.email().trim().toLowerCase()).isPresent()) {
                System.out.println("❌ User not found: " + loginRequest.email());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Invalid email or password"));
            }

            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email().trim().toLowerCase(),
                            loginRequest.password()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userService.getUserByEmail(loginRequest.email().trim().toLowerCase()).orElseThrow();
            String token = jwtUtil.generateToken(user.getEmail());

            System.out.println("✅ Login successful for user: " + user.getEmail());
            return ResponseEntity.ok(new AuthResponse(token, user));

        } catch (BadCredentialsException e) {
            System.out.println("❌ Bad credentials for email: " + loginRequest.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Invalid email or password"));
        } catch (Exception e) {
            System.out.println("❌ Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            System.out.println("🔄 Registration attempt for email: " + registerRequest.email());

            // Validate input
            if (registerRequest.name() == null || registerRequest.name().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Name is required"));
            }
            if (registerRequest.email() == null || registerRequest.email().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Email is required"));
            }
            if (registerRequest.password() == null || registerRequest.password().length() < 6) {
                return ResponseEntity.badRequest().body(createErrorResponse("Password must be at least 6 characters"));
            }

            String email = registerRequest.email().trim().toLowerCase();

            // Check if user already exists
            if (userService.getUserByEmail(email).isPresent()) {
                System.out.println("❌ Email already exists: " + email);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(createErrorResponse("Email is already taken!"));
            }

            // Create new user
            User newUser = new User();
            newUser.setName(registerRequest.name().trim());
            newUser.setEmail(email);
            newUser.setPassword(passwordEncoder.encode(registerRequest.password()));

            User savedUser = userService.saveUser(newUser);
            String token = jwtUtil.generateToken(savedUser.getEmail());

            System.out.println("✅ Registration successful for user: " + savedUser.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token, savedUser));

        } catch (Exception e) {
            System.out.println("❌ Registration error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Registration failed: " + e.getMessage()));
        }
    }

    // Add a test endpoint to verify the auth controller is accessible
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Auth controller is working!");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    // Get current user info (for authenticated users)
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication required"));
            }

            String userEmail = authentication.getName();
            User user = userService.getUserByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to get user info: " + e.getMessage()));
        }
    }

    // NEW: Update user profile (name only) - AUTHENTICATED ENDPOINT
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest updateRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication required"));
            }

            String currentUserEmail = authentication.getName();
            User user = userService.getUserByEmail(currentUserEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Validate input
            if (updateRequest.name() == null || updateRequest.name().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Name is required"));
            }

            // Update only the name, keep email unchanged
            user.setName(updateRequest.name().trim());
            User updatedUser = userService.saveUser(user);

            System.out.println("✅ Profile updated for user: " + updatedUser.getEmail());
            return ResponseEntity.ok(updatedUser);

        } catch (Exception e) {
            System.out.println("❌ Profile update error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Profile update failed: " + e.getMessage()));
        }
    }

    // NEW: Change user password - AUTHENTICATED ENDPOINT
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication required"));
            }

            String userEmail = authentication.getName();
            User user = userService.getUserByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Validate input
            if (changePasswordRequest.currentPassword() == null || changePasswordRequest.currentPassword().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Current password is required"));
            }
            if (changePasswordRequest.newPassword() == null || changePasswordRequest.newPassword().length() < 6) {
                return ResponseEntity.badRequest().body(createErrorResponse("New password must be at least 6 characters"));
            }

            // Verify current password
            if (!passwordEncoder.matches(changePasswordRequest.currentPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Current password is incorrect"));
            }

            // Update password
            user.setPassword(passwordEncoder.encode(changePasswordRequest.newPassword()));
            userService.saveUser(user);

            System.out.println("✅ Password changed for user: " + user.getEmail());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ Password change error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Password change failed: " + e.getMessage()));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }

    @PostMapping("/google-signin")
    public ResponseEntity<?> googleSignIn(@RequestBody GoogleAuthRequest googleAuthRequest) {
        try {
            System.out.println("🔄 Google sign-in attempt for email: " + googleAuthRequest.email());

            // Verify the Google token
            GoogleIdToken.Payload payload = googleTokenVerifier.verifyToken(googleAuthRequest.idToken());

            // Extract user info from the verified token
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // Verify the email matches the request
            if (!email.equals(googleAuthRequest.email())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Token email doesn't match request email"));
            }

            // Check if user already exists
            Optional<User> existingUser = userService.getUserByEmail(email);
            User user;

            if (existingUser.isPresent()) {
                // User exists, just login
                user = existingUser.get();
                System.out.println("✅ Existing Google user found: " + email);
            } else {
                // Create new user
                user = new User();
                user.setEmail(email);
                user.setName(name);
                user.setPassword(passwordEncoder.encode("GOOGLE_AUTH_" + System.currentTimeMillis())); // Dummy password
                user = userService.saveUser(user);
                System.out.println("✅ New Google user created: " + email);
            }

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail());

            return ResponseEntity.ok(new AuthResponse(token, user));

        } catch (Exception e) {
            System.out.println("❌ Google sign-in error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Google sign-in failed: " + e.getMessage()));
        }
    }
}