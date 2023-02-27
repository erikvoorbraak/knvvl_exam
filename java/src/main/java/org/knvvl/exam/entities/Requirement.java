package org.knvvl.exam.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="t_requirement")
public class Requirement implements LabeledEntity
{
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "label")
    private String label;

    @ManyToOne
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

    @Override
    public Integer getId()
    {
        return id;
    }

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

    @Override
    public String toString()
    {
        return label;
    }
}
