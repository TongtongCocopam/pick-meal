package kongju.pickmeal.api.family;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.common.ApiResponse.ApiResponse;
import kongju.pickmeal.application.family.FamilyService;
import kongju.pickmeal.application.family.data.FamiliesRequest;
import kongju.pickmeal.application.family.data.FamiliesResponse;


@RestController
@RequestMapping("/api/v1/families")
@RequiredArgsConstructor
public class FamilyController {
    private final FamilyService familyService;

    @PostMapping
    public ResponseEntity<ApiResponse<FamiliesResponse.Create>> createFamily(
            @RequestBody @Valid FamiliesRequest.Create request,
            @AuthenticationPrincipal User user
    ) {

        FamiliesResponse.Create response = familyService.createFamily(request, user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<FamiliesResponse.Create>> applyFamily(
            @RequestBody @Valid FamiliesRequest.Apply request,
            @AuthenticationPrincipal User user
    ) {
        familyService.apply(request, user);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }
}
