package com.matibabu.backend.api.referral;

import com.matibabu.backend.application.referral.CancelReferralUseCase;
import com.matibabu.backend.application.referral.CompleteReferralUseCase;
import com.matibabu.backend.application.referral.CreateReferralUseCase;
import com.matibabu.backend.application.referral.GetReferralUseCase;
import com.matibabu.backend.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class ReferralController {

    private final CreateReferralUseCase createReferralUseCase;
    private final GetReferralUseCase getReferralUseCase;
    private final CompleteReferralUseCase completeReferralUseCase;
    private final CancelReferralUseCase cancelReferralUseCase;

    public ReferralController(
            CreateReferralUseCase createReferralUseCase,
            GetReferralUseCase getReferralUseCase,
            CompleteReferralUseCase completeReferralUseCase,
            CancelReferralUseCase cancelReferralUseCase
    ) {
        this.createReferralUseCase = createReferralUseCase;
        this.getReferralUseCase = getReferralUseCase;
        this.completeReferralUseCase = completeReferralUseCase;
        this.cancelReferralUseCase = cancelReferralUseCase;
    }

    /*
     * The referring clinician is taken from the authenticated
     * session, same reasoning as EncounterController.start(): who
     * made a referral must not be something the client can spoof by
     * passing a different ID.
     */
    @PostMapping("/api/encounters/{encounterId}/referrals")
    @ResponseStatus(HttpStatus.CREATED)
    public ReferralResponse create(
            @PathVariable UUID encounterId,
            @RequestBody CreateReferralRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return ReferralResponse.from(
                createReferralUseCase.create(
                        encounterId,
                        principal.getClinician().getId(),
                        request.diagnosisId(),
                        request.reason(),
                        request.urgency(),
                        request.receivingFacility(),
                        request.department()
                )
        );
    }

    @GetMapping("/api/referrals/{id}")
    public ReferralResponse getById(@PathVariable UUID id) {
        return ReferralResponse.from(getReferralUseCase.getById(id));
    }

    @GetMapping("/api/encounters/{encounterId}/referrals")
    public List<ReferralResponse> listByEncounter(@PathVariable UUID encounterId) {
        return getReferralUseCase.listByEncounter(encounterId).stream()
                .map(ReferralResponse::from)
                .toList();
    }

    @GetMapping("/api/patients/{patientId}/referrals")
    public List<ReferralResponse> listByPatient(@PathVariable UUID patientId) {
        return getReferralUseCase.listByPatient(patientId).stream()
                .map(ReferralResponse::from)
                .toList();
    }

    /*
     * Who resolved the referral is taken from the authenticated
     * session, not the request body, for the same reason the
     * referring clinician is: it should not be spoofable by the
     * client.
     */
    @PostMapping("/api/referrals/{id}/complete")
    public ReferralResponse complete(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return ReferralResponse.from(
                completeReferralUseCase.complete(id, principal.getClinician().getId())
        );
    }

    @PostMapping("/api/referrals/{id}/cancel")
    public ReferralResponse cancel(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return ReferralResponse.from(
                cancelReferralUseCase.cancel(id, principal.getClinician().getId())
        );
    }
}
