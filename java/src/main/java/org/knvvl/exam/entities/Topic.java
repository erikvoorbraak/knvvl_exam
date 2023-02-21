package org.knvvl.exam.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="t_topic")
public class Topic implements LabeledEntity
{
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "label")
    private String label;

    @Column(name = "num_questions")
    private int numQuestions;

    public Topic()
    {
    }

    public Topic(String label)
    {
        this.label = label;
    }

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

    public int getNumQuestions()
    {
        return numQuestions;
    }

    public void setNumQuestions(int numQuestions)
    {
        this.numQuestions = numQuestions;
    }
}
