package org.knvvl.exam.entities;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;

import java.io.Serializable;
import java.util.List;

import org.hibernate.annotations.Cache;
import org.knvvl.exam.meta.EntityField;
import org.knvvl.exam.meta.EntityFields;
import org.knvvl.exam.meta.KnvvlEntity;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="t_exam_answer")
@Cacheable @Cache(usage = READ_WRITE)
public class ExamAnswer implements KnvvlEntity
{
    @EmbeddedId
    private ExamAnswerKey examAnswerKey;

    @Column(name = "topic")
    private int topic;

    @Column(name = "answers_correct")
    private String answersCorrect;

    @Column(name = "answer_given")
    private String answerGiven;

    public static EntityFields<ExamAnswer> getFields()
    {
        return new EntityFields<>(List.of(
            new EntityField.EntityFieldString<>("student", ExamAnswer::getStudent, ExamAnswer::setStudent),
            new EntityField.EntityFieldInteger<>("exam", ExamAnswer::getExam, ExamAnswer::setExam),
            new EntityField.EntityFieldInteger<>("question", ExamAnswer::getQuestion, ExamAnswer::setQuestion),
            new EntityField.EntityFieldInteger<>("topic", ExamAnswer::getTopic, ExamAnswer::setTopic),
            new EntityField.EntityFieldString<>("answersCorrect", ExamAnswer::getAnswersCorrect, ExamAnswer::setAnswersCorrect),
            new EntityField.EntityFieldString<>("answerGiven", ExamAnswer::getAnswerGiven, ExamAnswer::setAnswerGiven)));
    }

    public ExamAnswer()
    {
        // For Hibernate

    }

    public ExamAnswer(String student, int exam, int question, int topic, String answersCorrect, String answerGiven)
    {
        this.examAnswerKey = new ExamAnswerKey(student, exam, question);
        this.topic = topic;
        this.answersCorrect = answersCorrect;
        this.answerGiven = answerGiven;
    }

    public static ExamAnswer newExamAnswerForJsonImport()
    {
        return new ExamAnswer(null, 0, 0, 0, null, null);
    }

    @Embeddable
    public static class ExamAnswerKey implements Serializable
    {
        @Column(name = "student")
        private String student;

        @Column(name = "exam")
        private int exam;

        @Column(name = "question")
        private int question;

        public ExamAnswerKey()
        {
            // For Hibernate
        }

        public ExamAnswerKey(String student, int exam, int question)
        {
            this.student = student;
            this.exam = exam;
            this.question = question;
        }
    }

    public String getStudent()
    {
        return examAnswerKey.student;
    }

    public void setStudent(String student)
    {
        examAnswerKey.student = student;
    }

    public int getExam()
    {
        return examAnswerKey.exam;
    }

    public void setExam(int exam)
    {
        examAnswerKey.exam = exam;
    }

    public int getQuestion()
    {
        return examAnswerKey.question;
    }

    public void setQuestion(int question)
    {
        examAnswerKey.question = question;
    }

    public int getTopic()
    {
        return topic;
    }

    public void setTopic(int topic)
    {
        this.topic = topic;
    }

    public String getAnswersCorrect()
    {
        return answersCorrect;
    }

    public void setAnswersCorrect(String answerCorrect)
    {
        this.answersCorrect = answerCorrect;
    }

    public String getAnswerGiven()
    {
        return answerGiven;
    }

    public void setAnswerGiven(String answerGiven)
    {
        this.answerGiven = answerGiven;
    }

    public boolean isCorrect()
    {
        return answersCorrect.contains(answerGiven);
    }
}
