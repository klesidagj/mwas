package mk.ukim.finki.wp.kol2024g1.config;

import jakarta.servlet.DispatcherType;
import lombok.AllArgsConstructor;
import mk.ukim.finki.wp.kol2024g1.security.JwtAuthFilter;
import mk.ukim.finki.wp.kol2024g1.security.JwtTokenProvider;
import mk.ukim.finki.wp.kol2024g1.security.SignInSuccessHandler;
import mk.ukim.finki.wp.kol2024g1.service.impl.EmployeeServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import static org.springframework.boot.autoconfigure.security.SecurityProperties.BASIC_AUTH_ORDER;


@AllArgsConstructor
@Configuration
public class SecurityConfig {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, DaoAuthenticationProvider daoAuthenticationProvider, LdapAuthenticationProvider ldapProvider, CustomOidcUserService customOidcUserService) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/home", "/about", "/reservations").permitAll()
                    .requestMatchers("/reservations/extend/**").hasAnyAuthority("ROLE_USER", "SCOPE_employee")
                    .anyRequest().hasAnyRole("ADMIN", "SCOPE_manager")
            )
            .authenticationProvider(daoAuthenticationProvider)
            .authenticationProvider(ldapProvider)
                //change
            .formLogin((login) -> login
                    .defaultSuccessUrl("/reservations", true)
                    .failureUrl("/login?error=true")
                    .permitAll()
            )
            .oauth2Login(Customizer.withDefaults())
            .oauth2Login(oauth2 ->
                    oauth2.userInfoEndpoint(userInfo -> {
                    userInfo.oidcUserService(customOidcUserService());
                    })
            )
            .logout((logout) -> logout
                    .logoutSuccessUrl("/login")
                    .logoutSuccessHandler(oidcLogoutSuccessHandler())
                    .permitAll()
                    .deleteCookies("JSESSIONID")
            )
            .rememberMe((rememberMe) -> rememberMe
                    .key("uniqueAndSecret")
                    .tokenValiditySeconds(604800)
            );
        return http.build();
    }

    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/");
        return oidcLogoutSuccessHandler;
    }

    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("""
            ROLE_ADMIN > ROLE_MANAGER
            ROLE_MANAGER > SCOPE_manager
            ROLE_ADMIN > ROLE_USER
            ROLE_ADMIN > SCOPE_manager
            ROLE_USER > ROLE_EMPLOYEE
            ROLE_EMPLOYEE > SCOPE_employee
            ROLE_USER > SCOPE_employee
            """);
    }

    @Order(BASIC_AUTH_ORDER - 100)
    @Bean
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthenticationFilter) throws Exception {
        http.securityMatcher("/api/**") // Apply this chain only to /api/** endpoints
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/api/login").permitAll()
                        .requestMatchers("/api/refresh_token").hasAnyRole("REFRESH", "ADMIN")
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        .anyRequest().authenticated() // All API requests must be authenticated
                )
                .formLogin(login -> login
                        .successHandler(authenticationSuccessHandler())
                        .permitAll()
                )// ✅ Ensures successful form login processes JWT
                .csrf(CsrfConfigurer::disable) // Disable CSRF for stateless APIs
                .cors(CorsConfigurer::disable) // Disable CORS
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Enforce stateless session
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint((req, res, authEx) -> {
                            res.setHeader("WWW-Authenticate", "Basic realm=\"\"");
                            res.setStatus(HttpStatus.UNAUTHORIZED.value());
                        })
                        .accessDeniedHandler(new AccessDeniedHandlerImpl()));
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return daoAuthenticationProvider;
    }

    @Profile("ldap-auth")
    @Bean(name = "customLdapProvider")
    public LdapAuthenticationProvider customLdapProvider(BaseLdapPathContextSource contextSource) {
        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        authenticator.setUserSearch(new FilterBasedLdapUserSearch(
                "ou=Users,dc=hotelchain,dc=com",
                "cn={0}",
                contextSource
        ));
        return new LdapAuthenticationProvider(authenticator, ldapAuthoritiesPopulator());
    }

    @Profile("ldap-auth")
    @Bean
    public LdapAuthoritiesPopulator ldapAuthoritiesPopulator() {
        return new CustomLdapAuthoritiesPopulator();
    }

    @Profile("ldap-auth")
    @Bean(name = "ldapContextSource")
    public DefaultSpringSecurityContextSource ldapContextSource() {
        DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource("ldap://185.153.49.190:389");
        contextSource.setUserDn("cn=admin,dc=hotelchain,dc=com");
        contextSource.setPassword("admin");
        return contextSource;
    }

    @Bean
    public CustomOidcUserService customOidcUserService() {
        return new CustomOidcUserService();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new SignInSuccessHandler(jwtTokenProvider);
    }
}

/**
private final UserDetailsServiceImpl userDetailsService;
private final JwtAuthFilter jwtAuthFilter;
private final ClientRegistrationRepository clientRegistrationRepository;
private final JwtTokenProvider jwtTokenProvider;

@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
    return new JwtAuthenticationConverter();
}

@Bean
public JwtDecoder jwtDecoder() {
    return JwtDecoders.fromIssuerLocation("http://localhost:8080/realms/hotelchain");
}
@Bean
public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
}

@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
}


@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/reservations").permitAll()
                    .requestMatchers("/reservations/extend/**").hasRole("USER")
                    .requestMatchers("/api/login").permitAll() // ✅ Explicitly allow API login
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().hasRole("ADMIN")
            )
            .formLogin(login -> login
                    .successHandler(authenticationSuccessHandler()) // ✅ Ensures successful form login processes JWT
                    .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(userInfo -> userInfo.userAuthoritiesMapper(grantedAuthoritiesMapper()))
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(Customizer.withDefaults())
            )
            .rememberMe(rememberMe -> rememberMe
                    .key("uniqueAndSecret")
                    .tokenValiditySeconds(604800) // 7 days
            )
            .logout(logout -> logout
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}

@Bean
public GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
    return authorities -> authorities.stream()
            .map(authority -> authority.getAuthority().contains("admin")
                    ? new SimpleGrantedAuthority("ROLE_ADMIN")
                    : new SimpleGrantedAuthority("ROLE_USER"))
            .collect(Collectors.toSet());
}
@Bean
public RoleHierarchy roleHierarchy() {
    RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
    roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
    return roleHierarchy;
}

@Bean
public OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler() {
    return new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
}


}
**/