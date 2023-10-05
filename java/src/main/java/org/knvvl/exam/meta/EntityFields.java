package org.knvvl.exam.meta;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import org.springframework.data.jpa.repository.JpaRepository;

import com.google.gson.stream.JsonWriter;

public class EntityFields<T extends KnvvlEntity>
{
    private final List<EntityField<T>> fields;

    public EntityFields(List<EntityField<T>> fields)
    {
        this.fields = fields;
    }

    public List<EntityField<T>> getFields()
    {
        return fields;
    }

    @Nonnull
    public EntityField<T> getFieldByName(String name)
    {
        return fields.stream()
            .filter(f -> name.equals(f.getField()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Field not found: " + name));
    }
}
