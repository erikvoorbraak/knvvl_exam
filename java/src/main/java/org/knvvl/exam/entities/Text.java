package org.knvvl.exam.entities;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;
import static org.knvvl.exam.meta.Config.CONFIGS;
import static org.knvvl.exam.meta.Config.WRITE_ONLY_CONFIGS;

import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.hibernate.annotations.Cache;
import org.knvvl.exam.meta.Config;
import org.knvvl.exam.meta.EntityField;
import org.knvvl.exam.meta.EntityFields;
import org.knvvl.exam.meta.KnvvlEntity;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="t_text")
@Cacheable @Cache(usage = READ_WRITE)
public class Text implements KnvvlEntity
{
    public static final String WRITE_ONLY_VALUE = "****";

    @Id
    @Column(name = "id")
    private String key;

    @Column(name = "label")
    private String value;

    public static EntityFields<Text> getFields()
    {
        return new EntityFields<>(List.of(
            new EntityField.EntityFieldString<>("key", Text::getKey, Text::setKey),
            new EntityField.EntityFieldString<>("value", Text::getValue, Text::setValue)));
    }

    public Text()
    {
    }

    public Text(String key, String label)
    {
        this.key = key;
        this.value = label;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getValueToEdit()
    {
        return Strings.isBlank(value)
            ? ""
            : isWriteOnly() ? WRITE_ONLY_VALUE : value;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getLabel()
    {
        return CONFIGS.stream().filter(c -> c.key().equals(key)).map(Config::label).findFirst().orElse("");
    }

    private boolean isWriteOnly()
    {
        return WRITE_ONLY_CONFIGS.stream().map(Config::key).anyMatch(key::equals);
    }

    @Override
    public String toString()
    {
        return value;
    }
}
