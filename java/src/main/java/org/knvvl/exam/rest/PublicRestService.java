package org.knvvl.exam.rest;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

import java.util.Map;
import java.util.stream.Stream;

import org.knvvl.exam.entities.Picture;
import org.knvvl.exam.services.ExamCreationService;
import org.knvvl.exam.services.ExamRepositories;
import org.knvvl.exam.services.ExamToDocumentService;
import org.knvvl.exam.values.Languages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("public")
public class PublicRestService
{
    @Autowired private ExamRepositories examRepositories;
    @Autowired private ExamCreationService examCreationService;
    @Autowired private ExamToDocumentService examToDocumentService;

    @GetMapping(value = "/practice/{topicId}", produces = TEXT_HTML_VALUE)
    String createPracticeExam(@PathVariable("topicId") int topicId)
    {
        var questions = examCreationService.createPracticeExam(topicId, 10, Languages.LANGUAGE_NL);
        return examToDocumentService.generateHtml(questions);
    }

    @PostMapping(value = "/practice/{topicId}", produces = TEXT_HTML_VALUE)
    String checkPracticeExam(@PathVariable("topicId") int topicId,
        @RequestParam Map<String, String> params)
    {
        String questionIds = params.get("questions");
        var questions = Stream.of(questionIds.split(","))
            .map(Integer::parseInt)
            .map(examCreationService::getPracticeQuestion)
            .toList();
        return examToDocumentService.checkPracticeExam(topicId, questions, params);
    }

    @GetMapping(value = "/pictures/{pictureId}", produces = IMAGE_JPEG_VALUE)
    byte[] getPictureBytes(@PathVariable("pictureId") int pictureId)
    {
        try
        {
            Picture picture = examRepositories.getPictureRepository().getReferenceById(pictureId);
            return picture.getFileData();
        }
        catch (EntityNotFoundException e)
        {
            throw new ResponseStatusException(NOT_FOUND, "Picture not found in database: " + pictureId);
        }
    }
}
