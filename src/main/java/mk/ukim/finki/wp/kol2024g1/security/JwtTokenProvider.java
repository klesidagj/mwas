package mk.ukim.finki.wp.kol2024g1.security;

import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mk.ukim.finki.wp.kol2024g1.service.impl.UserDetailsServiceImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {
    private final String secret = "&sdfgjkl-yuiopZXCvbnm5d6f7g8h9j0k1l2z3x4c5v6b7n8m9";
    private final SecretKey secretKey;

    private final long validityInMilliseconds = 3600000; // 1 hour

    private final UserDetailsServiceImpl userDetailsService;

    public JwtTokenProvider(UserDetailsServiceImpl userDetailsService) throws NoSuchAlgorithmException {
        this.userDetailsService = userDetailsService;

        // Generate a secure SecretKey
        byte[] keyBytes = this.secret.getBytes(Charset.defaultCharset());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(String username, String role) {
        return this.createToken(username, role, null);
    }

    public String createToken(String username, String role, Long validityInMilliseconds) {
        ClaimsBuilder claims = Jwts.claims().subject(username);
        claims.add("role", role);

        Date now = new Date();
        if (validityInMilliseconds == null) {
            validityInMilliseconds = this.validityInMilliseconds;
        }
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .claims(claims.build())
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public Authentication getAuthentication(String token) {
        String username = getUsername(token);
        String role = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
        // I'm ensuring that the roles are not injected from the client side
        UserDetails userDetails = new User(
                username,
                "",
                List.of(new SimpleGrantedAuthority(role))
        );

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
