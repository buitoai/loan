package com.loan.decision.security;

import com.loan.decision.model.entity.User;
import com.loan.decision.model.entity.UserStatus;
import com.loan.decision.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Loads user-specific data from the database for Spring Security authentication.
 * Maps the application {@link com.loan.decision.model.entity.Role} to a {@link SimpleGrantedAuthority}
 * using the {@code ROLE_} prefix convention.
 * An {@link UserStatus#INACTIVE} user is represented as a disabled account in the returned {@link UserDetails}.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Locates the user by username and builds a Spring Security {@link UserDetails} object.
     *
     * @param username the username identifying the user whose data is required
     * @return a fully populated {@link UserDetails} instance (never {@code null})
     * @throws UsernameNotFoundException if no user with the given username exists in the database
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));

        boolean enabled = UserStatus.ACTIVE.equals(user.getStatus());

        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                enabled,          // enabled
                true,             // accountNonExpired
                true,             // credentialsNonExpired
                true,             // accountNonLocked
                List.of(authority)
        );
    }
}
