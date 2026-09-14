package com.matibabu.backend.domain.medicine;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicineRepository {

    Optional<Medicine> findById(UUID id);

    /*
     * Free-text search across name, generic name, and ATC code.
     * Used to power a prescribing/typeahead search in the UI.
     */
    List<Medicine> search(String query);

    List<Medicine> findAllActive();

    /*
     * Medicines whose ATC mapping still needs a reviewer's judgment
     * (AUTO_MATCHED, not yet checked by a person; or NEEDS_REVIEW,
     * where the importer had no confident guess at all). Powers the
     * admin review queue.
     */
    List<Medicine> findByAtcMappingStatusIn(List<AtcMappingStatus> statuses);

    void save(Medicine medicine);
}