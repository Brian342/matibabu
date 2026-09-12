package com.matibabu.backend.application.medicine;

import com.matibabu.backend.domain.medicine.Medicine;

import java.util.List;

public interface ListMedicinesUseCase {

    /*
     * Lists medicines matching a free-text search (name, generic
     * name, or ATC code). A blank/null query returns all active
     * medicines, which powers a default browsable list in the UI.
     */
    List<Medicine> search(String query);
}
