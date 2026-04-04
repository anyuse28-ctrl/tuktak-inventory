package com.tuktak.inventory.service;

import com.tuktak.inventory.entity.AdminUser;
import com.tuktak.inventory.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // ✅ Try username first, then email
        AdminUser adminUser = adminUserRepository.findByUsername(username)
                .or(() -> adminUserRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Admin not found with username or email: " + username));

        return new User(
                adminUser.getUsername(),
                adminUser.getPassword(),
                adminUser.isEnabled(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + adminUser.getRole().name()))
        );
    }
}