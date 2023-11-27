package com.epam.esm.filter;

import com.epam.esm.model.Role;
import com.epam.esm.model.AuthenticatedUser;
import com.epam.esm.utils.openfeign.AuthFeignClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ServiceAuthenticationFilterTest {
    @Mock
    private AuthFeignClient authClient;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @InjectMocks
    private ServiceAuthenticationFilter authenticationFilter;

    @Mock
    private FilterChain filterChain;

    @Test
    void testFilterNoBearerToken() throws ServletException, IOException {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        authenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testFilterInvalidBearerToken() throws ServletException, IOException {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("InvalidToken");
        authenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testFilterValidBearerTokenSuccessfulAuthentication() throws ServletException, IOException {
        String validToken = "Bearer BearerValidToken";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(validToken);

        AuthenticatedUser userDTO = new AuthenticatedUser();
        userDTO.setRole(Role.USER);
        when(authClient.getAuthenticatedUserFromJwt(validToken)).thenReturn(ResponseEntity.ok(userDTO));

        authenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(userDTO, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        AuthenticatedUser authUser = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals(Role.USER, authUser.getRole());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testFilterAuthenticationErrorHttpClientErrorException() throws ServletException, IOException {
        String validToken = "Bearer BearerValidToken";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(validToken);

        when(authClient.getAuthenticatedUserFromJwt(validToken))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(writer).write("{\"detail\":\"UNAUTHORIZED\",\"title\":\"Authentication Exception\",\"status\":401}");
    }

    @Test
    void testFilterAuthenticationErrorHttpServerErrorException() throws ServletException, IOException {
        String validToken = "Bearer BearerValidToken";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(validToken);

        when(authClient.getAuthenticatedUserFromJwt(validToken))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(writer).write("{\"detail\":\"INTERNAL_SERVER_ERROR\",\"title\":\"Authentication Exception\"," +
                "\"status\":500}");
    }
}
