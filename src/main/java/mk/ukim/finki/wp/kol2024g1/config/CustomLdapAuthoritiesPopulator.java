package mk.ukim.finki.wp.kol2024g1.config;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {

    @Override
    public Collection<GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Get the full DN of the authenticated user
        String userDn = userData.getDn().toString().toLowerCase();

        // Assign roles based on organizational unit (OU)
        if (userDn.contains("ou=managers")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else if (userDn.contains("ou=employees")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_GUEST"));  // Default role if no match
        }

        return authorities;
    }
}
