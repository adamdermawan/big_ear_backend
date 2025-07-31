package org.bigear.api.bigearbackend.auth;

import org.bigear.api.bigearbackend.users.User;
import org.bigear.api.bigearbackend.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // You'll need a JWT utility class - a simple one is provided below
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userService.getUserByEmail(loginRequest.email()).orElseThrow();
            String token = jwtUtil.generateToken(user.getEmail());

            return ResponseEntity.ok(new AuthResponse(token, user));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        if (userService.getUserByEmail(registerRequest.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already taken!");
        }

        User newUser = new User();
        newUser.setName(registerRequest.name());
        newUser.setEmail(registerRequest.email());
        newUser.setPassword(passwordEncoder.encode(registerRequest.password()));

        User savedUser = userService.saveUser(newUser);
        String token = jwtUtil.generateToken(savedUser.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token, savedUser));
    }
}


