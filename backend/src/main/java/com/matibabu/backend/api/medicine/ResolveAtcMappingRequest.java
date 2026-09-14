package com.matibabu.backend.api.medicine;

/*
 * action = CONFIRM requires atcCode to be present.
 * action = MARK_UNMAPPED ignores atcCode (a reviewer has determined
 * no ATC code genuinely applies to this medicine).
 */
public record ResolveAtcMappingRequest(
        AtcMappingAction action,
        String atcCode
) {
    public enum AtcMappingAction {
        CONFIRM,
        MARK_UNMAPPED
    }
}
