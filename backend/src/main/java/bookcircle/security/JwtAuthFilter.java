package bookcircle.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring("Bearer ".length()).trim();
            try {
                Jws<Claims> jws = jwtService.parse(token);
                Claims c = jws.getBody();
                String email = (String) c.get("email");
                String role = (String) c.get("role");

                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                var principal = new JwtPrincipal(Long.parseLong(c.getSubject()), email, role);

                var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT authenticated userId={} role={}", principal.userId(), principal.role());
            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
                log.warn("JWT authentication failed for {} {}: {}",
                        request.getMethod(),
                        request.getRequestURI(),
                        ex.getClass().getSimpleName());
            }
        }

        filterChain.doFilter(request, response);
    }
}
