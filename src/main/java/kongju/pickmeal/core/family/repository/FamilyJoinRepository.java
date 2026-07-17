package kongju.pickmeal.core.family.repository;

import java.util.List;
import java.util.Optional;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.family.ApplyStatus;
import kongju.pickmeal.core.family.FamilyJoinRequest;


public interface FamilyJoinRepository {

    boolean checkPendingRequest(User user, Family family, ApplyStatus status);

    List<FamilyJoinRequest> findAllByFamilyAndStatus(Family family, ApplyStatus status);

    Optional<FamilyJoinRequest> findByIdAndFamily(Long id, Long family);

    FamilyJoinRequest save(FamilyJoinRequest familyJoinRequest);

    Optional<FamilyJoinRequest> findById(Long id);
}
