package org.knvvl.exam.entities;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.Cache;
import org.knvvl.exam.meta.EntityField;
import org.knvvl.exam.meta.EntityFields;
import org.knvvl.exam.meta.KnvvlEntity;
import org.knvvl.exam.repos.QuestionRepository;
import org.knvvl.exam.repos.UserRepository;

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
public class Change implements KnvvlEntity
{
    @EmbeddedId
    private ChangeKey changeKey;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    public static EntityFields<Change> getFields(UserRepository userRepository, QuestionRepository questionRepository)
    {
        return new EntityFields<>(List.of(
            new EntityField.EntityFieldIdEntity<>("changedBy", userRepository, Change::getChangedBy, Change::setChangedBy),
            new EntityField.EntityFieldLong<>("changedAt", Change::getChangedAtSeconds, Change::setChangedAtSeconds),
            new EntityField.EntityFieldIdEntity<>("question", questionRepository, Change::getQuestion, Change::setQuestion),
            new EntityField.EntityFieldString<>("field", Change::getField, Change::setField),
            new EntityField.EntityFieldString<>("oldValue", Change::getOldValue, Change::setOldValue),
            new EntityField.EntityFieldString<>("newValue", Change::getNewValue, Change::setNewValue)));
    }

    public Change()
    {
    }

    private Change(ChangeKey changeKey)
    {
        this.changeKey = changeKey;
    }

    public Change(ChangedByAt changedByAt, Question question, String field, String oldValue, String newValue)
    {
        this.changeKey = new ChangeKey(changedByAt.changedBy, changedByAt.changedAt.getEpochSecond(), question, field);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public static Change newChangeForJsonImport()
    {
        return new Change(new ChangeKey());
    }

    public record ChangedByAt(User changedBy, Instant changedAt)
    {
        public ChangedByAt(User changedBy)
        {
            this(changedBy, now());
        }

        public static Instant now()
        {
            var now = Instant.now();
            return now.minusNanos(now.getNano());
        }

        public String toString()
        {
            return changedAt + " by " + changedBy;
        }
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

    public void setOldValue(String oldValue)
    {
        this.oldValue = oldValue;
    }

    public String getNewValue()
    {
        return newValue;
    }

    public void setNewValue(String newValue)
    {
        this.newValue = newValue;
    }

    // ---------------- Getters/setters for JSON constructor ----------------

    private User getChangedBy()
    {
        return changeKey.changedBy;
    }

    private void setChangedBy(User user)
    {
        changeKey.changedBy = user;
    }

    private long getChangedAtSeconds()
    {
        return changeKey.changedAt;
    }

    private void setChangedAtSeconds(long seconds)
    {
        changeKey.changedAt = seconds;
    }

    private Question getQuestion()
    {
        return changeKey.question;
    }

    private void setQuestion(Question question)
    {
        changeKey.question = question;
    }

    private String getField()
    {
        return changeKey.field;
    }

    private void setField(String field)
    {
        changeKey.field = field;
    }
}
