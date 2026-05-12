package kongju.pickmeal.core.family;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyApplyRepository extends JpaRepository<JoinApply, Long> {
    boolean existsByUserId(Long userId);
}
