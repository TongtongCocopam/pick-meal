package kongju.pickmeal.core.menu;

import lombok.Getter;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Size;

import kongju.pickmeal.core.common.BaseTimeEntity;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ingredient extends BaseTimeEntity {
    @Column(unique = true, nullable = false)
    @Size(min = 1, max = 100)
    String name;

    @Builder(access = AccessLevel.PRIVATE)
    public Ingredient(String name) {
        this.name = name;
    }

    public static Ingredient create(String name) {
        return Ingredient.builder()
                .name(name)
                .build();
    }
}
