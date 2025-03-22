package mk.ukim.finki.wp.kol2024g1.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mk.ukim.finki.wp.kol2024g1.security.JwtTokenProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }


    @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String path = request.getRequestURI();
            return path.equals("/api/login") || path.equals("/login");
        }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }

    //    @Override
    //    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    //            throws ServletException, IOException {
    //        String token = jwtTokenProvider.resolveToken(request);
    //
    //        if (token != null && jwtTokenProvider.validateToken(token)) {
    //            Authentication auth = jwtTokenProvider.getAuthentication(token);
    //            SecurityContextHolder.getContext().setAuthentication(auth);
    //            response.setHeader("Authorization", "Bearer " + token);
    //        }
    //
    //        filterChain.doFilter(request, response);
    //    }

}