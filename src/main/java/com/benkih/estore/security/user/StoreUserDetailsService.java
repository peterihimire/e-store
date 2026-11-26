package com.benkih.estore.security.user;

import com.benkih.estore.user.entity.User;
import com.benkih.estore.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user =
                Optional.ofNullable(userRepository.findByEmail(email))
                        .orElseThrow()
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return StoreUserDetails.buildUserDetails(user);
    }
}
