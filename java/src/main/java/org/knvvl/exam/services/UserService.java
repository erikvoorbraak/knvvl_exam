package org.knvvl.exam.services;

import static org.knvvl.exam.services.ExamRepositories.SORT_BY_ID;

import java.util.List;
import java.util.function.Supplier;

import org.knvvl.exam.entities.User;
import org.knvvl.exam.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

@Service
public class UserService
{
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChangeDetector changeDetector;

    private Supplier<Authentication> authenticationSupplier = () -> SecurityContextHolder.getContext().getAuthentication();

    @PostConstruct
    public void init()
    {
        if (userRepository.findByUsername("admin") == null)
        {
            User admin = new User(1, "admin");
            setPassword(admin, "Welcome01");
            userRepository.save(admin);
        }
    }

    public List<User> findAll()
    {
        return userRepository.findAll(SORT_BY_ID);
    }

    public User getById(int userId)
    {
        return userRepository.getReferenceById(userId);
    }

    public String validatePassword(String password)
    {
        if (password.length() < 8)
            return "Password must be at least 8 characters";
        if (password.length() > 50)
            return "Password must be at most 50 characters";
        return null;
    }

    private void setPassword(User user, String rawPassword)
    {
        String msg = validatePassword(rawPassword);
        if (msg != null)
            throw new IllegalArgumentException(msg);
        user.setEncodedPassword(passwordEncoder.encode(rawPassword));
    }

    @Transactional
    public void addUser(String username, String rawPassword, String email)
    {
        User lastUser = userRepository.findTopByOrderByIdDesc();
        int id = lastUser == null ? 1 : lastUser.getId() + 1;
        User user = new User(id, username);
        setPassword(user, rawPassword);
        user.setEmail(email);
        userRepository.save(user);
        changeDetector.changed();
    }

    @Transactional
    public void saveUser(User user, String rawPassword)
    {
        setPassword(user, rawPassword);
        userRepository.save(user);
        changeDetector.changed();
    }

    public void setAuthenticationSupplier(Supplier<Authentication> authenticationSupplier)
    {
        this.authenticationSupplier = authenticationSupplier;
    }

    public User getCurrentUser()
    {
        String currentPrincipalName = authenticationSupplier.get().getName();
        User user = userRepository.findByUsername(currentPrincipalName);
        if (user == null)
            throw new IllegalStateException("User not found: " + currentPrincipalName);
        return user;
    }
}
