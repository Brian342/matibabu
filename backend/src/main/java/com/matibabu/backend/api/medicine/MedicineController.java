package com.matibabu.backend.api.medicine;

import com.matibabu.backend.application.medicine.GetMedicineUseCase;
import com.matibabu.backend.application.medicine.ListMedicinesUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/*
 * Exposes the local medicine reference catalog (seeded from the
 * Kenya Essential Medicines List) so a prescriber can search for and
 * select a medicine when adding a Treatment, instead of typing a
 * drug name as free text.
 */
@RestController
@RequestMapping("/api/medicines")
public class MedicineController {

    private final ListMedicinesUseCase listMedicinesUseCase;
    private final GetMedicineUseCase getMedicineUseCase;

    public MedicineController(
            ListMedicinesUseCase listMedicinesUseCase,
            GetMedicineUseCase getMedicineUseCase
    ) {
        this.listMedicinesUseCase = listMedicinesUseCase;
        this.getMedicineUseCase = getMedicineUseCase;
    }

    @GetMapping
    public List<MedicineResponse> search(
            @RequestParam(required = false) String query
    ) {
        return listMedicinesUseCase.search(query).stream()
                .map(MedicineResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public MedicineResponse getById(@PathVariable UUID id) {
        return MedicineResponse.from(getMedicineUseCase.getById(id));
    }
}
