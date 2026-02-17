package com.ayansh.Backend.Security.JWT;


import com.ayansh.Backend.Security.Service.UserDetailServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

    @Component
    public class AuthTokenFilter extends OncePerRequestFilter {

        @Autowired
        private JwtUtils jwtUtils;

        @Autowired
        private UserDetailServiceImpl userDetailsService;

        private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {

            String requestURI = request.getRequestURI();

            // Skip JWT validation for permitAll paths
            if (requestURI.startsWith("/api/auth/") || requestURI.startsWith("/api/public/") ||
                    requestURI.startsWith("/v3/api-docs/") || requestURI.startsWith("/swagger-ui/") ||
                    requestURI.startsWith("/images/") || requestURI.equals("/")) {
                logger.debug("Skipping JWT validation for public path: {}", requestURI);
                filterChain.doFilter(request, response);
                return;
            }

            logger.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());

            try {
                // Log all cookies
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        logger.debug("Cookie found: {} = {}", cookie.getName(), cookie.getValue());
                    }
                }

                // Log Authorization header
                String authHeader = request.getHeader("Authorization");
                logger.debug("Authorization header: {}", authHeader);

                String jwt = parseJwt(request);
                logger.debug("Parsed JWT: {}", (jwt != null ? jwt.substring(0, Math.min(20, jwt.length())) + "..." : "null"));

                if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    logger.debug("Valid JWT for user: {}", username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    logger.debug("Authentication set for user: {}", username);
                } else {
                    logger.debug("JWT is null or invalid");
                    if (jwt != null) {
                        // Try to validate to see what error we get
                        try {
                            jwtUtils.validateJwtToken(jwt);
                        } catch (Exception e) {
                            logger.error("JWT validation error: {}", e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Cannot set User Authentication", e);
            }

            filterChain.doFilter(request, response);
        }

        private String parseJwt(HttpServletRequest request) {
            String jwtFromCookie = jwtUtils.getJwtFromCookie(request);
            logger.debug("JWT from cookie: {}", (jwtFromCookie != null ? "present" : "null"));

            String header = jwtUtils.getJwtFromHeader(request);
            logger.debug("JWT from header: {}", (header != null ? "present" : "null"));

            if(jwtFromCookie != null){
                return jwtFromCookie;
            }
            return header;
        }
    }

