package com.freightos.fms.domain.common.vo;

import java.util.Objects;

public record VesselVoyage(String vesselName, String voyageNo) {

    public VesselVoyage {
        Objects.requireNonNull(vesselName, "VesselVoyage.vesselName must not be null");
        if (vesselName.isBlank()) throw new IllegalArgumentException("VesselVoyage.vesselName must not be blank");
    }

    public static VesselVoyage of(String vesselName, String voyageNo) {
        if (vesselName == null || vesselName.isBlank()) return null;
        return new VesselVoyage(vesselName, voyageNo);
    }
}
