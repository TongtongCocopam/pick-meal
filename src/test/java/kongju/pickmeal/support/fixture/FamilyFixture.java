package kongju.pickmeal.support.fixture;

import kongju.pickmeal.core.family.Family;
import org.springframework.test.util.ReflectionTestUtils;

public class FamilyFixture {
    public static Family family(){
        return Family.builder()
                .familyName("Family")
                .invitationCode("1234asdf")
                .build();
    }

    public static Family familyWithId(String familyName, Long id) {
        Family family = Family.builder()
                .familyName(familyName)
                .invitationCode("1234asdf")
                .build();

        ReflectionTestUtils.setField(family, "id", id);
        return family;
    }
}
