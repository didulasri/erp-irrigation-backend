package com.irrigation.erp.backend.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret:mySecretKey12345678901234567890123456789012345678901234567890}")
    private String secret;

    @Value("${jwt.expiration:3153600000000}") // Default to ~100 years
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Generate token with user details
    public String generateToken(String email, String username, String role, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("role", role);
        claims.put("userId", userId);

        return createToken(claims, email);
    }

    // Create JWT token - Modified for non-expiring tokens
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();

        JwtBuilder jwtBuilder = Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256);

        // Only set expiration if it's not -1 (non-expiring indicator)
        if (expiration != -1) {
            Date expiryDate = new Date(now.getTime() + expiration);
            jwtBuilder.setExpiration(expiryDate);
        }
        // If expiration is -1, don't set any expiration date

        return jwtBuilder.compact();
    }

    // Extract email from token
    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // Extract username from token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, claims -> (String) claims.get("username"));
    }

    // Extract role from token
    public String getRoleFromToken(String token) {
        return getClaimFromToken(token, claims -> (String) claims.get("role"));
    }

    // Extract user ID from token
    public Long getUserIdFromToken(String token) {
        return getClaimFromToken(token, claims ->
                claims.get("userId") != null ? Long.valueOf(claims.get("userId").toString()) : null);
    }

    // Extract expiration date from token
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    // Extract any claim from token
    public <T> T getClaimFromToken(String token, ClaimsResolver<T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.resolve(claims);
    }

    // Get all claims from token
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Check if token is expired - Modified for non-expiring tokens
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            // If no expiration date is set, token never expires
            if (expiration == null) {
                return false;
            }
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // Validate token
    public Boolean validateToken(String token, String email) {
        try {
            final String tokenEmail = getEmailFromToken(token);
            return (tokenEmail.equals(email) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    // Check if token is valid without email
    public Boolean isTokenValid(String token) {
        try {
            getAllClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // Check if this token has an expiration date
    public Boolean hasExpirationDate(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration != null;
        } catch (Exception e) {
            return false;
        }
    }

    // Get token expiration time in minutes (returns -1 if non-expiring)
    public Long getExpirationTimeInMinutes() {
        if (expiration == -1) {
            return -1L; // Indicates non-expiring
        }
        return expiration / (1000 * 60);
    }

    @FunctionalInterface
    public interface ClaimsResolver<T> {
        T resolve(Claims claims);
    }
}


//package com.irrigation.erp.backend.util;
//
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import java.util.*;
//
//@Component
//public class JwtUtil {
//
//    @Value("${jwt.secret:mySecretKey12345678901234567890123456789012345678901234567890}")
//    private String secret;
//
//    @Value("${jwt.expiration:3153600000000}") // Default to ~100 years
//    private Long expiration;
//
//    private SecretKey getSigningKey() {
//        return Keys.hmacShaKeyFor(secret.getBytes());
//    }
//
//    // === Generate token ===
//    public String generateToken(String email, String username, String role, Long userId) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("username", username);
//        claims.put("role", role);     // single role
//        claims.put("userId", userId);
//
//        return createToken(claims, email);
//    }
//
//    private String createToken(Map<String, Object> claims, String subject) {
//        Date now = new Date();
//
//        JwtBuilder jwtBuilder = Jwts.builder()
//                .setClaims(claims)
//                .setSubject(subject)
//                .setIssuedAt(now)
//                .signWith(getSigningKey(), SignatureAlgorithm.HS256);
//
//        if (expiration != -1) {
//            Date expiryDate = new Date(now.getTime() + expiration);
//            jwtBuilder.setExpiration(expiryDate);
//        }
//        return jwtBuilder.compact();
//    }
//
//    // === Extract common claims ===
//    public String getEmailFromToken(String token) {
//        return getClaimFromToken(token, Claims::getSubject);
//    }
//
//    public String getUsernameFromToken(String token) {
//        return getClaimFromToken(token, claims -> (String) claims.get("username"));
//    }
//
//    public String getRoleFromToken(String token) {
//        return getClaimFromToken(token, claims -> (String) claims.get("role"));
//    }
//
//    public Long getUserIdFromToken(String token) {
//        return getClaimFromToken(token,
//                claims -> claims.get("userId") != null
//                        ? Long.valueOf(claims.get("userId").toString())
//                        : null);
//    }
//
//    public Date getExpirationDateFromToken(String token) {
//        return getClaimFromToken(token, Claims::getExpiration);
//    }
//
//    public <T> T getClaimFromToken(String token, ClaimsResolver<T> claimsResolver) {
//        final Claims claims = getAllClaimsFromToken(token);
//        return claimsResolver.resolve(claims);
//    }
//
//    private Claims getAllClaimsFromToken(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(getSigningKey())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    // === Expiration / Validation ===
//    public Boolean isTokenExpired(String token) {
//        try {
//            final Date expiration = getExpirationDateFromToken(token);
//            if (expiration == null) {
//                return false; // no expiry = valid forever
//            }
//            return expiration.before(new Date());
//        } catch (Exception e) {
//            return true;
//        }
//    }
//
//    public Boolean validateToken(String token, String email) {
//        try {
//            final String tokenEmail = getEmailFromToken(token);
//            return (tokenEmail.equals(email) && !isTokenExpired(token));
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    public Boolean isTokenValid(String token) {
//        try {
//            getAllClaimsFromToken(token);
//            return !isTokenExpired(token);
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    public Boolean hasExpirationDate(String token) {
//        try {
//            final Date expiration = getExpirationDateFromToken(token);
//            return expiration != null;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    public Long getExpirationTimeInMinutes() {
//        if (expiration == -1) {
//            return -1L;
//        }
//        return expiration / (1000 * 60);
//    }
//
//    // === NEW: Extract roles list safely ===
//    public List<String> getRolesFromToken(String token) {
//        Claims claims = getAllClaimsFromToken(token);
//
//        Object rolesClaim = null;
//        if (claims.containsKey("roles")) {
//            rolesClaim = claims.get("roles"); // array/list
//        } else if (claims.containsKey("authorities")) {
//            rolesClaim = claims.get("authorities");
//        } else if (claims.containsKey("role")) {
//            rolesClaim = claims.get("role"); // single string
//        }
//
//        if (rolesClaim == null) {
//            return Collections.emptyList();
//        }
//
//        if (rolesClaim instanceof Collection<?>) {
//            List<String> roles = new ArrayList<>();
//            for (Object o : (Collection<?>) rolesClaim) {
//                if (o != null) roles.add(o.toString());
//            }
//            return roles;
//        }
//
//        return List.of(rolesClaim.toString());
//    }
//
//    @FunctionalInterface
//    public interface ClaimsResolver<T> {
//        T resolve(Claims claims);
//    }
//}
