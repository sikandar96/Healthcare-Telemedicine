package com.health.care.security;

import java.io.IOException;

import com.health.care.config.JwtProperties;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final MongoUserDetailsService mongoUserDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, JwtProperties jwtProperties, MongoUserDetailsService mongoUserDetailsService) {
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.mongoUserDetailsService = mongoUserDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(jwtProperties.getHeader());
        if (!StringUtils.hasText(header) || !header.startsWith(jwtProperties.getPrefix() + " ")) {
            logger.debug("No JWT token found in request header");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring((jwtProperties.getPrefix() + " ").length());
        if (!jwtService.isTokenValid(token)) {
            logger.warn("Invalid or expired JWT token detected");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtService.extractUsername(token);
            if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.debug("Authenticating user: {} with JWT token", username);
                UserDetails userDetails = mongoUserDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("User {} authenticated successfully via JWT token", username);
            }
        } catch (Exception e) {
            logger.error("Failed to authenticate user with JWT token", e);
        }

        filterChain.doFilter(request, response);
    }
}
