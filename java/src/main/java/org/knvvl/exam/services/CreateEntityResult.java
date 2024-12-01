package org.knvvl.exam.services;

import org.knvvl.exam.meta.IdEntity;

import jakarta.annotation.Nullable;

public record CreateEntityResult(@Nullable IdEntity entity, @Nullable String message)
{}