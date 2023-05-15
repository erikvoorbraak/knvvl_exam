package org.knvvl.exam.entities;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;

import java.io.Serializable;
import java.time.Instant;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="t_change")
@Cacheable @Cache(usage = READ_WRITE)
public class Change
{
    @EmbeddedId
    private ChangeKey changeKey;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    public Change()
    {
    }

    public Change(ChangedByAt changedByAt, Question question, String field, String oldValue, String newValue)
    {
        this.changeKey = new ChangeKey(changedByAt.changedBy, changedByAt.changedAt.getEpochSecond(), question, field);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public record ChangedByAt(User changedBy, Instant changedAt)
    {
    }

    @Embeddable
    public static class ChangeKey implements Serializable
    {
        @ManyToOne
        @JoinColumn(name = "changed_by")
        private User changedBy;

        @Column(name = "changed_at")
        private long changedAt;

        @ManyToOne
        @JoinColumn(name = "question")
        private Question question;

        @Column(name = "field")
        private String field;

        public ChangeKey()
        {
        }

        public ChangeKey(User changedBy, long changedAt, Question question, String field)
        {
            this.changedBy = changedBy;
            this.changedAt = changedAt;
            this.question = question;
            this.field = field;
        }

        public User getChangedBy()
        {
            return changedBy;
        }

        public Instant getChangedAt()
        {
            return Instant.ofEpochSecond(changedAt);
        }

        public Question getQuestion()
        {
            return question;
        }

        public String getField()
        {
            return field;
        }
    }

    public ChangeKey getChangeKey()
    {
        return changeKey;
    }

    public String getOldValue()
    {
        return oldValue;
    }

    public String getNewValue()
    {
        return newValue;
    }
}
