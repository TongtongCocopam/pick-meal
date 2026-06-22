package kongju.pickmeal.api.diet;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import kongju.pickmeal.api.security.CustomUserDetails;
import kongju.pickmeal.application.diet.AiDietService;
import kongju.pickmeal.common.ApiResponse.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import kongju.pickmeal.infrastructure.external.ai.data.DietGenerationDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


@RestController
@RequestMapping("/api/v1/diets")
@RequiredArgsConstructor
public class AiDietController {
    private final AiDietService aiDietService;

    @PostMapping("/ai-generate")
    public ResponseEntity<ApiResponse<DietGenerationDto.GenerateResponse>> generate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid DietGenerationDto.GenerateRequest request
    ) {
        DietGenerationDto.GenerateResponse response = aiDietService.generate(userDetails.id(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
