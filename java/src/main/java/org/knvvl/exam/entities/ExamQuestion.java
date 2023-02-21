package org.knvvl.exam.entities;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="t_exam_question")
public class ExamQuestion
{
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "exam")
    private int exam;

    @ManyToOne
    @BatchSize(size=20)
    @JoinColumn(name = "question")
    private Question question;

    @ManyToOne
    @BatchSize(size=20)
    @JoinColumn(name = "topic")
    private Topic topic;

    @Column(name = "question_index")
    private int questionIndex;

    public int getExam()
    {
        return exam;
    }

    public ExamQuestion()
    {
        // For Hibernate
    }

    public ExamQuestion(int exam, Question question, Topic topic, int questionIndex)
    {
        this.id = exam * 1000 + questionIndex;
        this.exam = exam;
        this.question = question;
        this.topic = topic;
        this.questionIndex = questionIndex;
    }

    public Question getQuestion()
    {
        return question;
    }

    public void setQuestion(Question question)
    {
        this.question = question;
    }

    public int getQuestionIndex()
    {
        return questionIndex;
    }

    public int getId()
    {
        return id;
    }

    public Topic getTopic()
    {
        return topic;
    }
}
