package com.f1.seasonchampions.dto;

import java.util.List;

public record RaceSyncResult(
    int updatedCount, List<String> updatedRaces, boolean success, String message) {}
