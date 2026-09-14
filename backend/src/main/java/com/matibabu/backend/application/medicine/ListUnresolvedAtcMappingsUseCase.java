package com.matibabu.backend.application.medicine;

import com.matibabu.backend.domain.medicine.Medicine;

import java.util.List;

public interface ListUnresolvedAtcMappingsUseCase {

    /*
     * Medicines whose ATC mapping is AUTO_MATCHED (unreviewed) or
     * NEEDS_REVIEW (importer had no confident guess). Powers the
     * admin review queue.
     */
    List<Medicine> list();
}
