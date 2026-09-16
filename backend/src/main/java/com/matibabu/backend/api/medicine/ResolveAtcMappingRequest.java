package com.matibabu.backend.api.medicine;


public record ResolveAtcMappingRequest(
        AtcMappingAction action,
        String atcCode
) {
    public enum AtcMappingAction {
        CONFIRM,
        MARK_UNMAPPED
    }
}
