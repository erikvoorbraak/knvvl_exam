package org.knvvl.exam.meta;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Base64;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.data.jpa.repository.JpaRepository;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import jakarta.annotation.Nonnull;

public abstract class EntityField<T extends KnvvlEntity>
{
    protected final String field;

    EntityField(@Nonnull String field)
    {
        this.field = field;
    }

    public String getField()
    {
        return field;
    }

    public String getValueField()
    {
        return getField();
    }

    public boolean isMandatory()
    {
        return "topic".equals(field)
            || "requirement".equals(field)
            || "question".equals(field)
            || field.startsWith("answer");
    }

    @Override
    public String toString()
    {
        return field;
    }

    public abstract boolean hasValue(T entity);
    public abstract String toStringValue(T entity);
    public abstract void copyValue(T from, T to);

    public abstract void writeJson(T entity, JsonObject jsonObject);
    public abstract void readJson(T entity, JsonElement jsonElement);

    public void exportJson(T entity, JsonWriter writer) throws IOException
    {
        if (hasValue(entity))
        {
            writer.name(field);
            exportJsonValue(entity, writer);
        }
    }

    protected abstract void exportJsonValue(T entity, JsonWriter writer) throws IOException;
    public abstract void importJsonValue(T entity, JsonReader reader) throws IOException;

    public static class EntityFieldString<T extends KnvvlEntity> extends EntityField<T>
    {
        private final Function<T, String> getter;
        private final BiConsumer<T, String> setter;

        public EntityFieldString(String field, Function<T, String> getter, BiConsumer<T, String> setter)
        {
            super(field);
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public boolean hasValue(T entity)
        {
            return getter.apply(entity) != null;
        }

        @Override
        public String toStringValue(T entity)
        {
            String value = getter.apply(entity);
            return value == null ? "" : value;
        }

        public void setStringValue(T entity, String value)
        {
            setter.accept(entity, value);
        }

        @Override
        public void copyValue(T from, T to)
        {
            setter.accept(to, getter.apply(from));
        }

        @Override
        public void writeJson(T entity, JsonObject jsonObject)
        {
            jsonObject.addProperty(field, getter.apply(entity));
        }

        @Override
        public void readJson(@Nonnull T entity, @Nonnull JsonElement jsonElement)
        {
            setter.accept(entity, jsonElement.getAsString());
        }

        @Override
        protected void exportJsonValue(T entity, JsonWriter writer) throws IOException
        {
            writer.value(getter.apply(entity));
        }

        @Override
        public void importJsonValue(T entity, JsonReader reader) throws IOException
        {
            setter.accept(entity, reader.nextString());
        }
    }

    public static class EntityFieldBoolean<T extends KnvvlEntity> extends EntityField<T>
    {
        private final Function<T, Boolean> getter;
        private final BiConsumer<T, Boolean> setter;

        public EntityFieldBoolean(String field, Function<T, Boolean> getter, BiConsumer<T, Boolean> setter)
        {
            super(field);
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public boolean hasValue(T entity)
        {
            return getter.apply(entity) != null;
        }

        @Override
        public String toStringValue(T question)
        {
            return getter.apply(question) ? "true" : "false";
        }

        @Override
        public void writeJson(T question, JsonObject jsonObject)
        {
            jsonObject.addProperty(field, getter.apply(question));
        }

        @Override
        public void readJson(T question, JsonElement jsonElement)
        {
            setter.accept(question, jsonElement.getAsBoolean());
        }

        @Override
        protected void exportJsonValue(T entity, JsonWriter writer) throws IOException
        {
            writer.value(getter.apply(entity));
        }

        @Override
        public void importJsonValue(T entity, JsonReader reader) throws IOException
        {
            setter.accept(entity, reader.nextBoolean());
        }

        @Override
        public void copyValue(T from, T to)
        {
            setter.accept(to, getter.apply(from));
        }
    }

    public static class EntityFieldInteger<T extends KnvvlEntity> extends EntityField<T>
    {
        private final Function<T, Integer> getter;
        private final BiConsumer<T, Integer> setter;

        public EntityFieldInteger(String field, Function<T, Integer> getter, BiConsumer<T, Integer> setter)
        {
            super(field);
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public boolean hasValue(T entity)
        {
            return getter.apply(entity) != null;
        }

        @Override
        public String toStringValue(T question)
        {
            return String.valueOf(getter.apply(question));
        }

        @Override
        public void writeJson(T question, JsonObject jsonObject)
        {
            jsonObject.addProperty(field, getter.apply(question));
        }

        @Override
        public void readJson(T question, JsonElement jsonElement)
        {
            setter.accept(question, jsonElement.getAsInt());
        }

        @Override
        protected void exportJsonValue(T entity, JsonWriter writer) throws IOException
        {
            writer.value(getter.apply(entity));
        }

        @Override
        public void importJsonValue(T entity, JsonReader reader) throws IOException
        {
            setter.accept(entity, reader.nextInt());
        }

        @Override
        public void copyValue(T from, T to)
        {
            setter.accept(to, getter.apply(from));
        }
    }

    public static class EntityFieldLong<T extends KnvvlEntity> extends EntityField<T>
    {
        private final Function<T, Long> getter;
        private final BiConsumer<T, Long> setter;

        public EntityFieldLong(String field, Function<T, Long> getter, BiConsumer<T, Long> setter)
        {
            super(field);
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public boolean hasValue(T entity)
        {
            return getter.apply(entity) != null;
        }

        @Override
        public String toStringValue(T question)
        {
            return String.valueOf(getter.apply(question));
        }

        @Override
        public void writeJson(T question, JsonObject jsonObject)
        {
            jsonObject.addProperty(field, getter.apply(question));
        }

        @Override
        public void readJson(T question, JsonElement jsonElement)
        {
            setter.accept(question, jsonElement.getAsLong());
        }

        @Override
        protected void exportJsonValue(T entity, JsonWriter writer) throws IOException
        {
            writer.value(getter.apply(entity));
        }

        @Override
        public void importJsonValue(T entity, JsonReader reader) throws IOException
        {
            setter.accept(entity, reader.nextLong());
        }

        @Override
        public void copyValue(T from, T to)
        {
            setter.accept(to, getter.apply(from));
        }
    }

    public static class EntityFieldBytes<T extends KnvvlEntity> extends EntityField<T>
    {
        private final Function<T, byte[]> getter;
        private final BiConsumer<T, byte[]> setter;

        public EntityFieldBytes(String field, Function<T, byte[]> getter, BiConsumer<T, byte[]> setter)
        {
            super(field);
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public boolean hasValue(T entity)
        {
            return getter.apply(entity) != null;
        }

        @Override
        public String toStringValue(T question)
        {
            byte[] bytes = getter.apply(question);
            return bytes == null ? "" : Base64.getEncoder().encodeToString(bytes);
        }

        @Override
        public void writeJson(T question, JsonObject jsonObject)
        {
            jsonObject.addProperty(field, toStringValue(question));
        }

        @Override
        public void readJson(T question, JsonElement jsonElement)
        {
            setter.accept(question, Base64.getDecoder().decode(jsonElement.getAsString()));
        }

        @Override
        protected void exportJsonValue(T entity, JsonWriter writer) throws IOException
        {
            writer.value(toStringValue(entity));
        }

        @Override
        public void importJsonValue(T entity, JsonReader reader) throws IOException
        {
            String base64 = reader.nextString();
            if (Strings.isNullOrEmpty(base64))
            {
                return;
            }
            setter.accept(entity, Base64.getDecoder().decode(base64));
        }

        @Override
        public void copyValue(T from, T to)
        {
            setter.accept(to, getter.apply(from));
        }
    }

    public static class EntityFieldIdEntity<T extends KnvvlEntity, V extends IdEntity> extends EntityField<T>
    {
        private final JpaRepository<V, Integer> repository;
        private final Function<T, V> getter;
        private final BiConsumer<T, V> setter;

        public EntityFieldIdEntity(String field, JpaRepository<V, Integer> repository, Function<T, V> getter, BiConsumer<T, V> setter)
        {
            super(field);
            this.repository = repository;
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public String getValueField()
        {
            return getField() + "Id";
        }

        @Override
        public boolean hasValue(T entity)
        {
            return getter.apply(entity) != null;
        }

        @Override
        public String toStringValue(T entity)
        {
            V idEntity = getIdEntity(entity);
            return idEntity instanceof LabeledEntity le ? le.getLabel() : "";
        }

        @Override
        public void readJson(T entity, JsonElement jsonElement)
        {
            setter.accept(entity, repository.getReferenceById(jsonElement.getAsInt()));
        }

        @Override
        public void writeJson(T entity, JsonObject jsonObject)
        {
            V idEntity = getIdEntity(entity);
            if (idEntity != null)
            {
                if (idEntity instanceof LabeledEntity le)
                {
                    jsonObject.addProperty(field, le.getLabel());
                }
                jsonObject.addProperty(getValueField(), idEntity.getId());
            }
        }

        public V getIdEntity(T entity)
        {
            return getter.apply(entity);
        }

        @Override
        public void exportJson(T entity, JsonWriter writer) throws IOException
        {
            if (getter.apply(entity) != null)
            {
                super.exportJson(entity, writer);
            }
        }

        @Override
        protected void exportJsonValue(T entity, JsonWriter writer) throws IOException
        {
            V idEntity = requireNonNull(getter.apply(entity));
            writer.value(idEntity.getId());
        }

        @Override
        public void importJsonValue(T entity, JsonReader reader) throws IOException
        {
            int id = reader.nextInt();
            V idEntity = repository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Cannot find record for " + entity + "." + field + ": " + id));
            setter.accept(entity, idEntity);
        }

        @Override
        public void copyValue(T from, T to)
        {
            setter.accept(to, getter.apply(from));
        }
    }
}
