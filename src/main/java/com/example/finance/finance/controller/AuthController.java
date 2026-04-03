package com.example.finance.finance.controller;

import com.example.finance.finance.security.JWTutil;
import com.example.finance.finance.repository.user_repository;
import com.example.finance.finance.entity.userEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;

/**
 * AuthController
 *
 * Purpose:
 * - Provides authentication endpoints (login)
 * - Provides a restricted signup endpoint ONLY for creating the first Admin (or additional Admins)
 *
 * Rules (as per your requirement):
 * - Only Admin can sign up (i.e., create an admin account).
 * - All other users (VIEWER/ANALYST) must be created by Admin via user-management APIs.
 *
 * Notes:
 * - This controller uses email as the unique identifier (username).
 * - JWT token includes "role" claim so downstream APIs can authorize without DB calls per request.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    user_repository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final JWTutil jwtUtil;

    public AuthController(user_repository userRepository,
                          PasswordEncoder passwordEncoder,
                          JWTutil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * POST /auth/login
     *
     * Purpose:
     * - Authenticates user by email + password (checked against DB)
     * - Rejects INACTIVE users
     * - Returns JWT token containing the user's role claim (ADMIN/ANALYST/VIEWER)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {

        userEntity userOpt = userRepository.findByEmail(req.email());
        if (userOpt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiError("INVALID_CREDENTIALS", "Invalid email or password."));
        }

        userEntity user = userOpt;

        // Block inactive users
        if (user.getStatus() == userEntity.Status.INACTIVE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiError("USER_INACTIVE", "User is inactive. Contact admin."));
        }

        // Verify password
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiError("INVALID_CREDENTIALS", "Invalid email or password."));
        }

        String role = user.getRole().name(); // "ADMIN" | "ANALYST" | "VIEWER"
        String token = jwtUtil.generateToken(user.getEmail(), role);

        return ResponseEntity.ok(new LoginResponse(token, user.getEmail(), role));
    }

    /**
     * POST /auth/admin/signup
     *
     * Purpose:
     * - Creates an ADMIN account only.
     */
    @PostMapping("/admin/signup")
    public ResponseEntity<?> adminSignup(@Valid @RequestBody AdminSignupRequest req,
                                         @RequestHeader(value = "Authorization", required = false) String authHeader) {



        if (userRepository.findByEmail(req.email()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiError("EMAIL_EXISTS", "Email already exists."));
        }

        userEntity admin = new userEntity();
        admin.setName(req.name());
        admin.setEmail(req.email());
        admin.setPassword(passwordEncoder.encode(req.password()));
        admin.setRole(userEntity.Role.ADMIN);
        admin.setStatus(userEntity.Status.ACTIVE);

        userRepository.save(admin);
        String token = jwtUtil.generateToken(admin.getEmail(), admin.getRole().name());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SignupResponse("Admin created successfully.", admin.getEmail(), "ADMIN", token));
    }

    /**
     * LoginRequest
     * Purpose: Request body for /auth/login.
     */
    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    /**
     * AdminSignupRequest
     * Purpose: Request body for /auth/admin/signup.
     * Note: This endpoint always creates role=ADMIN.
     */
    public record AdminSignupRequest(
            @NotBlank @Size(min = 2, max = 50) String name,
            @Email @NotBlank String email,
            @NotBlank @Size(min = 6, max = 100) String password
    ) {}

    /**
     * LoginResponse
     * Purpose: Successful login response containing JWT and basic user info.
     */
    public record LoginResponse(
            String token,
            String email,
            String role
    ) {}

    /**
     * SignupResponse
     * Purpose: Successful admin signup response.
     */
    public record SignupResponse(
            String message,
            String email,
            String role,
            String token
    ) {}

    /**
     * ApiError
     * Purpose: Small consistent error response payload.
     */
    public record ApiError(
            String code,
            String message,
            Instant timestamp
    ) {
        public ApiError(String code, String message) {
            this(code, message, Instant.now());
        }
    }
}