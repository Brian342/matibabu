package com.matibabu.backend.application.medicine;

import com.matibabu.backend.domain.medicine.Medicine;

import java.util.UUID;

public interface GetMedicineUseCase {

    Medicine getById(UUID id);
}
