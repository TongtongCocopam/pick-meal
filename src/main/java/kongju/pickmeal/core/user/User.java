package kongju.pickmeal.core.user;

import java.time.LocalDate;
import java.time.LocalDateTime;

import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.user.type.UserRole;
import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.LastModifiedDate;

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
    @Column(nullable = false, unique = true)
    private String nickname;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    @NotNull
    @Past(message = "생년월일은 미래일 수 없습니다.")
    private LocalDate birthDate;
    @Column(nullable = false, unique = true)
    private String loginId;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String email;
    @Setter
    private Long pickCount;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    @Builder
    public User(String nickname, LocalDate birthDate, String loginId, String email, String password){
        this.nickname = nickname;
        this.birthDate = birthDate;
        this.loginId = loginId;
        this.email = email;
        this.password = password;
        this.role = UserRole.GUEST;
        this.pickCount = 0L;
    }

    public void joinFamilyLeader(Family family){
        if(this.family != null){
            throw new BusinessException(ErrorCode.ALREADY_HAS_FAMILY);
        }
        this.family = family;
        this.role = UserRole.READER;
    }

    public void joinFamilyMember(Family family){
        if(this.family != null){
            throw new BusinessException(ErrorCode.ALREADY_HAS_FAMILY);
        }
        this.family = family;
        this.role = UserRole.MEMBER;
    }

    public void deleteFamilyLeader(){
        this.role = UserRole.GUEST;
        this.family = null;
    }

    public void deleteFamilyMember(){
        this.role = UserRole.GUEST;
        this.family = null;
    }

    public void updateNickname(String nickname){
        this.nickname = nickname;
    }

    public void updateBirthDate(LocalDate birthDate){
        this.birthDate = birthDate;
    }
}
