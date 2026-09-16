package com.matibabu.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/*
 * A deployment's own identity: which facility it is, and whether it
 * is running as an ordinary facility node or as the central hub.
 * See docs/decisions/ADR-08-synchronization.md.
 */
@Component
public class NodeIdentity {

    private final String nodeRole;
    private final UUID facilityId;

    public NodeIdentity(
            @Value("${app.node.role:facility}") String nodeRole,
            @Value("${app.facility.id}") String facilityId
    ) {
        this.nodeRole = nodeRole;
        this.facilityId = UUID.fromString(facilityId);
    }

    public UUID facilityId() {
        return facilityId;
    }

    public boolean isHub() {
        return "hub".equalsIgnoreCase(nodeRole);
    }
}
