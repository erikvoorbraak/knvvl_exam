package org.knvvl.exam.entities;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;

import java.util.List;

import org.hibernate.annotations.Cache;
import org.knvvl.exam.meta.KnvvlEntity;
import org.knvvl.exam.meta.EntityField;
import org.knvvl.exam.meta.EntityFields;

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
    @Id
    @Column(name = "id")
    private String key;

    @Column(name = "label")
    private String label;

    public static EntityFields<Text> getFields()
    {
        return new EntityFields<>(List.of(
            new EntityField.EntityFieldString<>("key", Text::getKey, Text::setKey),
            new EntityField.EntityFieldString<>("label", Text::getLabel, Text::setLabel)));
    }

    public Text()
    {
    }

    public Text(String key, String label)
    {
        this.key = key;
        this.label = label;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    @Override
    public String toString()
    {
        return label;
    }
}
