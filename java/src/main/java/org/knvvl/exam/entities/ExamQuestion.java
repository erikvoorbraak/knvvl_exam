package org.knvvl.exam.entities;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;

import java.util.List;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.knvvl.exam.meta.IdEntity;
import org.knvvl.exam.meta.KnvvlEntity;
import org.knvvl.exam.repos.QuestionRepository;
import org.knvvl.exam.repos.TopicRepository;
import org.knvvl.exam.meta.EntityField;
import org.knvvl.exam.meta.EntityFields;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="t_exam_question")
@BatchSize(size=20)
@Cacheable @Cache(usage = READ_WRITE)
public class ExamQuestion implements IdEntity
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

    public static EntityFields<ExamQuestion> getFields(QuestionRepository questionRepository, TopicRepository topicRepository)
    {
        return new EntityFields<>(List.of(
            new EntityField.EntityFieldInteger<>("exam", ExamQuestion::getExam, ExamQuestion::setExam),
            new EntityField.EntityFieldIdEntity<>("question", questionRepository, ExamQuestion::getQuestion, ExamQuestion::setQuestion),
            new EntityField.EntityFieldIdEntity<>("topic", topicRepository, ExamQuestion::getTopic, ExamQuestion::setTopic),
            new EntityField.EntityFieldInteger<>("questionIndex", ExamQuestion::getQuestionIndex, ExamQuestion::setQuestionIndex)));
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

    public int getExam()
    {
        return exam;
    }

    public void setExam(int exam)
    {
        this.exam = exam;
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

    public void setQuestionIndex(int questionIndex)
    {
        this.questionIndex = questionIndex;
    }

    public Topic getTopic()
    {
        return topic;
    }

    public void setTopic(Topic topic)
    {
        this.topic = topic;
    }
}
