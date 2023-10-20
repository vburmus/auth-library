package com.epam.esm.filter;


import com.epam.esm.utils.openfeign.AuthFeignClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static com.epam.esm.utils.AuthConstants.AUTHENTICATION_BEARER_TOKEN;

@RequiredArgsConstructor
public class ServiceAuthenticationFilter extends OncePerRequestFilter {
    private final AuthFeignClient authClient;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith(AUTHENTICATION_BEARER_TOKEN)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String role = authClient.getRole(bearerToken).getBody();
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(null, null, List.of(new SimpleGrantedAuthority(role)));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            response.setStatus(e.getStatusCode().value());
            response.getWriter().write(e.getMessage());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        }
    }
}