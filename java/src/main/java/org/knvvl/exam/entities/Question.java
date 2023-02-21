package org.knvvl.exam.entities;

import static java.util.Objects.requireNonNull;

import static org.apache.commons.lang3.Validate.isTrue;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
//@BatchSize(size=20) // TODO Batch size breaks getting Change instances
@Table(name="t_question")
public class Question
{
    @Id
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "topic")
    private Topic topic;

    @ManyToOne
    @JoinColumn(name = "requirement")
    private Requirement requirement;

    @Column(name = "question")
    private String question;

    @Column(name = "answer_a")
    private String answerA;

    @Column(name = "answer_b")
    private String answerB;

    @Column(name = "answer_c")
    private String answerC;

    @Column(name = "answer_d")
    private String answerD;

    @Column(name = "answer")
    private String answer;

    @ManyToOne
    @JoinColumn(name = "picture")
    private Picture picture;

    @Column(name = "ignore")
    private boolean ignore;

    @Column(name = "allow_b2")
    private boolean allowB2;

    @Column(name = "allow_b3")
    private boolean allowB3;

    @Column(name = "discuss")
    private boolean discuss;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "exam_group")
    private String examGroup;

    public Integer getId()
    {
        return id;
    }

    public Topic getTopic()
    {
        return topic;
    }

    public Requirement getRequirement()
    {
        return requirement;
    }

    public String getQuestion()
    {
        return question;
    }

    public String getAnswerA()
    {
        return answerA;
    }

    public String getAnswerB()
    {
        return answerB;
    }

    public String getAnswerC()
    {
        return answerC;
    }

    public String getAnswerD()
    {
        return answerD;
    }

    public String getAnswer()
    {
        return answer;
    }

    public Picture getPicture()
    {
        return picture;
    }

    public boolean isIgnore()
    {
        return ignore;
    }

    public boolean isAllowB2()
    {
        return allowB2;
    }

    public boolean isAllowB3()
    {
        return allowB3;
    }

    public boolean isDiscuss()
    {
        return discuss;
    }

    public String getRemarks()
    {
        return remarks;
    }

    @Nonnull
    public String getExamGroup()
    {
        return examGroup == null ? "" : examGroup;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setTopic(Topic topic)
    {
        this.topic = requireNonNull(topic);
    }

    public void setRequirement(Requirement requirement)
    {
        this.requirement = requireNonNull(requirement);
    }

    public void setQuestion(String question)
    {
        this.question = validateAndCorrect(question);
    }

    public void setAnswerA(String answerA)
    {
        this.answerA = validateAndCorrect(answerA);
    }

    public void setAnswerB(String answerB)
    {
        this.answerB = validateAndCorrect(answerB);
    }

    public void setAnswerC(String answerC)
    {
        this.answerC = validateAndCorrect(answerC);
    }

    public void setAnswerD(String answerD)
    {
        this.answerD = validateAndCorrect(answerD);
    }

    public void setAnswer(String answer)
    {
        this.answer = validate(answer);
    }

    private String validateAndCorrect(String text)
    {
        String ret = validate(text);
        if (Character.isLowerCase(ret.charAt(0)))
            ret = ret.substring(0, 1).toUpperCase() + ret.substring(1);
        if (Character.isLetterOrDigit(ret.charAt(ret.length() - 1)))
            ret = ret + ".";
        return ret.replace("  ", " ");
    }

    private String validate(String text)
    {
        requireNonNull(text);
        isTrue(!text.isBlank());
        return text;
    }

    public void setPicture(Picture picture)
    {
        this.picture = picture;
    }

    public void setIgnore(boolean ignore)
    {
        this.ignore = ignore;
    }

    public void setAllowB2(boolean allowB2)
    {
        this.allowB2 = allowB2;
    }

    public void setAllowB3(boolean allowB3)
    {
        this.allowB3 = allowB3;
    }

    public void setDiscuss(boolean discuss)
    {
        this.discuss = discuss;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    public void setExamGroup(String examGroup)
    {
        this.examGroup = examGroup;
    }

    public boolean applySearch(String searchLower)
    {
        if (searchLower.isEmpty())
        {
            return true;
        }
        return tryMatch(String.valueOf(id), searchLower)
            || tryMatch(question, searchLower)
            || tryMatch(answerA, searchLower)
            || tryMatch(answerB, searchLower)
            || tryMatch(answerC, searchLower)
            || tryMatch(answerD, searchLower)
            || tryMatch(remarks, searchLower)
            // Match keywords (tags)
            || (picture != null && tryMatch(picture.getFilename(), searchLower))
            || tryMatch(getExamGroup(), searchLower)
            || (allowB2 && "b2".contains(searchLower))
            || (allowB3 && "b3".contains(searchLower));
    }

    private boolean tryMatch(String value, String searchLower)
    {
        return value.toLowerCase().contains(searchLower);
    }

    public boolean allowForCertificate(int certificate)
    {
        if (certificate == 2)
            return allowB2;
        if (certificate == 3)
            return allowB3;
        return false;
    }
}
