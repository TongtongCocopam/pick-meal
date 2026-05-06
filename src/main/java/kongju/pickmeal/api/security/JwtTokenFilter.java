package kongju.pickmeal.api.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import kongju.pickmeal.core.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import kongju.pickmeal.core.user.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@RequiredArgsConstructor // final이 붙은 필드를 모아서 생성자를 자동으로 만들어줌
public class JwtTokenFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

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

        String isLogout = redisTemplate.opsForValue().get("blacklist:" + token);
        if (isLogout != null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 토큰 유효확인, 안에 담긴 User id가져옴
        // DB 유저 찾기
        jwtService.extractSubject(token)
                .flatMap(id -> userRepository.findById(Long.valueOf(id)))
                // 데이터가 있을 때만 실행
                .ifPresent(user -> {
                    // 스프링 시큐리티 권한
                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                    );
                    // 시큐리티가 이해가능한 인증 티켓 생성
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, authorities);
                    // 저장
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
        // 필터 처리 완료
        filterChain.doFilter(request, response);

    }
}
