package kongju.pickmeal.core.family.repository;

import java.util.Optional;

import kongju.pickmeal.core.family.Family;


public interface FamilyRepository {
    boolean existsByInvitationCode(String invitationCode);

    Optional<Family> findByInvitationCode(String invitationCode);

    Family save(Family family);

    Optional<Family> findById(Long familyId);

    void delete(Family family);

    Optional<Family> findByIdForUpdate(Long familyId);
}
