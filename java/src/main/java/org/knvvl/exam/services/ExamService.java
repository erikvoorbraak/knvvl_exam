package org.knvvl.exam.services;

import java.util.ArrayList;
import java.util.List;

import org.knvvl.exam.entities.Exam;
import org.knvvl.exam.entities.ExamQuestion;
import org.knvvl.exam.entities.Picture;
import org.knvvl.exam.entities.Question;
import org.knvvl.exam.entities.Requirement;
import org.knvvl.exam.entities.Topic;
import org.knvvl.exam.meta.Config;
import org.knvvl.exam.repos.ExamQuestionRepository;
import org.knvvl.exam.repos.ExamRepository;
import org.knvvl.exam.repos.QuestionRepository;
import org.knvvl.exam.repos.TopicRepository;
import org.knvvl.exam.values.ExamQuestions;
import org.knvvl.exam.values.PassCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class ExamService
{
    public static final int MAX_FILTERS = 3;

    @Autowired
    private ExamRepository examRepository;
    @Autowired
    private ExamQuestionRepository examQuestionRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private ChangeDetector changeDetector;
    @Autowired
    private TextService textService;
    @Autowired
    private TopicRepository topicRepository;

    @Transactional
    public void addExam(Exam exam, List<Question> questions)
    {
        if (exam.getId() == null)
            exam.setId(examRepository.findTopByOrderByIdDesc().getId() + 1);
        examRepository.save(exam);
        Integer examId = exam.getId();
        List<ExamQuestion> examQuestions = new ArrayList<>(questions.size());
        for (int i = 0; i < questions.size(); i++)
        {
            Question question = questions.get(i);
            examQuestions.add(new ExamQuestion(examId, question, question.getTopic(), i, question.getAnswer()));
        }
        examQuestionRepository.saveAll(examQuestions);
        changeDetector.changed();
    }

    /**
     *
     * @param examId
     * @return Questions ordered by how they appeared in the exam
     */
    public List<ExamQuestion> getExamQuestionsForExam(int examId)
    {
        return examQuestionRepository.findByExamOrderByQuestionIndex(examId);
    }

    public ExamQuestions getExamQuestions(int examId)
    {
        return new ExamQuestions(examId, getExamQuestionsForExam(examId));
    }

    @Transactional
    public void setAltQuestion(int examQuestionId, int altQuestionId)
    {
        ExamQuestion examQuestion = examQuestionRepository.getReferenceById(examQuestionId);
        examQuestion.setQuestion(questionRepository.getReferenceById(altQuestionId));
        examQuestionRepository.save(examQuestion);
        changeDetector.changed();
    }

    public List<Question> getAltQuestions(ExamQuestion examQuestion)
    {
        int examId = examQuestion.getExam();
        Question questionForWhichToGetAlt = examQuestion.getQuestion();
        Topic topic = questionForWhichToGetAlt.getTopic();
        Exam exam = examRepository.getReferenceById(examId);
        List<Question> allQuestionsForTopic = questionRepository.findByTopicOrderById(topic);
        List<Question> allAltQuestionsForTopic = ExamCreationService.filterForExam(allQuestionsForTopic, exam.getLanguage(), exam.getCertificate(), topic).toList();
        List<Question> questionsForTopicInExam = getExamQuestionsForExam(examId).stream()
            .filter(eq -> topic.getId().equals(eq.getTopic().getId()))
            .map(ExamQuestion::getQuestion).toList();

        List<Question> altQuestionsForTopic = getAltQuestionsForTopic(allAltQuestionsForTopic, questionsForTopicInExam);

        // Set order: first questions with same requirement, then with same topic
        List<Question> altQuestions = new ArrayList<>();
        altQuestions.add(questionForWhichToGetAlt);
        Requirement requirement = questionForWhichToGetAlt.getRequirement();
        allAltQuestionsForTopic.stream().filter(q -> q.getRequirement().equals(requirement)).forEach(altQuestions::add);
        altQuestionsForTopic.stream().filter(q -> !q.getRequirement().equals(requirement)).forEach(altQuestions::add);
        return altQuestions;
    }

    private List<Question> getAltQuestionsForTopic(List<Question> allAltQuestionsForTopic, List<Question> questionsForTopicInExam)
    {
        // As long as too few alt-questions remain, skip more filters of the similar-questions check
        List<Question> altQuestionsForTopic = new ArrayList<>();
        int howManyFilters = MAX_FILTERS;
        while (howManyFilters >= 0 && altQuestionsForTopic.size() < 10) {
            altQuestionsForTopic = getAltQuestionsForTopic(allAltQuestionsForTopic, questionsForTopicInExam, howManyFilters);
            howManyFilters--;
        }
        return altQuestionsForTopic;
    }

    private List<Question> getAltQuestionsForTopic(List<Question> allAltQuestionsForTopic, List<Question> questionsForTopicInExam, int howManyFilters)
    {
        // Start with all questions for this topic that 'fit' in this exam
        List<Question> altQuestionsForTopic = new ArrayList<>(allAltQuestionsForTopic);
        // Remove questions having same picture/group/etc as any of the questions already in the exam
        questionsForTopicInExam.forEach(q -> removeSimilarQuestions(q, altQuestionsForTopic, howManyFilters));
        // Remove questions that are already in this exam
        altQuestionsForTopic.removeAll(questionsForTopicInExam); // includes questionForWhichToGetAlt
        return altQuestionsForTopic;
    }

    @Transactional
    public void deleteExam(int examId)
    {
        List<ExamQuestion> questions = examQuestionRepository.findByExamOrderByQuestionIndex(examId);
        examQuestionRepository.deleteAll(questions);
        examRepository.delete(examRepository.getReferenceById(examId));
        changeDetector.changed();
    }

    /**
     * @param examQuestion
     * @param removeFrom
     * @param howManyFilters 0, 1, 2, 3
     */
    public static void removeSimilarQuestions(Question examQuestion, List<Question> removeFrom, int howManyFilters)
    {
        // Use any given picture only once
        if (howManyFilters > 0) {
            Picture pictureInExam = examQuestion.getPicture();
            if (pictureInExam != null)
                removeFrom.removeIf(q -> pictureInExam.equals(q.getPicture()));
        }
        // Include at most one question in a given examGroup
        if (howManyFilters > 1) {
            String examGroup = examQuestion.getExamGroup();
            if (!examGroup.isEmpty())
                removeFrom.removeIf(q -> examGroup.equals(q.getExamGroup()));
        }
        // Include one question per requirement
        if (howManyFilters > 2) {
            Requirement requirement = examQuestion.getRequirement();
            removeFrom.removeIf(q -> requirement.equals(q.getRequirement()));
        }
    }

    public PassCriteria getPassCriteria()
    {
        return new PassCriteria(
            (int)topicRepository.count(),
            Integer.parseInt(textService.get(Config.EXAM_THRESHOLD_PER_TOPIC)),
            Integer.parseInt(textService.get(Config.EXAM_THRESHOLD_OVERALL)));
    }
}
