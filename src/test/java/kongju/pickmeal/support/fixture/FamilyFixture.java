package kongju.pickmeal.support.fixture;

import kongju.pickmeal.core.family.Family;
import org.springframework.test.util.ReflectionTestUtils;

public class FamilyFixture {
    public static Family family(){
        return Family.create("Family", "1234asdf");

    }

    public static Family family(String familyName) {

        return Family.create(familyName, "1234asdf");
    }
}
