package kongju.pickmeal.application.family;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyString;
import static org.assertj.core.api.Assertions.assertThat;

import kongju.pickmeal.core.family.repository.FamilyRepository;


@ExtendWith(MockitoExtension.class)
public class InvitationCodeGeneratorTest {
    @Mock
    private FamilyRepository familyRepository;
    @InjectMocks
    private InvitationCodeGenerator invitationCodeGenerator;

    @Test
    @DisplayName("초대코드 생성")
    void should_generate_code_success_when_generate() {
        // when
        given(familyRepository.existsByInvitationCode(anyString())).willReturn(false);
        String code = invitationCodeGenerator.generateUniqueCode();
        // then
        assertThat(code).matches("^[A-Z0-9]{8}");
    }

}
