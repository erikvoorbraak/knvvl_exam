package org.knvvl.exam.services;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

import static org.knvvl.exam.services.Languages.LANGUAGE_EN;
import static org.knvvl.exam.services.Languages.LANGUAGE_NL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.knvvl.exam.entities.Exam;
import org.knvvl.exam.entities.ExamQuestion;
import org.knvvl.exam.entities.Picture;
import org.knvvl.exam.entities.Question;
import org.knvvl.exam.entities.Requirement;
import org.knvvl.exam.entities.Topic;
import org.knvvl.exam.repos.ExamQuestionRepository;
import org.knvvl.exam.repos.ExamRepository;
import org.knvvl.exam.repos.PictureRepository;
import org.knvvl.exam.repos.QuestionRepository;
import org.knvvl.exam.repos.RequirementRepository;
import org.knvvl.exam.repos.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import jakarta.annotation.PostConstruct;

@Service
public class ImportExportService
{
    public static final int DEFAULT_NUM_QUESTIONS = 20;
    @Autowired private TextService textService;
    @Autowired private ExamQuestionRepository examQuestionRepository;
    @Autowired private ExamRepository examRepository;
    @Autowired private PictureRepository pictureRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private RequirementRepository requirementRepository;
    @Autowired private TopicRepository topicRepository;

    @Value("${exam.importInitial}") boolean importInitial = true;

    @PostConstruct
    public void init()
    {
        textService.saveInitialTexts();
        if (importInitial && topicRepository.count() == 0)
        {
            importInitial();
        }
        importQuestionTranslations("Questions_en.txt", LANGUAGE_EN);
    }

    private void importQuestionTranslations(String fileName, String language)
    {
        File importDir = new File("ImportTranslations");
        File importFile = new File(importDir, fileName);
        System.out.println("Looking for more CSV question translations in: " + importFile.getAbsolutePath());

        if (importFile.exists())
        {
            readCSV(importFile).forEach(r -> importQuestionTranslation(r, language));
            try
            {
                Files.delete(importFile.toPath());
            }
            catch (IOException e)
            {
                System.out.println("Error deleting " + importFile + ": " + e.getMessage());
            }
        }
    }

    private void importQuestionTranslation(List<String> line, String language)
    {
        var lineCopy = new ArrayList<>(line);
        lineCopy.set(0, String.valueOf(questionRepository.findTopByOrderByIdDesc().getId() + 1));
        lineCopy.set(14, "Translation of " + line.get(0));
        importQuestion(lineCopy, language);
    }

    private void importInitial()
    {
        File importDir = new File("ImportInitial");
        File filesDir = new File(importDir, "Afbeeldingen");
        System.out.println("Looking for initial CSV data files in: " + importDir.getAbsolutePath());
        System.out.println("Looking for picture files in: " + filesDir.getAbsolutePath());

        if (importDir.exists() && importDir.isDirectory())
        {
            readCSV(new File(importDir, "Afbeeldingen.txt")).forEach(r -> importPicture(r, filesDir));
            readCSV(new File(importDir, "Vakken.txt")).forEach(this::importTopic);
            readCSV(new File(importDir, "Exameneisen.txt")).forEach(this::importRequirement);
            readCSV(new File(importDir, "Vragen.txt")).forEach(l -> importQuestion(l, LANGUAGE_NL));
            List<Topic> topics = topicRepository.findAll();
            List<Question> questions = questionRepository.findAll();
            readCSV(new File(importDir, "GegenereerdeExamens.txt")).forEach(l -> importExam(l, questions, topics));
            System.out.println("Import completed");
        }
        else
        {
            System.out.println("Nothing found to import");
        }
    }

    private void importPicture(List<String> line, File filesDir)
    {
        Picture picture = new Picture();
        picture.setId(toInt(line.get(0)));
        String filename = line.get(1).trim();
        picture.setFilename(filename);
        readPictureFile(filesDir, picture, filename);
        pictureRepository.save(picture);
    }

    private static void readPictureFile(File filesDir, Picture picture, String filename)
    {
        File pictureFile = new File(filesDir, filename + ".jpg");
        if (pictureFile.exists())
        {
            try
            {
                byte[] bytes = Files.readAllBytes(pictureFile.toPath());
                picture.setFileData(bytes);
            }
            catch (IOException e)
            {
                System.out.println("Error reading file: " + pictureFile.getAbsolutePath() + ": " + e.getMessage());
            }
        }
        else
        {
            System.out.println("Expected file not found: " + pictureFile.getAbsolutePath());
        }
    }

    private void importTopic(List<String> line)
    {
        Topic topic = new Topic();
        topic.setId(toInt(line.get(0)));
        topic.setLabel(line.get(1));
        topic.setNumQuestions(DEFAULT_NUM_QUESTIONS);
        topicRepository.save(topic);
    }

    private void importRequirement(List<String> line)
    {
        Requirement requirement = new Requirement();
        requirement.setId(toInt(line.get(0)));
        requirement.setTopic(topicRepository.getReferenceById(toInt(line.get(1))));
        requirement.setDomain(toInt(line.get(2)));
        requirement.setDomainTitle(line.get(3));
        requirement.setSubdomain(line.get(4));
        requirement.setLabel(line.get(5));
        requirement.setLevelB2(line.get(6));
        requirement.setLevelB3(line.get(7));
        requirementRepository.save(requirement);
    }

    private void importQuestion(List<String> line, String language)
    {
        Question question = new Question();
        question.setId(toInt(line.get(0)));
        question.setTopic(topicRepository.getReferenceById(toInt(line.get(1))));
        question.setRequirement(requirementRepository.getReferenceById(toInt(line.get(2))));
        question.setQuestion(line.get(4).trim());
        question.setAnswerA(line.get(5).trim());
        question.setAnswerB(line.get(6).trim());
        question.setAnswerC(line.get(7).trim());
        question.setAnswerD(line.get(8).trim());
        question.setAnswer(line.get(9).trim());
        question.setPicture(line.get(11).isEmpty() ? null : pictureRepository.getReferenceById(toInt(line.get(11))));
        question.setIgnore("1".equals(line.get(12)));
        boolean onlyB3 = "1".equals(line.get(13));
        question.setAllowB2(!onlyB3);
        question.setAllowB3(true);
        question.setRemarks(line.get(14));
        question.setLanguage(language);
        questionRepository.save(question);
    }

    private void importExam(List<String> line, List<Question> questions, List<Topic> topics)
    {
        Exam exam = new Exam();
        exam.setId(toInt(line.get(0)));
        exam.setCertificate(toInt(line.get(1)));
        exam.setLabel(line.get(2));
        examRepository.save(exam);

        String[] examQuestions = line.get(3).split("[,]");
        for (int i = 0; i < DEFAULT_NUM_QUESTIONS * 5; i++)
        {
            int topicIc = i / DEFAULT_NUM_QUESTIONS + 1;
            int questionId = toInt(examQuestions[i]);
            Topic topic = topics.stream().filter(t -> t.getId().equals(topicIc)).findFirst().orElseThrow();
            Question question = questions.stream().filter(q -> q.getId().equals(questionId)).findFirst().orElseThrow();
            ExamQuestion examQuestion = new ExamQuestion(exam.getId(), question, topic, i + 1);
            examQuestionRepository.save(examQuestion);
        }
    }

    private int toInt(String value)
    {
        return (int)Double.parseDouble(value);
    }

    private List<List<String>> readCSV(File csvFile)
    {
        try (CSVReader csvReader = new CSVReader(new FileReader(csvFile, ISO_8859_1));) {
            List<List<String>> records = new ArrayList<>();
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                records.add(Arrays.asList(values));
            }
            System.out.println("Read " + records.size() + " rows from " + csvFile);
            return records;
        }
        catch (FileNotFoundException e)
        {
            System.out.println("File not found: " + csvFile);
        }
        catch (IOException e)
        {
            System.out.println("IO exception reading " + csvFile + ": " + e.getMessage());
        }
        catch (CsvValidationException e)
        {
            System.out.println("CSV validation error reading " + csvFile + ": " + e.getMessage());
        }
        return Collections.emptyList();
    }
}
