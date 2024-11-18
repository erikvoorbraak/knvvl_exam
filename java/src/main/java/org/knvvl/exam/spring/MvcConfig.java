package org.knvvl.exam.spring;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer
{
    static final List<String> SPRING_VIEW_URIS = new ArrayList<>();
    private static final String URI_ROOT = uri("/");
    private static final String URI_LOGIN = uri("/login");
    private static final String URI_HOME = uri("/home");

    private static String uri(String uri)  {
        SPRING_VIEW_URIS.add(uri);
        return uri;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController(URI_ROOT).setViewName("login");
        registry.addViewController(URI_HOME).setViewName("home");
        registry.addViewController(URI_LOGIN).setViewName("login");
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

}