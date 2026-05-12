package kongju.pickmeal.core.family;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FamilyRepository extends JpaRepository<Family,Long> {
    boolean existsByInvitationCode(String invitationCode);
    Optional<Family> findByInvitationCode(String invitationCode);
}
