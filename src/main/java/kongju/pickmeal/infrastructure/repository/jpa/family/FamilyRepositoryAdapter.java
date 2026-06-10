package kongju.pickmeal.infrastructure.repository.jpa.family;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.family.repository.FamilyRepository;


@Repository
@RequiredArgsConstructor
public class FamilyRepositoryAdapter implements FamilyRepository {
    private final FamilyJpaRepository familyJpaRepository;

    @Override
    public boolean existsByInvitationCode(String invitationCode) {
        return familyJpaRepository.existsByInvitationCode(invitationCode);
    }

    @Override
    public Optional<Family> findByInvitationCode(String invitationCode) {
        return familyJpaRepository.findByInvitationCode(invitationCode);
    }

    @Override
    public Family save(Family family) {
        return familyJpaRepository.save(family);
    }

    @Override
    public Optional<Family> findById(Long familyId) {
        return familyJpaRepository.findById(familyId);
    }

    @Override
    public void delete(Family family) {
        familyJpaRepository.delete(family);
    }
}
