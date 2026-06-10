package kongju.pickmeal.infrastructure.repository.jpa.family;

import java.util.Optional;

import kongju.pickmeal.core.family.Family;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FamilyJpaRepository  extends JpaRepository<Family,Long> {
    boolean existsByInvitationCode(String invitationCode);
    Optional<Family> findByInvitationCode(String invitationCode);
}
