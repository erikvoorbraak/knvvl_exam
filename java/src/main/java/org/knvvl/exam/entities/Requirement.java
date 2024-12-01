package org.knvvl.exam.entities;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;

import java.util.List;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.knvvl.exam.meta.EntityField;
import org.knvvl.exam.meta.EntityFields;
import org.knvvl.exam.meta.LabeledEntity;
import org.knvvl.exam.repos.TopicRepository;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="t_requirement")
@BatchSize(size=20)
@Cacheable @Cache(usage = READ_WRITE)
public class Requirement implements LabeledEntity
{
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "label")
    private String label;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic")
    private Topic topic;

    @Column(name = "domain")
    private int domain;

    @Column(name = "domain_title")
    private String domainTitle;

    @Column(name = "subdomain")
    private String subdomain;

    @Column(name = "level_b2")
    private String levelB2;

    @Column(name = "level_b3")
    private String levelB3;

    public static EntityFields<Requirement> getFields(TopicRepository topicRepository)
    {
        return new EntityFields<>(List.of(
            new EntityField.EntityFieldString<>("label", Requirement::getLabel, Requirement::setLabel).mandatory(),
            new EntityField.EntityFieldIdEntity<>("topic", topicRepository, Requirement::getTopic, Requirement::setTopic).mandatory(),
            new EntityField.EntityFieldInteger<>("domain", Requirement::getDomain, Requirement::setDomain).mandatory(),
            new EntityField.EntityFieldString<>("domainTitle", Requirement::getDomainTitle, Requirement::setDomainTitle).mandatory(),
            new EntityField.EntityFieldString<>("subdomain", Requirement::getSubdomain, Requirement::setSubdomain).mandatory(),
            new EntityField.EntityFieldString<>("levelB2", Requirement::getLevelB2, Requirement::setLevelB2),
            new EntityField.EntityFieldString<>("levelB3", Requirement::getLevelB3, Requirement::setLevelB3)));
    }

    @Override
    public Integer getId()
    {
        return id;
    }

    @Override
    public void setId(int id)
    {
        this.id = id;
    }

    @Override
    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public Topic getTopic()
    {
        return topic;
    }

    public void setTopic(Topic topic)
    {
        this.topic = topic;
    }

    public int getDomain()
    {
        return domain;
    }

    public void setDomain(int domain)
    {
        this.domain = domain;
    }

    public String getDomainTitle()
    {
        return domainTitle;
    }

    public void setDomainTitle(String domainTitle)
    {
        this.domainTitle = domainTitle;
    }

    public String getSubdomain()
    {
        return subdomain;
    }

    public void setSubdomain(String subdomain)
    {
        this.subdomain = subdomain;
    }

    public String getLevelB2()
    {
        return levelB2;
    }

    public void setLevelB2(String levelB2)
    {
        this.levelB2 = levelB2;
    }

    public String getLevelB3()
    {
        return levelB3;
    }

    public void setLevelB3(String levelB3)
    {
        this.levelB3 = levelB3;
    }

    public String getTopicLabel()
    {
        return topic == null ? "" : topic.getLabel();
    }

    public Integer getTopicId()
    {
        return topic == null ? null : topic.getId();
    }

    @Override
    public String toString()
    {
        return label;
    }
}
