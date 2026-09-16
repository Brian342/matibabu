package com.matibabu.backend.api.patient;

import com.matibabu.backend.application.patient.DeletePatientUseCase;
import com.matibabu.backend.application.patient.GetPatientUseCase;
import com.matibabu.backend.application.patient.ListPatientsUseCase;
import com.matibabu.backend.application.patient.RegisterPatientUseCase;
import com.matibabu.backend.application.patient.SearchPatientByPhoneNumberUseCase;
import com.matibabu.backend.application.patient.UpdatePatientUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final RegisterPatientUseCase registerPatientUseCase;
    private final GetPatientUseCase getPatientUseCase;
    private final ListPatientsUseCase listPatientsUseCase;
    private final UpdatePatientUseCase updatePatientUseCase;
    private final DeletePatientUseCase deletePatientUseCase;
    private final SearchPatientByPhoneNumberUseCase searchPatientByPhoneNumberUseCase;

    public PatientController(
            RegisterPatientUseCase registerPatientUseCase,
            GetPatientUseCase getPatientUseCase,
            ListPatientsUseCase listPatientsUseCase,
            UpdatePatientUseCase updatePatientUseCase,
            DeletePatientUseCase deletePatientUseCase,
            SearchPatientByPhoneNumberUseCase searchPatientByPhoneNumberUseCase
    ) {
        this.registerPatientUseCase = registerPatientUseCase;
        this.getPatientUseCase = getPatientUseCase;
        this.listPatientsUseCase = listPatientsUseCase;
        this.updatePatientUseCase = updatePatientUseCase;
        this.deletePatientUseCase = deletePatientUseCase;
        this.searchPatientByPhoneNumberUseCase = searchPatientByPhoneNumberUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientResponse register(
            @Valid @RequestBody RegisterPatientRequest request
    ) {
        return PatientResponse.from(
                registerPatientUseCase.register(
                        request.firstName(),
                        request.lastName(),
                        request.dateOfBirth(),
                        request.gender(),
                        request.phoneNumber(),
                        request.address(),
                        request.nationalId(),
                        request.birthCertificateNumber()
                )
        );
    }

    @GetMapping("/{id}")
    public PatientResponse getById(@PathVariable UUID id) {
        return PatientResponse.from(
                getPatientUseCase.getById(id)
        );
    }

    @GetMapping
    public Page<PatientResponse> list(
            @PageableDefault(page = 0, size = 20) Pageable pageable
    ) {
        return listPatientsUseCase.list(pageable)
                .map(PatientResponse::from);
    }

    @PutMapping("/{id}")
    public PatientResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePatientRequest request
    ) {
        return PatientResponse.from(
                updatePatientUseCase.update(
                        id,
                        request.firstName(),
                        request.lastName(),
                        request.dateOfBirth(),
                        request.gender(),
                        request.phoneNumber(),
                        request.address(),
                        request.nationalId(),
                        request.birthCertificateNumber()
                )
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        deletePatientUseCase.delete(id);
    }

    @GetMapping("/search")
    public PatientResponse searchByPhoneNumber(
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "query", required = false) String queryParam
    ) {
        String query = phoneNumber != null ? phoneNumber : (phone != null ? phone : (q != null ? q : queryParam));
        if (query == null || query.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parameter 'phoneNumber' is required");
        }
        return PatientResponse.from(
                searchPatientByPhoneNumberUseCase.searchByPhoneNumber(query)
        );
    }

    @GetMapping("/phone/{phoneNumber}")
    public PatientResponse getByPhoneNumber(@PathVariable String phoneNumber) {
        return PatientResponse.from(
                searchPatientByPhoneNumberUseCase.searchByPhoneNumber(phoneNumber)
        );
    }
}