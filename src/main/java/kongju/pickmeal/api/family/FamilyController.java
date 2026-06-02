package kongju.pickmeal.api.family;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import kongju.pickmeal.application.family.data.*;
import kongju.pickmeal.common.ApiResponse.ApiResponse;
import kongju.pickmeal.api.security.CustomUserDetails;
import kongju.pickmeal.application.family.FamilyService;


@RestController
@RequestMapping("/api/v1/families")
@RequiredArgsConstructor
public class FamilyController {
    private final FamilyService familyService;

    @PostMapping
    public ResponseEntity<ApiResponse<FamilyDto.CreateResponse>> createFamily(
            @RequestBody @Valid FamilyDto.CreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        FamilyDto.CreateResponse response = familyService.createFamily(request, userDetails.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/applications")
    public ResponseEntity<ApiResponse<Void>> joinRequestFamily(
            @RequestBody @Valid FamilyJoinRequestDto.CreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        familyService.joinRequest(request, userDetails.getId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }

    @GetMapping("/me/applications")
    @PreAuthorize("hasRole('LEADER')")
    public ResponseEntity<ApiResponse<List<FamilyJoinRequestDto.Summary>>> requestListFamily(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<FamilyJoinRequestDto.Summary> joinRequestSummary = familyService.loadJoinRequestSummary(userDetails.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(joinRequestSummary));
    }

    @PostMapping("/me/applications/{requestId}")
    @PreAuthorize("hasRole('LEADER')")
    public ResponseEntity<ApiResponse<FamilyJoinRequestDto.ProcessResponse>> processRequestFamily(
            @PathVariable Long requestId,
            @RequestBody @Valid FamilyJoinRequestDto.ProcessRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FamilyJoinRequestDto.ProcessResponse response = familyService.processJoinRequest(requestId, request, userDetails.getId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @PatchMapping("/me/invitation-code")
    @PreAuthorize("hasRole('LEADER')")
    public ResponseEntity<ApiResponse<FamilyInvitationDto.CodeResponse>> updateInvitationCode(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FamilyInvitationDto.CodeResponse response = familyService.createInvitationCode(userDetails.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/me/members")
    @PreAuthorize("hasRole('LEADER') or hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<List<FamilyMemberDto.ListItem>>> getFamilyMembers(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<FamilyMemberDto.ListItem> listItems = familyService.getMembers(userDetails.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(listItems));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('LEADER')")
    public ResponseEntity<ApiResponse<Void>> disbandMyFamily(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        familyService.disbandFamily(userDetails.getId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }

    @DeleteMapping("/me/members/{userId}")
    @PreAuthorize("hasRole('LEADER')")
    public ResponseEntity<ApiResponse<FamilyMemberDto.KickResponse>> kickFamilyMember(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FamilyMemberDto.KickResponse response = familyService.kickMember(userId, userDetails.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @DeleteMapping("/me/membership")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<Void>> leaveFamilyMember(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        familyService.leaveMember(userDetails.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }

    @PatchMapping("/me/picks/config")
    @PreAuthorize("hasRole('LEADER')")
    public ResponseEntity<ApiResponse<FamilyPickDto.ConfigResponse>> pickAllocation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid FamilyPickDto.UpdateConfigRequest request
    ) {
        FamilyPickDto.ConfigResponse response = familyService.pickConfig(userDetails.getId(), request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/me/picks/reset")
    @PreAuthorize("hasRole('LEADER')")
    public ResponseEntity<ApiResponse<FamilyPickDto.ResetResponse>> resetAllocation(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FamilyPickDto.ResetResponse response = familyService.resetConfig(userDetails.getId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }
}
