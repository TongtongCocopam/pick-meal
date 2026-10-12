package kongju.pickmeal.api.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.ServletException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;


@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtAuthenticationService jwtAuthenticationService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        jwtAuthenticationService.authenticate(authorization)
                .ifPresent(authentication ->
                        SecurityContextHolder.getContext()
                                .setAuthentication(authentication)
                );

        filterChain.doFilter(request, response);
    }
}