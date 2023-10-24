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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.util.Strings;
import org.knvvl.exam.entities.Exam;
import org.knvvl.exam.entities.ExamQuestion;
import org.knvvl.exam.entities.Question;
import org.knvvl.exam.entities.Text;
import org.knvvl.exam.entities.Topic;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
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
    static final int MARGIN = 52;
    static final Path fontsDir = Path.of("fonts");

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

    private void addFrontCover()
    {
        String title = textService.get(exam.getCertificate() == 2 ? EXAM_TITLE_B2 : EXAM_TITLE_B3);
        addToDocument(getHeader(title, titleFont));
        addToDocument(NEWLINE);
        addToDocument(getHeader(exam.getLabel(), bodyFont));
        addToDocument(NEWLINE);
        addToDocument(NEWLINE);
        String cover = textService.get(exam.getCertificate() == 2 ? EXAM_COVER_B2 : EXAM_COVER_B3);
        doInTable(cell -> cell.addElement(new Paragraph(cover, bodyFont)));
    }

    private void addBackCover()
    {
        grayBackground = false;
        addToDocument(NEWLINE);
        addToDocument(NEWLINE);
        addToDocument(NEWLINE);
        addToDocument(getHeader(textService.get(EXAM_BACK_TITLE), titleFont));
        addToDocument(NEWLINE);
        addToDocument(NEWLINE);
        doInTable(cell -> cell.addElement(new Paragraph(textService.get(EXAM_BACK_COVER), bodyFont)));
    }

    private void addQuestions()
    {
        int tally = 1;
        Topic topic = null;

        for (ExamQuestion question : questions)
        {
            Topic questionTopic = question.getTopic();
            if (!questionTopic.sameTopic(topic))
            {
                topic = questionTopic;
                document.newPage();
                counter.topic = questionTopic.getLabel(); // after newPage
                addToDocument(getHeader(topic.getLabel(), titleFont));
                addToDocument(NEWLINE);
                tally = 1; // reset
            }
            addQuestion(question.getQuestion(), tally);
            tally++;
            grayBackground = !grayBackground;
        }
    }

    private void addQuestion(Question question, int questionTally)
    {
        doInTable(cell -> {
            addQuestionCaption(question, cell);
            addQuestion(question, questionTally, cell);
            addAnswers(question, cell);
        });
    }

    private void addQuestionCaption(Question question, PdfPCell cell)
    {
        if (withQuestionId)
        {
            String req = question.getRequirement().getSubdomain();
            String tags = String.join(", ", question.getTags(false));
            String caption = "" + question.getId() + " " + question.getAnswer() + ", " + req + ", " + tags;
            PdfPTable table = createPdfPTable(FULL_WIDTH);
            table.addCell(getCell(caption, 1, captionFont));
            cell.addElement(table);
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
        if (createFontsDir())
        {
            extractFont(TextService.EXAM_TITLE_FONTNAME);
            extractFont(TextService.EXAM_BODY_FONTNAME);
            int registered = Files.exists(fontsDir) ? FontFactory.registerDirectory(fontsDir.toFile().getAbsolutePath()) : 0;
            System.out.println("Registered " + registered + " font(s) found in " + fontsDir);
        }
    }

    private boolean createFontsDir()
    {
        try
        {
            if (!Files.exists(fontsDir))
            {
                Files.createDirectory(fontsDir);
            }
        }
        catch (IOException e)
        {
            System.out.println("Cannot create fonts folder: " + e.getMessage());
            return false;
        }
        return true;
    }

    private void extractFont(Text fontSetting)
    {
        String fontName = textService.get(fontSetting);
        if (Strings.isBlank(fontName))
        {
            return;
        }
        String fontFileName = fontName + ".ttf";
        Path fontFile = fontsDir.resolve(fontFileName);
        if (Files.exists(fontFile))
        {
            return;
        }
        try (InputStream inputStream = getClass().getResourceAsStream("/fonts/" + fontFileName))
        {
            if (inputStream == null)
            {
                System.out.println("Could not read font resource: " + fontFileName);
                return;
            }
            try (FileOutputStream outputStream = new FileOutputStream(fontFile.toFile()))
            {
                outputStream.write(inputStream.readAllBytes());
                System.out.println("Extracted font resource: " + fontFileName);
            }
        }
        catch (IOException e)
        {
            System.out.println("Could not extract font resource: " + fontFileName + ", " + e.getMessage());
        }
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
            float targetArea = 80000;
            float currentArea = img.getWidth() * img.getHeight();
            float factor = (float)Math.sqrt(targetArea / currentArea);
            float factorShrinkOnly = Math.min(1, factor); // Don't blow up low-res pictures
            img.scalePercent(factorShrinkOnly * 100);
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

    private void doInTable(Consumer<PdfPCell> cellConsumer)
    {
        PdfPTable wrapper = createPdfPTable(FULL_WIDTH);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(NO_BORDER);
        if (grayBackground)
            cell.setBackgroundColor(BACKGROUND);

        cellConsumer.accept(cell);

        wrapper.addCell(cell);
        addToDocument(wrapper);
    }

    private void addToDocument(Element element)
    {
        try
        {
            document.add(element);
        }
        catch (DocumentException e)
        {
            throw new ExamException(e.getMessage());
        }
    }

    private static PdfPTable createPdfPTable(float... widths)
    {
        PdfPTable answers = new PdfPTable(widths);
        answers.setWidthPercentage(FULL_WIDTH);
        return answers;
    }

    private PdfPCell getCell(String contents, int paddingBottom)
    {
        return getCell(contents, paddingBottom, bodyFont);
    }

    private PdfPCell getCell(String contents, int paddingBottom, Font font)
    {
        Phrase phrase = new Phrase(contents);
        phrase.setFont(font);
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

