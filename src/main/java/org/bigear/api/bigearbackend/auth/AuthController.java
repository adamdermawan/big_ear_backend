package org.bigear.api.bigearbackend.auth;

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

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}