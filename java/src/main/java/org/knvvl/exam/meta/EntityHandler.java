package org.knvvl.exam.meta;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.data.jpa.repository.JpaRepository;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public record EntityHandler<T extends KnvvlEntity>(JpaRepository<T, ?> repository, EntityFields<T> fields, String fieldName, Supplier<T> constructor)
{
    private static final String FIELD_ID = "id";

    public int exportAll(JsonWriter writer) throws IOException
    {
        List<T> entities = repository.findAll();
        writer.name(fieldName);
        writer.beginArray();

        for (T entity : entities)
        {
            writer.beginObject();
            if (entity instanceof IdEntity idEntity)
            {
                writer.name(FIELD_ID);
                writer.value(idEntity.getId());
            }
            for (EntityField<T> field : fields.getFields())
            {
                field.exportJson(entity, writer);
            }
            writer.endObject();
        }
        writer.endArray();
        return entities.size();
    }

    public boolean isEmpty()
    {
        return repository.count() == 0;
    }

    public int importAll(JsonReader reader) throws IOException
    {
        int count = 0;
        reader.beginArray();

        while (reader.hasNext())
        {
            T entity = constructor.get();
            reader.beginObject();
            while (reader.hasNext())
            {
                String fieldName = reader.nextName();
                if (FIELD_ID.equals(fieldName) && entity instanceof IdEntity idEntity)
                {
                    idEntity.setId(reader.nextInt());
                }
                else
                {
                    fields.getFieldByName(fieldName).importJsonValue(entity, reader);
                }
            }
            reader.endObject();
            repository.save(entity);
            count++;
        }
        reader.endArray();
        return count;
    }
}
