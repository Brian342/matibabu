package com.matibabu.backend.api.facility;

import com.matibabu.backend.application.facility.CreateFacilityUseCase;
import com.matibabu.backend.application.facility.DeactivateFacilityUseCase;
import com.matibabu.backend.application.facility.ListFacilitiesUseCase;
import com.matibabu.backend.domain.facility.Facility;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/facilities")
public class FacilityController {

    private final CreateFacilityUseCase createFacilityUseCase;
    private final ListFacilitiesUseCase listFacilitiesUseCase;
    private final DeactivateFacilityUseCase deactivateFacilityUseCase;

    public FacilityController(
            CreateFacilityUseCase createFacilityUseCase,
            ListFacilitiesUseCase listFacilitiesUseCase,
            DeactivateFacilityUseCase deactivateFacilityUseCase
    ) {
        this.createFacilityUseCase = createFacilityUseCase;
        this.listFacilitiesUseCase = listFacilitiesUseCase;
        this.deactivateFacilityUseCase = deactivateFacilityUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FacilityResponse create(@RequestBody CreateFacilityRequest request) {
        return FacilityResponse.from(
                createFacilityUseCase.create(request.name(), request.type(), request.mflCode())
        );
    }

    @GetMapping("/{id}")
    public FacilityResponse getById(@PathVariable UUID id) {
        return FacilityResponse.from(listFacilitiesUseCase.getById(id));
    }

    @GetMapping
    public List<FacilityResponse> list(@RequestParam(required = false) String query) {
        List<Facility> facilities =
                (query == null || query.isBlank())
                        ? listFacilitiesUseCase.listActive()
                        : listFacilitiesUseCase.search(query);

        return facilities.stream()
                .map(FacilityResponse::from)
                .toList();
    }

    @PostMapping("/{id}/deactivate")
    public FacilityResponse deactivate(@PathVariable UUID id) {
        return FacilityResponse.from(deactivateFacilityUseCase.deactivate(id));
    }
}
