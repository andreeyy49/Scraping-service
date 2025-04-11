package webscraping.authservice.security.jjwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import webscraping.authservice.security.UserDetailsServiceImpl;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            log.debug("Received Authorization Header: {}", request.getHeader(HttpHeaders.AUTHORIZATION));
            String jwtToken = getToken(request);
            log.debug("Extracted JWT Token: {}", jwtToken);

            if (jwtToken != null && jwtUtils.validate(jwtToken)) {
                String token = jwtUtils.getEmail(jwtToken);
                log.debug("Extracted email from token: {}", token);
                System.out.println(token + " token");

                UserDetails userDetails = userDetailsService.loadUserById(UUID.fromString(token));
                log.debug("Loaded UserDetails: {}", userDetails);

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                log.debug("Authentication set in SecurityContext");
            } else {
                log.warn("JWT Token is null or invalid:{}", request.getHeader(HttpHeaders.AUTHORIZATION));
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String getToken(HttpServletRequest request) {
        String headerAuth = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(headerAuth)) {
            log.warn("Authorization header is missing in request to {}", request.getRequestURI());
            return null;
        }

        log.debug("Authorization header received: {}", headerAuth);

        if (headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);
            log.debug("Extracted token: {}", token);
            return token;
        }

        log.warn("Authorization header is malformed: {}", headerAuth);
        return null;
    }

}
