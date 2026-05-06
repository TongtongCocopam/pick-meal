package kongju.pickmeal.core.family;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyRepository extends JpaRepository<Family,Long> {
    boolean existsByInvitationCode(String invitationCode);
}
