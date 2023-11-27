package com.epam.esm.filter;


import com.epam.esm.model.AuthenticatedUser;
import com.epam.esm.utils.openfeign.AuthFeignClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

import static com.epam.esm.utils.AuthConstants.*;

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
            AuthenticatedUser user = authClient.getAuthenticatedUserFromJwt(bearerToken).getBody();
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(user, null,
                            Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            setException(response, e);
        }
    }

    private void setException(HttpServletResponse response, HttpStatusCodeException e) throws IOException {
        int statusCode = e.getStatusCode().value();
        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(getJsonProblem(e, statusCode));
    }

    public String getJsonProblem(HttpStatusCodeException e, int statusCode) {
        JSONObject errorJson = new JSONObject();
        errorJson.put(TITLE, AUTHENTICATION_EXCEPTION);
        errorJson.put(STATUS, statusCode);
        errorJson.put(DETAIL, e.getStatusText());
        return errorJson.toString();
    }
}