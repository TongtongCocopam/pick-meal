package kongju.pickmeal.core.family;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface FamilyRepository extends JpaRepository<Family,Long> {
    boolean existsByInvitationCode(String invitationCode);
    Optional<Family> findByInvitationCode(String invitationCode);
}
