package org.knvvl.exam.spring;

import org.knvvl.exam.entities.User;
import org.knvvl.exam.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService
{
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        User user = userRepository.findByUsername(username);
        if (user == null)
            throw new UsernameNotFoundException("User not found: " + username);
        return org.springframework.security.core.userdetails.User.withUsername(username)
            //.passwordEncoder()
            .username(username)
            .password(user.getEncodedPassword())
            .roles("user")
            .build();
    }
}
