package kongju.pickmeal.api.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import kongju.pickmeal.core.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import kongju.pickmeal.core.user.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Component
@RequiredArgsConstructor // final이 붙은 필드를 모아서 생성자를 자동으로 만들어줌
public class JwtTokenFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 토큰 꺼내기
        String header = request.getHeader("Authorization");

        // 토큰 x, Bearer로 시작하지 않으면 통과
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        // Bearer뒷부분 토큰값 추출
        String token = header.split(" ")[1];

        // 토큰 유효확인, 안에 담긴 User id가져옴
        // DB 유저 찾기
        jwtService.getSubFromAccessToken(token)
                .flatMap(id -> userRepository.findById(Long.valueOf(id)))
                .ifPresent(user -> {
                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                    );
                    // 시큐리티가 이해가능한 인증 티켓 생성
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, authorities);
                    // 임시 저장
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
        // 필터 처리 완료
        filterChain.doFilter(request, response);

    }
}
