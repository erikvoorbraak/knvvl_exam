package org.knvvl.exam.spring;

import static org.knvvl.exam.spring.MvcConfig.SPRING_VIEW_URIS;

import java.io.IOException;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * URIs that match Vue routes (examples: "/exams", "/questions/1234") will be forwarded to "index.html"
 * so that they can be picked up by Vue's router.
 */
@Component
public class VueFilter implements Filter
{
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterchain) throws IOException, ServletException
    {
        if (isForSpring(request)) {
            filterchain.doFilter(request, response);
        }
        else {
            request.getRequestDispatcher("/index.html").forward(request, response);
        }
    }

    private boolean isForSpring(ServletRequest request)
    {
        if (!(request instanceof HttpServletRequest http)) {
            return true;
        }
        var uri = http.getRequestURI();
        return SPRING_VIEW_URIS.contains(uri) // See MvcConfig
            || uri.contains(".") // "/index.html", .js, .css, .png, .jpg, etc
            || uri.startsWith("/api"); // REST endpoints
    }
}