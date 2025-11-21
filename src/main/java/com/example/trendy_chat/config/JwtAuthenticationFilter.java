package com.example.trendy_chat.config;

import com.example.trendy_chat.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Skip JWT validation for public endpoints
        String path = request.getRequestURI();
        if (path.startsWith("/trendy/auth/") || 
            path.startsWith("/oauth2/") || 
            path.startsWith("/login") ||
            path.equals("/") ||
            path.startsWith("/register") ||
            path.startsWith("/static/") ||
            path.startsWith("/images/") ||
            path.startsWith("/ws-chat/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Get Authorization header
        String authHeader = request.getHeader("Authorization");
        
        // Skip if no token or not Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // Extract token
            String token = authHeader.substring(7);
            
            // Validate token
            if (jwtService.validateToken(token)) {
                // Extract user info from token
                String userId = jwtService.extractUserId(token);
                String email = jwtService.extractEmail(token);
                
                // Create authentication object
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Set authentication in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // Token invalid - clear context
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }
}