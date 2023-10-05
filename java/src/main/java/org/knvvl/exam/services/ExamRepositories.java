package org.knvvl.exam.services;

import java.util.List;

import org.knvvl.exam.entities.Change;
import org.knvvl.exam.entities.Exam;
import org.knvvl.exam.entities.ExamQuestion;
import org.knvvl.exam.entities.Picture;
import org.knvvl.exam.entities.Question;
import org.knvvl.exam.entities.Requirement;
import org.knvvl.exam.entities.Text;
import org.knvvl.exam.entities.Topic;
import org.knvvl.exam.entities.User;
import org.knvvl.exam.meta.EntityHandler;
import org.knvvl.exam.repos.ChangeRepository;
import org.knvvl.exam.repos.ExamQuestionRepository;
import org.knvvl.exam.repos.ExamRepository;
import org.knvvl.exam.repos.PictureRepository;
import org.knvvl.exam.repos.QuestionRepository;
import org.knvvl.exam.repos.RequirementRepository;
import org.knvvl.exam.repos.TextRepository;
import org.knvvl.exam.repos.TopicRepository;
import org.knvvl.exam.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class ExamRepositories
{
    public static final Sort SORT_BY_ID = Sort.by("id");
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private RequirementRepository requirementRepository;
    @Autowired
    private PictureRepository pictureRepository;
    @Autowired
    private ExamRepository examRepository;
    @Autowired
    private ExamQuestionRepository examQuestionRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private ChangeRepository changeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TextRepository textRepository;
    @Autowired
    private ChangeDetector changeDetector;

    public void setRepositories(
        TopicRepository topicRepository,
        RequirementRepository requirementRepository,
        PictureRepository pictureRepository,
        ExamRepository examRepository,
        ExamQuestionRepository examQuestionRepository,
        QuestionRepository questionRepository,
        ChangeRepository changeRepository,
        UserRepository userRepository,
        TextRepository textRepository)
    {
        this.topicRepository = topicRepository;
        this.requirementRepository = requirementRepository;
        this.pictureRepository = pictureRepository;
        this.examRepository = examRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.questionRepository = questionRepository;
        this.changeRepository = changeRepository;
        this.userRepository = userRepository;
        this.textRepository = textRepository;
    }

    public PictureRepository getPictureRepository()
    {
        return pictureRepository;
    }

    public ExamRepository getExamRepository()
    {
        return examRepository;
    }

    public ExamQuestionRepository getExamQuestionRepository()
    {
        return examQuestionRepository;
    }

    public QuestionRepository getQuestionRepository()
    {
        return questionRepository;
    }

    public boolean isEmpty()
    {
        return getEntityHandlers().stream().allMatch(EntityHandler::isEmpty);
    }

    public List<EntityHandler<?>> getEntityHandlers()
    {
        var requirementFields = Requirement.getFields(topicRepository);
        var questionFields = Question.getFields(topicRepository, requirementRepository, pictureRepository);
        var examQuestionFields = ExamQuestion.getFields(questionRepository, topicRepository);
        var changeFields = Change.getFields(userRepository, questionRepository);

        return List.of(
            new EntityHandler<>(textRepository, Text.getFields(), "texts", Text::new),
            new EntityHandler<>(userRepository, User.getFields(), "users", User::new),
            new EntityHandler<>(pictureRepository, Picture.getFields(),"pictures", Picture::new),
            new EntityHandler<>(topicRepository, Topic.getFields(),"topics", Topic::new),
            new EntityHandler<>(requirementRepository, requirementFields, "requirements", Requirement::new),
            new EntityHandler<>(questionRepository, questionFields, "questions", Question::new),
            new EntityHandler<>(examRepository, Exam.getFields(), "exams", Exam::new),
            new EntityHandler<>(examQuestionRepository, examQuestionFields, "examQuestions", ExamQuestion::new),
            new EntityHandler<>(changeRepository, changeFields, "changes", Change::newChangeForJsonImport));
    }

    @Transactional
    public void addTopic(Topic topic)
    {
        if (topic.getId() == null)
        {
            Topic latest = topicRepository.findTopByOrderByIdDesc();
            topic.setId(latest == null ? 1 : latest.getId() + 1);
        }
        topicRepository.save(topic);
        changeDetector.changed();
    }

    @Transactional
    public void addRequirement(Requirement requirement)
    {
        if (requirement.getId() == null)
        {
            Requirement latest = requirementRepository.findTopByOrderByIdDesc();
            requirement.setId(latest == null ? 1 : latest.getId() + 1);
        }
        requirementRepository.save(requirement);
        changeDetector.changed();
    }

    @Transactional
    public void addPicture(Picture picture)
    {
        if (picture.getId() == null)
        {
            Picture latest = pictureRepository.findTopByOrderByIdDesc();
            picture.setId(latest == null ? 1 : latest.getId() + 1);
        }
        pictureRepository.save(picture);
        changeDetector.changed();
    }

    @Transactional
    public void savePicture(Picture picture)
    {
        pictureRepository.save(picture);
        changeDetector.changed();
    }

    @Transactional
    public void deletePicture(int pictureId)
    {
        pictureRepository.deleteById(pictureId);
        changeDetector.changed();
    }
}
