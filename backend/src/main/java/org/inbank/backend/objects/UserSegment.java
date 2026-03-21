package org.inbank.backend.objects;

public record UserSegment(
    Integer creditModifier,
    boolean hasDebt
) {}

