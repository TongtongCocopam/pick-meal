package kongju.pickmeal.infrastructure.repository.jpa.family;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.family.ApplyStatus;
import kongju.pickmeal.core.family.FamilyJoinRequest;
import kongju.pickmeal.core.family.repository.FamilyJoinRepository;


@Repository
@RequiredArgsConstructor
public class FamilyJoinRepositoryAdapter implements FamilyJoinRepository {

    private final FamilyJoinJpaRepository familyJoinJpaRepository;

    @Override
    public boolean checkPendingRequest(User user, Family family, ApplyStatus status) {
        return familyJoinJpaRepository.checkPendingRequest(user, family, status);
    }

    @Override
    public List<FamilyJoinRequest> findAllByFamilyAndStatus(Family family, ApplyStatus status) {
        return familyJoinJpaRepository.findAllByFamilyAndStatus(family, status);
    }

    @Override
    public FamilyJoinRequest save(FamilyJoinRequest familyJoinRequest) {
        return familyJoinJpaRepository.save(familyJoinRequest);
    }

    @Override
    public Optional<FamilyJoinRequest> findById(Long id) {
        return familyJoinJpaRepository.findById(id);
    }
}
