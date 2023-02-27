package org.knvvl.exam.services;

import static org.knvvl.exam.services.TextService.EXAM_BACK_COVER;
import static org.knvvl.exam.services.TextService.EXAM_BACK_TITLE;
import static org.knvvl.exam.services.TextService.EXAM_COVER_B2;
import static org.knvvl.exam.services.TextService.EXAM_COVER_B3;
import static org.knvvl.exam.services.TextService.EXAM_TITLE_B2;
import static org.knvvl.exam.services.TextService.EXAM_TITLE_B3;

import static com.itextpdf.text.Chunk.NEWLINE;
import static com.itextpdf.text.Element.ALIGN_CENTER;
import static com.itextpdf.text.Rectangle.NO_BORDER;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import org.knvvl.exam.entities.Exam;
import org.knvvl.exam.entities.ExamQuestion;
import org.knvvl.exam.entities.Question;
import org.knvvl.exam.entities.Topic;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

class ExamGenerator
{
    static final BaseColor BACKGROUND = new BaseColor(240, 240, 240);
    static final String BOX_NAME = "page";
    static final int FULL_WIDTH = 100;
    static final int WIDTH_PERCENTAGE = 100;
    static final int MARGIN = 52;

    private final TextService textService;
    private final Exam exam;
    private final List<ExamQuestion> questions;
    private final boolean withQuestionId;
    private final Font titleFont;
    private final Font bodyFont;
    private final Font captionFont;
    private final HeaderFooterPageEvent counter;
    private boolean grayBackground = false;
    private Document document;

    ExamGenerator(TextService textService, Exam exam, List<ExamQuestion> questions, boolean withQuestionId)
    {
        this.textService = textService;
        this.exam = exam;
        this.questions = questions;
        this.withQuestionId = withQuestionId;
        this.titleFont = getTitleFont();
        this.bodyFont = getBodyFont(Font.UNDEFINED);
        this.captionFont = getBodyFont(Font.ITALIC);
        this.counter = new HeaderFooterPageEvent();
    }

    byte[] generatePdf()
    {
        registerFonts();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document = new Document(PageSize.A4);
        try
        {
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            Rectangle rect = new Rectangle(30, 30, 550, 800);
            writer.setBoxSize(BOX_NAME, rect);
            writer.setPageEvent(counter);
            document.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
            document.open();
            addFrontCover();
            addQuestions();
            counter.topic = "";
            addBackCover();
            document.close();
        }
        catch (DocumentException e)
        {
            throw new ExamException(e.getMessage());
        }
        return outputStream.toByteArray();
    }

    private void addFrontCover() throws DocumentException
    {
        String title = textService.get(exam.getCertificate() == 2 ? EXAM_TITLE_B2 : EXAM_TITLE_B3);
        document.add(getHeader(title, titleFont));
        document.add(NEWLINE);
        document.add(getHeader(exam.getLabel(), bodyFont));
        document.add(NEWLINE);
        document.add(NEWLINE);
        String cover = textService.get(exam.getCertificate() == 2 ? EXAM_COVER_B2 : EXAM_COVER_B3);
        doInTable(cell -> cell.addElement(new Paragraph(cover, bodyFont)));
    }

    private void addBackCover() throws DocumentException
    {
        grayBackground = false;
        document.add(NEWLINE);
        document.add(NEWLINE);
        document.add(NEWLINE);
        document.add(getHeader(textService.get(EXAM_BACK_TITLE), titleFont));
        document.add(NEWLINE);
        document.add(NEWLINE);
        doInTable(cell -> cell.addElement(new Paragraph(textService.get(EXAM_BACK_COVER), bodyFont)));
    }

    private void addQuestions() throws DocumentException
    {
        int tally = 1;
        Topic topic = null;

        for (ExamQuestion question : questions)
        {
            Topic questionTopic = question.getTopic();
            if (!questionTopic.equals(topic))
            {
                topic = questionTopic;
                document.newPage();
                counter.topic = questionTopic.getLabel(); // after newPage
                document.add(getHeader(topic.getLabel(), titleFont));
                document.add(NEWLINE);
                tally = 1; // reset
            }
            addQuestion(question.getQuestion(), tally);
            tally++;
            grayBackground = !grayBackground;
        }
    }

    private void addQuestion(Question question, int questionTally) throws DocumentException
    {
        addQuestionCaption(question);
        doInTable(cell -> {
            addQuestion(question, questionTally, cell);
            addAnswers(question, cell);
        });
    }

    private void addQuestionCaption(Question question) throws DocumentException
    {
        if (withQuestionId)
        {
            String req = question.getRequirement().getSubdomain();
            String caption = "" + question.getId() + " " + req + ", " + question.getTags(false);
            doInTable(cell -> cell.addElement(new Paragraph(caption, captionFont)));
        }
    }

    private void addQuestion(Question question, int questionTally, PdfPCell cell)
    {
        PdfPTable table = createPdfPTable(4, 96);
        int padding = 3;
        table.addCell(getCell("" + questionTally + ".", padding));
        table.addCell(getCell(question.getQuestion(), padding));
        if (question.getPicture() != null)
        {
            table.addCell(getCell("", 0));
            table.addCell(getImageCell(question));
        }
        cell.addElement(table);
    }

    private void addAnswers(Question question, PdfPCell cell)
    {
        PdfPTable answers = createPdfPTable(4, 4, 92);
        addAnswer("A.", question.getAnswerA(), answers);
        addAnswer("B.", question.getAnswerB(), answers);
        addAnswer("C.", question.getAnswerC(), answers);
        addAnswer("D.", question.getAnswerD(), answers);
        addBlankCell(answers);
        addBlankCell(answers);
        addBlankCell(answers);
        cell.addElement(answers);
    }

    private void registerFonts()
    {
        String fontsDir = new File("Fonts").getAbsolutePath();
        int registered = FontFactory.registerDirectory(fontsDir);
        System.out.println("Registered " + registered + " font(s) found in " + fontsDir);
    }

    private Font getTitleFont()
    {
        return FontFactory.getFont(
            textService.get(TextService.EXAM_TITLE_FONTNAME),
            Integer.parseInt(textService.get(TextService.EXAM_TITLE_FONTSIZE)),
            Font.BOLD);
    }

    private Font getBodyFont(int style)
    {
        return FontFactory.getFont(
            textService.get(TextService.EXAM_BODY_FONTNAME),
            FontFactory.defaultEncoding, FontFactory.defaultEmbedding,
            Integer.parseInt(textService.get(TextService.EXAM_BODY_FONTSIZE)),
            style);
    }

    private void addBlankCell(PdfPTable answers)
    {
        answers.addCell(getCell(" ", 0));
    }

    private PdfPCell getImageCell(Question question)
    {
        try
        {
            Image img = Image.getInstance(question.getPicture().getFileData());
            img.scaleToFit(500, 160);
            PdfPCell cell = getCell(0);
            cell.addElement(img);
            return cell;
        }
        catch (IOException | DocumentException e)
        {
            throw new ExamException(e.getMessage());
        }
    }

    private void addAnswer(String letter, String answer, PdfPTable answers)
    {
        int padding = 2;
        answers.addCell(getCell("", padding));
        answers.addCell(getCell(letter, padding));
        answers.addCell(getCell(answer, padding));
    }

    private Paragraph getHeader(String title, Font font)
    {
        Paragraph titleChunk = new Paragraph(title, font);
        titleChunk.setAlignment(ALIGN_CENTER);
        return titleChunk;
    }

    private void doInTable(Consumer<PdfPCell> cellConsumer) throws DocumentException
    {
        PdfPTable wrapper = createPdfPTable(FULL_WIDTH);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(NO_BORDER);
        if (grayBackground)
            cell.setBackgroundColor(BACKGROUND);

        cellConsumer.accept(cell);

        wrapper.addCell(cell);
        document.add(wrapper);
    }

    private static PdfPTable createPdfPTable(float... widths)
    {
        PdfPTable answers = new PdfPTable(widths);
        answers.setWidthPercentage(FULL_WIDTH);
        return answers;
    }

    private PdfPCell getCell(String contents, int paddingBottom)
    {
        Phrase phrase = new Phrase(contents);
        phrase.setFont(bodyFont);
        PdfPCell cell = getCell(paddingBottom);
        cell.addElement(phrase);
        return cell;
    }

    private static PdfPCell getCell(int paddingBottom)
    {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(NO_BORDER);
        cell.setPaddingTop(0);
        cell.setPaddingBottom(paddingBottom);
        return cell;
    }
}
