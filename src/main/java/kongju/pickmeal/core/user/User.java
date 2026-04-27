package kongju.pickmeal.core.user;

import org.springframework.data.annotation.LastModifiedDate;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import kongju.pickmeal.core.common.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 유저 정보를 담은 entity
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {
    @Column(nullable = false, unique = true)
    private String nickName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    @NotNull
    @Past(message = "생년월일은 미래일 수 없습니다.")
    private LocalDate birthDate;
    private Long familyId;
    @Column(nullable = false, unique = true)
    private String loginId;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String email;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    private Long pickCount;

    @Builder
    public User(String nickName, LocalDate birthDate, String loginId, String email, String password){
        this.nickName = nickName;
        this.birthDate = birthDate;
        this.loginId = loginId;
        this.email = email;
        this.password = password;
        this.role = UserRole.GUEST;
        this.pickCount = 0L;
    }
}
