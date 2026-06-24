package kongju.pickmeal.core.diet;

import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Builder;
import lombok.AccessLevel;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.diet.type.DietGenerationStatus;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DietGeneration {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    private LocalDate startDate;
    private LocalDate endDate;

    private Integer dailyMealCount;

    @Enumerated(EnumType.STRING)
    private DietGenerationStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder(access = AccessLevel.PRIVATE)
    private DietGeneration(Family family, LocalDate startDate, LocalDate endDate, Integer dailyMealCount,DietGenerationStatus status) {
        this.family = family;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dailyMealCount = dailyMealCount;
        this.status = status;
    }

    public static DietGeneration createPending(
            Family family,
            LocalDate startDate,
            LocalDate endDate,
            Integer dailyMealCount
    ) {
        return DietGeneration.builder()
                .family(family)
                .startDate(startDate)
                .endDate(endDate)
                .dailyMealCount(dailyMealCount)
                .status(DietGenerationStatus.PENDING)
                .build();
    }

    public void processing() {
        this.status = DietGenerationStatus.PROCESSING;
    }

    public void completed() {
        this.status = DietGenerationStatus.COMPLETED;
    }

    public void failed() {
        this.status = DietGenerationStatus.FAILED;
    }

}
