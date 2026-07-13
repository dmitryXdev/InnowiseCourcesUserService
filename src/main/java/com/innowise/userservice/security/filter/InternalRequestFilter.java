package com.innowise.userservice.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class InternalRequestFilter extends OncePerRequestFilter {
    @Value("${internal.secret.header}")
    private String internalSecretHeader;
    @Value("${internal.secret}")
    private String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!secret.equals(request.getHeader(internalSecretHeader))) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "internal-service",
                null,
                Collections.emptyList()
        );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
