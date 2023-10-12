package org.knvvl.exam.entities;

import static java.util.Objects.requireNonNull;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;
import static org.knvvl.exam.services.Languages.LANGUAGE_NL;

import static jakarta.persistence.FetchType.LAZY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.knvvl.exam.meta.EntityField;
import org.knvvl.exam.meta.EntityFields;
import org.knvvl.exam.meta.IdEntity;
import org.knvvl.exam.repos.PictureRepository;
import org.knvvl.exam.repos.RequirementRepository;
import org.knvvl.exam.repos.TopicRepository;
import org.knvvl.exam.services.Languages;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
//@BatchSize(size=20) // TODO Setting BatchSize leads to null values for Change.question and ExamQuestion.question
@Table(name="t_question")
@Cacheable @Cache(usage = READ_WRITE)
public class Question implements IdEntity
{
    public static final String LANG_PREFIX = "Lang:";
    @Id
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "topic")
    private Topic topic;

    @ManyToOne(fetch = LAZY)
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

    @ManyToOne(fetch = LAZY)
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

    @Column(name = "language")
    private String language = LANGUAGE_NL;

    public static EntityFields<Question> getFields(TopicRepository topicRepository, RequirementRepository requirementRepository, PictureRepository pictureRepository)
    {
        return new EntityFields<>(List.of(
            new EntityField.EntityFieldIdEntity<>("topic", topicRepository, Question::getTopic, Question::setTopic),
            new EntityField.EntityFieldIdEntity<>("requirement", requirementRepository, Question::getRequirement, Question::setRequirement),
            new EntityField.EntityFieldString<>("question", Question::getQuestion, Question::setQuestion),
            new EntityField.EntityFieldString<>("answerA", Question::getAnswerA, Question::setAnswerA),
            new EntityField.EntityFieldString<>("answerB", Question::getAnswerB, Question::setAnswerB),
            new EntityField.EntityFieldString<>("answerC", Question::getAnswerC, Question::setAnswerC),
            new EntityField.EntityFieldString<>("answerD", Question::getAnswerD, Question::setAnswerD),
            new EntityField.EntityFieldString<>("answer", Question::getAnswer, Question::setAnswer),
            new EntityField.EntityFieldBoolean<>("allowB2", Question::isAllowB2, Question::setAllowB2),
            new EntityField.EntityFieldBoolean<>("allowB3", Question::isAllowB3, Question::setAllowB3),
            new EntityField.EntityFieldBoolean<>("ignore", Question::isIgnore, Question::setIgnore),
            new EntityField.EntityFieldBoolean<>("discuss", Question::isDiscuss, Question::setDiscuss),
            new EntityField.EntityFieldString<>("remarks", Question::getRemarks, Question::setRemarks),
            new EntityField.EntityFieldString<>("examGroup", Question::getExamGroup, Question::setExamGroup),
            new EntityField.EntityFieldString<>("language", Question::getLanguage, Question::setLanguage),
            new EntityField.EntityFieldIdEntity<>("picture", pictureRepository, Question::getPicture, Question::setPicture)));
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

    @Nonnull
    public String getLanguage()
    {
        return language;
    }

    @Nonnull
    private String getLanguageKeyword()
    {
        return LANG_PREFIX + getLanguage();
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

    public void setLanguage(String language)
    {
        this.language = Languages.validate(language);
    }

    public List<String> getTags(boolean asHtml)
    {
        List<String> tags = new ArrayList<>();
        if (isAllowB2())
            tags.add("B2");
        if (isAllowB3())
            tags.add("B3");
        if (isIgnore())
            tags.add("Negeren");
        if (isDiscuss())
            tags.add("Bespreken");
        tags.add(getLanguageKeyword());

        String examGroup = getExamGroup();
        if (!StringUtils.isBlank(examGroup))
            tags.add(examGroup);

        Picture picture = getPicture();
        if (picture != null)
            tags.add(asHtml
                ? "<a target=\"_blank\" href=\"/api/pictures/" + picture.getId() + "\">" + picture.getFilename() + "</a>"
                : picture.getFilename());
        return tags;
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
            || tryMatchTags(searchLower);
    }

    private boolean tryMatchTags(String searchLower)
    {
        List<String> wordsLower = Arrays.asList(searchLower.split("\\s+"));
        List<String> tagsLower = getTags(false).stream().map(String::toLowerCase).toList();
        // Each word in the user's search phrase must match a keyword
        return wordsLower.stream().allMatch(wordLower -> tagsLower.stream().anyMatch(tag -> tag.contains(wordLower)));
    }

    private boolean tryMatch(String value, String searchLower)
    {
        return value != null && value.toLowerCase().contains(searchLower);
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
