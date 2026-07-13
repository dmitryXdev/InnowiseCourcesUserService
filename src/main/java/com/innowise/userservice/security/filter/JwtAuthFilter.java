package com.innowise.userservice.security.filter;

import com.innowise.userservice.dto.TokenValidationRequestDto;
import com.innowise.userservice.dto.TokenValidationResponseDto;
import com.innowise.userservice.feign.AuthClient;
import com.innowise.userservice.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final AuthClient authClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        TokenValidationResponseDto validationResponse = authClient.validate(new TokenValidationRequestDto(token));

        if (validationResponse == null || !validationResponse.isValid() || validationResponse.getUserId() == null) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return;
        }

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + validationResponse.getRole()));

        UserPrincipal principal = new UserPrincipal(validationResponse.getUserId(), validationResponse.getRole());

        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
