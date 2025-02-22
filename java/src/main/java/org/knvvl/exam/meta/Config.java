package org.knvvl.exam.meta;

import static org.apache.commons.lang3.Validate.isTrue;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.knvvl.tools.chatgpt.Model;

public record Config(@Nonnull String key, String defaultValue, String label)
{
    private static final String INTRO = """
        Voor u ligt het theorie examen voor brevet 2 van de KNVvL afdeling schermvliegen.
                
        Dit examen bestaat uit:
            • een vragenlijst met 100 vragen over de volgende onderdelen
                o 20 vragen materiaalkennis
                o 20 vragen aerodynamica
                o 20 vragen meteorologie
                o 20 vragen regelgeving
                o 20 vragen vliegpraktijk
            • een kladvel
            • antwoordformulieren met invulinstructie
                
        Tijdens dit examen zijn slechts toegestaan:
            • schrijfmaterialen
                
        Eigen papieren zijn niet toegestaan!
                
        Vul de antwoorden op de antwoordformulieren in die u achteraan dit examen aantreft.
        Volg hierbij de instructies aan het eind van het examen (net vóór de antwoordformulieren). Voor elk vak moet een apart formulier worden ingevuld!
                
        Graag uw GSM toestel uitzetten.
        Als u klaar bent, of als de tijd verstreken is, graag de vragen, uw antwoorden en uw kladvel inleveren conform de mondelinge instructies van de examencommissie.
                
        De examenduur is 2 uur. U mag beginnen na het startsein van de examencommissie.
        Veel succes!
        """;

    public static final List<Config> CONFIGS = new ArrayList<>();

    public static final Config EXAM_TITLE_B2 = add("exam.title.b2", "KNVvL Schermvliegen - Examen Brevet 2", "B2 Exam title");
    public static final Config EXAM_COVER_B2 = add("exam.cover.b2", INTRO, "B2 Exam cover page text");
    public static final Config EXAM_TITLE_B3 = add("exam.title.b3", "KNVvL Schermvliegen - Examen Brevet 3", "B3 Exam title");
    public static final Config EXAM_COVER_B3 = add("exam.cover.b3", INTRO.replace("brevet 2", "brevet 3"), "B3 Exam cover page text");
    public static final Config EXAM_BACK_TITLE = add("exam.back.title", "Einde examenvragen", "Header to indicate the end of the exam");
    public static final Config EXAM_BACK_COVER = add("exam.back.cover", "", "Additional plain text placed at the end of the exam");
    public static final Config EXAM_TITLE_FONTNAME = add("exam.title.fontname", "calibri", "Title font name used for PDF exam");
    public static final Config EXAM_TITLE_FONTSIZE = add("exam.title.fontsize", "14", "Title font size used for PDF exam");
    public static final Config EXAM_BODY_FONTNAME = add("exam.body.fontname", "calibri", "Paragraph font name used for PDF exam");
    public static final Config EXAM_BODY_FONTSIZE = add("exam.body.fontsize", "11", "Paragraph font size used for PDF exam");
    public static final Config EXAM_LAST_CHANGED = add("exam.last-changed", "", "Date-time of last change, used for backup");
    public static final Config EXAM_LAST_BACKUP = add("exam.last-backup", "", "Date-time of last backup");
    public static final Config EXAM_THRESHOLD_PER_TOPIC = add("exam.threshold-per-topic", "65", "Minimum percentage correct per topic");
    public static final Config EXAM_THRESHOLD_OVERALL = add("exam.threshold-overall", "77", "Minimum percentage correct overall");

    public static final Config EXAM_TARGET_LANGUAGE = add("exam.target-language", "", "Default translate button translates to this language");
    public static final Config EXAM_CHATGPT_APIKEY = add("exam.chatgpt.apikey", "", "API key for ChatGPT client");
    public static final Config EXAM_CHATGPT_MODEL = add("exam.chatgpt.model", "gpt-4o-mini", "ChatGPT model used, one of " + Model.ids());
    public static final Config EXAM_CHATGPT_INSTRUCTIONS = add("exam.chatgpt.instructions",
        "Translate to {0} in the context of paragliding, not placing quotes around the result: \n{1}",
        "The prompt sent to ChatGPT for translating, parameters are: 0 = language, 1 = text to translate");

    public static final List<Config> READ_ONLY_CONFIGS = List.of(EXAM_LAST_CHANGED, EXAM_LAST_BACKUP);
    public static final List<Config> WRITE_ONLY_CONFIGS = List.of(EXAM_CHATGPT_APIKEY);

    private static Config add(String key, String defaultValue, String label)
    {
        isTrue(!isNullOrEmpty(key));
        Config text = new Config(key, defaultValue, label);
        CONFIGS.add(text);
        return text;
    }

    @Override
    public String toString()
    {
        return key;
    }
}
