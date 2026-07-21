package kongju.pickmeal.core.user;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.*;
import jakarta.persistence.*;
import org.springframework.data.annotation.LastModifiedDate;

import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.user.type.UserRole;
import kongju.pickmeal.core.common.BaseTimeEntity;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;

/**
 * 유저 정보를 담은 entity
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {
    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, unique = true, length = 15)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    @Builder(access = AccessLevel.PRIVATE)
    private User(String nickname, LocalDate birthDate, String loginId, String email, String password){
        this.nickname = nickname;
        this.birthDate = birthDate;
        this.loginId = loginId;
        this.email = email;
        this.password = password;
        this.role = UserRole.GUEST;
    }

    public static User create(String nickname, LocalDate birthDate, String loginId, String email, String password){
        return User.builder()
                .nickname(nickname)
                .birthDate(birthDate)
                .loginId(loginId)
                .email(email)
                .password(password)
                .build();
    }

    public void joinFamilyLeader(Family family){
        validateNotJoinedFamily();

        this.family = family;
        this.role = UserRole.LEADER;
    }

    public void joinFamilyMember(Family family){
        validateNotJoinedFamily();

        this.family = family;
        this.role = UserRole.MEMBER;
    }

    private void validateNotJoinedFamily() {
        if (family != null) {
            throw new BusinessException(ErrorCode.ALREADY_HAS_FAMILY);
        }
    }

    public void leaveFamily() {
        this.role = UserRole.GUEST;
        this.family = null;
    }

    public void updateNickname(String nickname){
        this.nickname = nickname;
    }

    public void updateBirthDate(LocalDate birthDate){
        this.birthDate = birthDate;
    }

    public void updatePassword(String password){
        this.password = password;
    }
}
