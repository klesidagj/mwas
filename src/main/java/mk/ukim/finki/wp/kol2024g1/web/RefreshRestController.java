package mk.ukim.finki.wp.kol2024g1.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import mk.ukim.finki.wp.kol2024g1.security.JwtTokenProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/api/refresh_token")
public class RefreshRestController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;


    @PostMapping
    public String refresh(HttpServletRequest request, HttpServletResponse response) {
        String username = jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(request));
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = jwtTokenProvider.createToken(username,
                userDetails.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(", ")));


        response.setHeader("Authorization", "Bearer " + token);

        return token;
    }
}
