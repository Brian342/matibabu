package com.matibabu.backend.api.medicine;

import com.matibabu.backend.application.medicine.ListUnresolvedAtcMappingsUseCase;
import com.matibabu.backend.application.medicine.ResolveAtcMappingUseCase;
import com.matibabu.backend.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/*
 * Reviewing an auto-matched or unresolved KEML-to-ATC mapping is a
 * pharmacology judgment call, not a prescribing action, so this lives
 * under /api/admin rather than being exposed to every clinician: it
 * edits shared reference data (see V12's comment on the medicines
 * table), not a single patient's record. Gated to ADMIN/SUPER_ADMIN
 * the same way as the rest of /api/admin in SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/medicines")
public class MedicineAtcReviewController {

    private final ListUnresolvedAtcMappingsUseCase listUnresolvedAtcMappingsUseCase;
    private final ResolveAtcMappingUseCase resolveAtcMappingUseCase;

    public MedicineAtcReviewController(
            ListUnresolvedAtcMappingsUseCase listUnresolvedAtcMappingsUseCase,
            ResolveAtcMappingUseCase resolveAtcMappingUseCase
    ) {
        this.listUnresolvedAtcMappingsUseCase = listUnresolvedAtcMappingsUseCase;
        this.resolveAtcMappingUseCase = resolveAtcMappingUseCase;
    }

    @GetMapping("/needs-review")
    public List<MedicineResponse> needsReview() {
        return listUnresolvedAtcMappingsUseCase.list().stream()
                .map(MedicineResponse::from)
                .toList();
    }

    /*
     * The reviewer is taken from the authenticated session, same
     * reasoning as EncounterController.start(): who resolved a
     * mapping must not be something the client can spoof by passing
     * a different ID in the request body.
     */
    @PatchMapping("/{id}/atc-mapping")
    public ResponseEntity<MedicineResponse> resolve(
            @PathVariable UUID id,
            @RequestBody ResolveAtcMappingRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        UUID reviewerId = principal.getClinician().getId();

        var medicine = switch (request.action()) {
            case CONFIRM -> {
                if (request.atcCode() == null || request.atcCode().isBlank()) {
                    yield null;
                }
                yield resolveAtcMappingUseCase.confirm(id, request.atcCode(), reviewerId);
            }
            case MARK_UNMAPPED -> resolveAtcMappingUseCase.markUnmapped(id, reviewerId);
        };

        if (medicine == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(MedicineResponse.from(medicine));
    }
}
