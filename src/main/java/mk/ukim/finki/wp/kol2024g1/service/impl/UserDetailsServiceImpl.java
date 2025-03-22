package mk.ukim.finki.wp.kol2024g1.service.impl;

import mk.ukim.finki.wp.kol2024g1.model.Employee;
import mk.ukim.finki.wp.kol2024g1.repository.EmployeeRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Profile("!memory")
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    public UserDetailsServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Loading user: " + username); // Debugging
        return employeeRepository.findByNameIgnoreCase(username)
                .map(u -> {
                    String role = u.getIsManager() ? "ROLE_ADMIN" : "ROLE_USER"; // ✅ Always use ROLE_ prefix
                    System.out.println("User found: " + u.getName() + ", Assigned Role: " + role); // ✅ Debugging
                    return new User(u.getName(), u.getPassword(), List.of(new SimpleGrantedAuthority(role)));
                })
                .orElseThrow(() -> {
                    System.out.println("User not found: " + username);
                    return new UsernameNotFoundException(username);
                });
    }
}