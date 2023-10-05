package org.knvvl.exam.spring;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private final List<UserLogon> userLogons = new ArrayList<>();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        User user = userRepository.findByUsername(username);
        if (user == null)
            throw new UsernameNotFoundException("User not found: " + username);

        userLogons.add(new UserLogon(LocalDateTime.now(), username));
        if (userLogons.size() > 100)
            userLogons.remove(0);

        return org.springframework.security.core.userdetails.User.withUsername(username)
            //.passwordEncoder()
            .username(username)
            .password(user.getEncodedPassword())
            .roles("user")
            .build();
    }

    public List<UserLogon> getUserLogons()
    {
        return userLogons;
    }

    public record UserLogon(LocalDateTime loggedOn, String username)
    {
        public String toString()
        {
            return loggedOn.withNano(0) + ": " + username;
        }
    }
}
