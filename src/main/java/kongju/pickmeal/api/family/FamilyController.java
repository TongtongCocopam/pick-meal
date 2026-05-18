package kongju.pickmeal.api.family;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.common.ApiResponse.ApiResponse;
import kongju.pickmeal.application.family.FamilyService;
import kongju.pickmeal.application.family.data.FamilyDto;
import kongju.pickmeal.application.family.data.FamilyInvitationDto;
import kongju.pickmeal.application.family.data.FamilyJoinRequestDto;


@RestController
@RequestMapping("/api/v1/families")
@RequiredArgsConstructor
public class FamilyController {
    private final FamilyService familyService;

    @PostMapping
    public ResponseEntity<ApiResponse<FamilyDto.CreateResponse>> createFamily(
            @RequestBody @Valid FamilyDto.CreateRequest request,
            @AuthenticationPrincipal User user
    ) {

        FamilyDto.CreateResponse response = familyService.createFamily(request, user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/applications")
    public ResponseEntity<ApiResponse<Void>> joinRequestFamily(
            @RequestBody @Valid FamilyJoinRequestDto.CreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        familyService.joinRequest(request, user);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }

    @GetMapping("/me/applications")
    @PreAuthorize("hasRole('LEADER')")
    public ResponseEntity<ApiResponse<List<FamilyJoinRequestDto.Summary>>> requestListFamily(
            @AuthenticationPrincipal User user
    ) {
        List<FamilyJoinRequestDto.Summary> joinRequestSummary = familyService.loadJoinRequestSummary(user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(joinRequestSummary));
    }

    @PostMapping("/me/applications/{requestId}")
    @PreAuthorize("hasRole('LEADER')")
    public ResponseEntity<ApiResponse<FamilyJoinRequestDto.ProcessResponse>> processRequestFamily(
            @PathVariable Long requestId,
            @RequestBody @Valid FamilyJoinRequestDto.ProcessRequest request,
            @AuthenticationPrincipal User user
    ) {
        FamilyJoinRequestDto.ProcessResponse response = familyService.processJoinRequest(requestId, request, user);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @PatchMapping("/me/invitation-code")
    @PreAuthorize("hasRole('LEADER')")
    public ResponseEntity<ApiResponse<FamilyInvitationDto.CodeResponse>> updateInvitationCode(@AuthenticationPrincipal User user) {
        FamilyInvitationDto.CodeResponse response = familyService.createInvitationCode(user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }


}
