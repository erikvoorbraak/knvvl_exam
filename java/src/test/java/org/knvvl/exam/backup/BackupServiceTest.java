package org.knvvl.exam.backup;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.knvvl.exam.entities.Change;
import org.knvvl.exam.entities.Exam;
import org.knvvl.exam.entities.ExamQuestion;
import org.knvvl.exam.entities.Picture;
import org.knvvl.exam.entities.Question;
import org.knvvl.exam.entities.Requirement;
import org.knvvl.exam.entities.Text;
import org.knvvl.exam.entities.Topic;
import org.knvvl.exam.entities.User;
import org.knvvl.exam.meta.IdEntity;
import org.knvvl.exam.repos.ChangeRepository;
import org.knvvl.exam.repos.ExamQuestionRepository;
import org.knvvl.exam.repos.ExamRepository;
import org.knvvl.exam.repos.PictureRepository;
import org.knvvl.exam.repos.QuestionRepository;
import org.knvvl.exam.repos.RequirementRepository;
import org.knvvl.exam.repos.TextRepository;
import org.knvvl.exam.repos.TopicRepository;
import org.knvvl.exam.repos.UserRepository;
import org.knvvl.exam.services.BackupService;
import org.knvvl.exam.services.ExamRepositories;
import org.mockito.ArgumentCaptor;
import org.springframework.data.jpa.repository.JpaRepository;

class BackupServiceTest
{
    private final TopicRepository topicRepository = mock(TopicRepository.class);
    private final RequirementRepository requirementRepository = mock(RequirementRepository.class);
    private final PictureRepository pictureRepository = mock(PictureRepository.class);
    private final ExamRepository examRepository = mock(ExamRepository.class);
    private final ExamQuestionRepository examQuestionRepository = mock(ExamQuestionRepository.class);
    private final QuestionRepository questionRepository = mock(QuestionRepository.class);
    private final ChangeRepository changeRepository = mock(ChangeRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final TextRepository textRepository = mock(TextRepository.class);

    private final ExamRepositories examRepositories = new ExamRepositories();
    private final BackupService backupService = new BackupService(examRepositories);

    @BeforeEach
    void setUp()
    {
        examRepositories.setRepositories(
            topicRepository, requirementRepository, pictureRepository, examRepository, examQuestionRepository, questionRepository,
            changeRepository, userRepository, textRepository);

        initRepository(pictureRepository, picture());
        initRepository(topicRepository, topic());
        initRepository(requirementRepository, requirement());
        initRepository(userRepository, user());
        initRepository(questionRepository, question());
        initRepository(examRepository, exam());
        initRepository(examQuestionRepository, examQuestion());
        when(changeRepository.findAll()).thenReturn(List.of(change()));
        when(textRepository.findAll()).thenReturn(List.of(text()));
    }

    private <T extends IdEntity> void initRepository(JpaRepository<T, Integer> repository, T entity)
    {
        when(repository.findAll()).thenReturn(List.of(entity));
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
    }

    private Text text()
    {
        var text = new Text();
        text.setKey("k");
        text.setLabel("L");
        return text;
    }

    private Topic topic()
    {
        var topic = new Topic("Aero");
        topic.setId(2);
        topic.setNumQuestions(20);
        return topic;
    }

    private Picture picture()
    {
        var picture = new Picture();
        picture.setId(3);
        picture.setFilename("P1.gif");
        picture.setFileData(new byte[]{2, 4, 6});
        return picture;
    }

    private Requirement requirement()
    {
        var requirement = new Requirement();
        requirement.setId(4);
        requirement.setLabel("R");
        requirement.setTopic(topic());
        requirement.setDomain(1);
        return requirement;
    }

    private User user()
    {
        var user = new User(5, "jack");
        user.setEmail("jack@google.com");
        user.setEncodedPassword("abc123");
        return user;
    }

    private Question question()
    {
        var question = new Question();
        question.setId(6);
        question.setQuestion("Q?");
        question.setTopic(topic());
        question.setRequirement(requirement());
        return question;
    }

    private Exam exam()
    {
        var exam = new Exam();
        exam.setId(7);
        exam.setLabel("B2 nov 2023");
        exam.setCertificate(2);
        exam.setLanguage("nl");
        return exam;
    }

    private ExamQuestion examQuestion()
    {
        var examQuestion = new ExamQuestion();
        examQuestion.setId(8);
        examQuestion.setExam(exam().getId());
        examQuestion.setTopic(topic());
        examQuestion.setQuestion(question());
        examQuestion.setQuestionIndex(42);
        return examQuestion;
    }

    private Change change()
    {
        var changedByAt = new Change.ChangedByAt(user(), Instant.now());
        return new Change(changedByAt, question(), "answerA", "Old", "New");
    }

    @Test
    void exportImport() throws IOException
    {
        Path file = Path.of("test.json");
        backupService.exportAll(file);
        assertNotNull(file);
        backupService.importFrom(file);
        Files.delete(file);

        verifyLoadedQuestion(); // field types and referred entities
        verifyLoadedPicture(); // bytes
    }

    private void verifyLoadedQuestion()
    {
        var questionCaptor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(questionCaptor.capture());
        var reloaded = questionCaptor.getValue();
        assertNotNull(reloaded);
        var expected = question();

        assertEquals(expected.getId(), reloaded.getId());
        assertEquals(expected.getQuestion(), reloaded.getQuestion());
        assertEquals(expected.getTopic().getId(), reloaded.getTopic().getId());
        assertEquals(expected.getRequirement().getId(), reloaded.getRequirement().getId());
    }

    private void verifyLoadedPicture()
    {
        var pictureCaptor = ArgumentCaptor.forClass(Picture.class);
        verify(pictureRepository).save(pictureCaptor.capture());
        var reloaded = pictureCaptor.getValue();
        assertNotNull(reloaded);
        var expected = picture();

        assertEquals(expected.getId(), reloaded.getId());
        assertEquals(expected.getFilename(), reloaded.getFilename());
        assertArrayEquals(expected.getFileData(), reloaded.getFileData());
    }
}