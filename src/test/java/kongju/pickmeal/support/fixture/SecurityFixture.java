package kongju.pickmeal.support.fixture;

import kongju.pickmeal.api.security.CustomUserDetails;
import kongju.pickmeal.core.user.type.UserRole;

public class SecurityFixture {
    private SecurityFixture() {
    }

    public static CustomUserDetails mockLoginUser(Long userId, UserRole role) {
        return CustomUserDetails.builder()
                .id(userId)
                .role(role)
                .build();
    }

    public static CustomUserDetails mockMember() {
        return mockLoginUser(1L, UserRole.MEMBER);
    }

    public static CustomUserDetails mockLeader() {
        return mockLoginUser(1L, UserRole.LEADER);
    }

    public static CustomUserDetails mockGuest() {
        return mockLoginUser(1L, UserRole.GUEST);
    }
}
