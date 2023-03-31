package org.knvvl.exam.services;

import org.knvvl.exam.entities.Picture;
import org.knvvl.exam.entities.Requirement;
import org.knvvl.exam.entities.Topic;
import org.knvvl.exam.entities.User;
import org.knvvl.exam.repos.ExamQuestionRepository;
import org.knvvl.exam.repos.ExamRepository;
import org.knvvl.exam.repos.PictureRepository;
import org.knvvl.exam.repos.QuestionRepository;
import org.knvvl.exam.repos.RequirementRepository;
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

    @Transactional
    public void addTopic(Topic topic)
    {
        if (topic.getId() == null)
        {
            Topic latest = topicRepository.findTopByOrderByIdDesc();
            topic.setId(latest == null ? 1 : latest.getId() + 1);
        }
        topicRepository.save(topic);
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
    }

    @Transactional
    public void savePicture(Picture picture)
    {
        pictureRepository.save(picture);
    }

    @Transactional
    public void deletePicture(int pictureId)
    {
        pictureRepository.deleteById(pictureId);
    }
}
