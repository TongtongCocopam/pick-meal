package kongju.pickmeal.core.menu;

import lombok.Getter;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import kongju.pickmeal.core.common.BaseTimeEntity;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ingredient extends BaseTimeEntity {
    @Column(unique = true, nullable = false, length = 100)
    private String name;

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
