package com.example.finance.finance.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.List;

/**
 * JWTfilter
 * Purpose:
 * - Intercepts each request
 * - If Bearer token exists, validates it
 * - Extracts email + role from token
 * - Creates Authentication object WITHOUT querying DB
 * - Stores Authentication in SecurityContextHolder for authorization decisions
 */
@Component
public class JWTfilter extends OncePerRequestFilter {

    private final JWTutil jwtUtil;

    public JWTfilter(JWTutil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * doFilterInternal()
     * Purpose:
     * - Skip public endpoints (swagger, login, etc.)
     * - Read Authorization header
     * - Validate token
     * - Set SecurityContext with user's role
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip swagger and auth endpoints (adjust as needed)

        if (path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        // helper to write consistent JSON errors
        final java.util.function.BiConsumer<Integer, String> sendError = (status, message) -> {
            try {
                response.setStatus(status);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(
                        "{\"status\":" + status +
                                ",\"error\":\"" + (status == 401 ? "UNAUTHORIZED" : "FORBIDDEN") + "\"" +
                                ",\"message\":\"" + message.replace("\"", "'") + "\"" +
                                ",\"path\":\"" + path + "\"}"
                );
            } catch (Exception ignored) {}
        };

        // No Bearer
        if (header == null || !header.startsWith("Bearer ")) {

            sendError.accept(401, "Missing Authorization header. Expected: 'Authorization: Bearer <token>'.");
            return;
        }

        String token = header.substring(7);

        // Token invalid
        if (!jwtUtil.validateToken(token)) {

            sendError.accept(401, "Invalid or expired JWT token. Please login again.");
            return;

        }

        String email = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);

        if (email == null || role == null) {
            chain.doFilter(request, response);
            return;
        }

        // Avoid resetting auth if already authenticated
        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            // Convert "ADMIN" -> "ROLE_ADMIN" for Spring Security
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);

            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // Optional: renew token if expiring soon (usually better done via refresh endpoint)
        if (jwtUtil.isTokenExpiringSoon(token)) {
            String newToken = jwtUtil.generateToken(email, role);
            response.setHeader("Authorization", "Bearer " + newToken);
        }

        chain.doFilter(request, response);
    }
}