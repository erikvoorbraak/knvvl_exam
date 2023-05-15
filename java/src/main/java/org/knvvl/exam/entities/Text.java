package org.knvvl.exam.entities;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="t_text")
@Cacheable @Cache(usage = READ_WRITE)
public class Text
{
    @Id
    @Column(name = "id")
    private String key;

    @Column(name = "label")
    private String label;

    Text()
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
