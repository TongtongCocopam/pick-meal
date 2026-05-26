package kongju.pickmeal.support.fixture;

import kongju.pickmeal.core.family.Family;

public class FamilyFixture {
    public static Family family(){
        return Family.builder()
                .familyName("Family")
                .leaderId(1L)
                .invitationCode("1234asdf")
                .build();
    }
}
