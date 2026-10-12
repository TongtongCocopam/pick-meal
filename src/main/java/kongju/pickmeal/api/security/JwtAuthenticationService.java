package kongju.pickmeal.api.security;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.service.JwtService;
import kongju.pickmeal.core.user.repository.UserRepository;


@Service
@RequiredArgsConstructor
public class JwtAuthenticationService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;

    public Optional<Authentication> authenticate(String authorizationHeader) {

        return jwtService.extractAccessToken(authorizationHeader)
                .filter(token -> !tokenBlacklistService.isBlacklisted(token))
                .flatMap(jwtService::extractSubjectFromAccessToken)
                .flatMap(userRepository::findById)
                .map(this::createAuthentication);
    }

    private Authentication createAuthentication(User user) {

        CustomUserDetails principal = CustomUserDetails.builder()
                .id(user.getId())
                .role(user.getRole())
                .build();

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
    }
}