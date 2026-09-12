package com.matibabu.backend.api.encounter;

import com.matibabu.backend.application.encounter.CancelEncounterUseCase;
import com.matibabu.backend.application.encounter.DischargeEncounterUseCase;
import com.matibabu.backend.application.encounter.GetEncounterUseCase;
import com.matibabu.backend.application.encounter.StartEncounterUseCase;
import com.matibabu.backend.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/encounters")
public class EncounterController {

    private final StartEncounterUseCase startEncounterUseCase;
    private final GetEncounterUseCase getEncounterUseCase;
    private final DischargeEncounterUseCase dischargeEncounterUseCase;
    private final CancelEncounterUseCase cancelEncounterUseCase;

    public EncounterController(
            StartEncounterUseCase startEncounterUseCase,
            GetEncounterUseCase getEncounterUseCase,
            DischargeEncounterUseCase dischargeEncounterUseCase,
            CancelEncounterUseCase cancelEncounterUseCase
    ) {
        this.startEncounterUseCase = startEncounterUseCase;
        this.getEncounterUseCase = getEncounterUseCase;
        this.dischargeEncounterUseCase = dischargeEncounterUseCase;
        this.cancelEncounterUseCase = cancelEncounterUseCase;
    }

    @GetMapping("/{id}")
    public EncounterResponse getById(@PathVariable UUID id) {
        return EncounterResponse.from(
                getEncounterUseCase.getById(id)
        );
    }

    /*
     * The attending clinician is taken from the authenticated
     * session: which clinician is treating a patient must not be
     * something the client can spoof by passing a different ID.
     */
    @PostMapping("/{patientId}")
    @ResponseStatus(HttpStatus.CREATED)
    public EncounterResponse start(
            @PathVariable UUID patientId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return EncounterResponse.from(
                startEncounterUseCase.start(
                        patientId,
                        principal.getClinician().getId(),
                        Instant.now()
                )
        );
    }

    @PostMapping("/{id}/discharge")
    public EncounterResponse discharge(@PathVariable UUID id) {

        dischargeEncounterUseCase.discharge(
                id,
                Instant.now()
        );

        return EncounterResponse.from(
                getEncounterUseCase.getById(id)
        );
    }

    @PostMapping("/{id}/cancel")
    public EncounterResponse cancel(@PathVariable UUID id) {

        cancelEncounterUseCase.cancel(
                id,
                Instant.now()
        );

        return EncounterResponse.from(
                getEncounterUseCase.getById(id)
        );
    }
}