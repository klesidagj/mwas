package mk.ukim.finki.wp.kol2024g1.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.stream.Collectors;

@AllArgsConstructor
@Component
public class SignInSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String token = jwtTokenProvider.createToken(
                authentication.getName(),
                authentication
                        .getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","))
        );

        String refreshToken = jwtTokenProvider.createToken(
                authentication.getName(),
                "ROLE_REFRESH",
                7 * 24 * 60 * 60 * 1000L
        );

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"token\":\"" + token + "\"}");
        response.getWriter().flush();
        response.setHeader("Authorization", "Bearer " + token);
        response.setStatus(HttpServletResponse.SC_OK);
        response.sendRedirect("/");

    }
}

    /**
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String token = jwtTokenProvider.createToken(
                authentication.getName(),
                authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","))
        );

        // ✅ Ensure header contains the JWT token
        response.setHeader("Authorization", "Bearer " + token);

        // ✅ Write JWT token in the response body (Fix for missing header)
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"token\":\"" + token + "\"}");
        response.getWriter().flush();
        Cookie jwtCookie = new Cookie("JWT-TOKEN", token);
        jwtCookie.setHttpOnly(true);  // Security best practice: Prevents JavaScript access
        jwtCookie.setSecure(false);   // Set to true if using HTTPS
        jwtCookie.setPath("/");       // Makes it available across all endpoints
        jwtCookie.setMaxAge(3600);    // 1 hour expira
        response.addCookie(jwtCookie);
        response.sendRedirect("/");

    }
     **/
