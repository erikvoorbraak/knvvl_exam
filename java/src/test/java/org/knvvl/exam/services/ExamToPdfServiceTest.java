package org.knvvl.exam.services;

import static org.junit.jupiter.api.Assertions.fail;
import static org.knvvl.exam.services.ExamGenerator.BACKGROUND;
import static org.knvvl.exam.services.ExamGenerator.FULL_WIDTH;
import static org.knvvl.exam.services.Languages.LANGUAGE_NL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.knvvl.exam.entities.Exam;
import org.knvvl.exam.entities.ExamQuestion;
import org.knvvl.exam.entities.Picture;
import org.knvvl.exam.entities.Question;
import org.knvvl.exam.entities.Requirement;
import org.knvvl.exam.entities.Text;
import org.knvvl.exam.entities.Topic;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

class ExamToPdfServiceTest
{
    private final ExamService examService = mock(ExamService.class);
    private final TextService textService = mock(TextService.class);
    private final ExamToPdfService service = new ExamToPdfService();
    private static final String longText =
        "Some long text. Some long text. Some long text. Some long text. Some long text. Some long text. " +
            "Some long text. Some long text. Some long text. Some long text. Some long text.";
    private final Exam exam = new Exam("B2 jan 2023", 2, LANGUAGE_NL);
    private final Topic topic = new Topic("Materiaalkennis");
    private final Requirement requirement = new Requirement();
    private final Question question1 = new Question();
    private final Question question2 = new Question();
    private final Picture picture1 = new Picture();
    private final Picture picture2 = new Picture();

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp()
    {
        int examId = 42;
        requirement.setLabel("Kennis van...");
        requirement.setSubdomain("1.01.01");

        question1.setTopic(topic);
        question1.setRequirement(requirement);
        question1.setQuestion(longText);
        question1.setAnswerA(longText);
        question1.setAnswerB("Short B");
        question1.setAnswerC("Short C");
        question1.setAnswerD("Short D");
        picture1.setFileData(getResourceBytes("Aer001.jpg"));
        question1.setPicture(picture1);
        ExamQuestion eq1 = new ExamQuestion(42, question1, topic, 0);

        question2.setTopic(topic);
        question2.setRequirement(requirement);
        question2.setQuestion(longText);
        question2.setAnswerA(longText);
        question2.setAnswerB("Short B");
        question2.setAnswerC("Short C");
        question2.setAnswerD("Short D");
        picture2.setFileData(getResourceBytes("Large.jpg"));
        question2.setPicture(picture2);
        ExamQuestion eq2 = new ExamQuestion(42, question2, topic, 1);

        exam.setId(examId);
        service.examService = examService;
        service.textService = textService;
        when(examService.getQuestionsForExam(exam.getId())).thenReturn(List.of(eq1, eq2));
        when(textService.get(any(Text.class))).thenAnswer(i -> ((Text)i.getArgument(0)).getLabel());
    }

    @Test
    void generatePdf() throws IOException
    {
        byte[] bytes = service.generatePdf(exam, true);
        Path path = tempDir.resolve("Exam.pdf");
        Files.write(path, bytes);
        System.out.println(path.toAbsolutePath());
    }

    @Test
    void pdfTest()
    {
        doWithResource("pdfTest.pdf", document -> {
            try
            {
                addText(document);
                addTable(document);
                addImages(document);
                document.close();
            }
            catch (IOException | DocumentException e)
            {
                fail(e);
            }
        });
    }

    private void addText(Document document) throws DocumentException
    {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        Chunk chunk = new Chunk("Hello World", font);
        document.add(chunk);
        Paragraph paragraph = new Paragraph(longText, font);
        document.add(paragraph);
    }

    private void addImages(Document document) throws IOException, DocumentException
    {
        Image img = Image.getInstance(getResourceBytes("Aer001.jpg"));
        img.scaleToFit(500, 160);
        document.add(img);
        Image imgLarge = Image.getInstance(getResourceBytes("Large.jpg"));
        imgLarge.scaleToFit(500, 160);
        document.add(imgLarge);
    }

    private void addTable(Document document) throws DocumentException
    {
        PdfPTable table = new PdfPTable(new float[]{5, 95});
        table.setWidthPercentage(FULL_WIDTH);
        PdfPCell cell1 = new PdfPCell(new Phrase("1."));
        cell1.setBorder(Rectangle.NO_BORDER);
        cell1.setBackgroundColor(BACKGROUND);
        table.addCell(cell1);
        PdfPCell cell2 = new PdfPCell(new Phrase(longText));
        cell2.setBorder(Rectangle.NO_BORDER);
        cell2.setBackgroundColor(BACKGROUND);
        table.addCell(cell2);
        document.add(table);
    }
    private void doWithResource(String resourceName, Consumer<Document> generator)
    {
        File file = tempDir.resolve(resourceName).toFile();
        doWithDocument(file, generator);
    }

    private void doWithDocument(File outputFile, Consumer<Document> generator)
    {
        try (FileOutputStream stream = new FileOutputStream(outputFile))
        {
            Document document = new Document();
            PdfWriter.getInstance(document, stream);
            document.open();
            generator.accept(document);
            document.close();
        }
        catch (IOException | DocumentException e)
        {
            fail(e);
        }
        System.out.println(outputFile.getAbsolutePath()); // To view the file
    }

    private byte[] getResourceBytes(String filename)
    {
        try (var stream = getClass().getClassLoader().getResourceAsStream(filename))
        {
            return stream.readAllBytes();
        }
        catch (IOException e)
        {
            fail(e);
            return null;
        }
    }


}