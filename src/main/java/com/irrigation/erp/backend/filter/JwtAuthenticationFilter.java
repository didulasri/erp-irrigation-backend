package com.irrigation.erp.backend.filter;

import com.irrigation.erp.backend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Skip JWT validation for auth endpoints
        String requestPath = request.getRequestURI();
        if (requestPath.startsWith("/api/auth/login") ||requestPath.startsWith("/api/auth/register") ||
                requestPath.startsWith("/api/auth/roles")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");

        String email = null;
        String jwtToken = null;

        // JWT Token is in the form "Bearer token"
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                email = jwtUtil.getEmailFromToken(jwtToken);
            } catch (Exception e) {
                logger.error("Unable to get JWT Token or JWT Token has expired: " + e.getMessage());
            }
        } else {
            logger.warn("JWT Token does not begin with Bearer String");
        }

        // Validate token
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwtToken, email)) {
                // Token is valid, set authentication
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(email, null, new ArrayList<>());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}

//
//package com.irrigation.erp.backend.filter;
//
//import com.irrigation.erp.backend.util.JwtUtil;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    // Endpoints that bypass JWT auth
//    // (startsWith match is used so /api/auth/check-password-change/123 also bypasses)
//    private static final List<String> AUTH_WHITELIST = List.of(
//            "/api/auth/login",
//            "/api/auth/register",
//            "/api/auth/roles",
//            "/api/auth/validate-token",
//            "/api/auth/check-password-change"
//    );
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        final String path = request.getRequestURI();
//
//        if (isWhitelisted(path)) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        final String authHeader = request.getHeader("Authorization");
//        String token = null;
//        String email = null;
//
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            token = authHeader.substring(7);
//            try {
//                email = jwtUtil.getEmailFromToken(token); // subject
//            } catch (Exception e) {
//                log.warn("JWT parse error: {}", e.getMessage());
//            }
//        } else {
//            if (authHeader != null) {
//                log.debug("Authorization header present but not Bearer, header={}", authHeader);
//            } else {
//                log.debug("No Authorization header present for path={}", path);
//            }
//        }
//
//        // Only authenticate if we have an email and no existing authentication
//        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//            try {
//                if (jwtUtil.validateToken(token, email)) {
//                    // Extract roles from token (supports "role"/"roles"/"authorities")
//                    List<String> roles = safeRolesFromToken(token);
//
//                    // Map to ROLE_* authorities required by Spring Security
//                    List<GrantedAuthority> authorities = roles.stream()
//                            .filter(Objects::nonNull)
//                            .map(String::trim)
//                            .filter(s -> !s.isEmpty())
//                            .map(this::normalizeRoleToSpring) // ROLE_ prefix
//                            .distinct()
//                            .map(SimpleGrantedAuthority::new)
//                            .collect(Collectors.toList());
//
//                    log.debug("Authenticated user={} with authorities={}", email, authorities);
//
//                    UsernamePasswordAuthenticationToken authentication =
//                            new UsernamePasswordAuthenticationToken(email, null, authorities);
//                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//                    SecurityContextHolder.getContext().setAuthentication(authentication);
//                    log.info("JWT OK for user={} -> authorities={}", email, authorities);
//                } else {
//                    log.debug("Invalid JWT for user={}, path={}", email, path);
//                }
//            } catch (Exception e) {
//                log.warn("JWT validation error for path {}: {}", path, e.getMessage());
//            }
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    private boolean isWhitelisted(String path) {
//        if (path == null) return false;
//        for (String open : AUTH_WHITELIST) {
//            if (path.equals(open) || path.startsWith(open + "/")) return true;
//        }
//        return false;
//    }
//
//    private List<String> safeRolesFromToken(String token) {
//        try {
//            List<String> roles = jwtUtil.getRolesFromToken(token);
//            return (roles != null) ? roles : Collections.emptyList();
//        } catch (Exception e) {
//            log.debug("Failed to read roles from JWT: {}", e.getMessage());
//            return Collections.emptyList();
//        }
//    }
//
//    /**
//     * Normalize role to Spring format: ensure 'ROLE_' prefix and uppercase/underscored name.
//     * e.g., "storekeeper" -> "ROLE_STOREKEEPER", "ROLE_admin" -> "ROLE_ADMIN"
//     */
//    private String normalizeRoleToSpring(String role) {
//        String r = role.toUpperCase(Locale.ROOT).replace(' ', '_');
//        if (!r.startsWith("ROLE_")) r = "ROLE_" + r;
//        return r;
//    }
//
//
//
//}
//
