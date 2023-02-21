package org.knvvl.exam.services;

import java.util.ArrayList;
import java.util.List;

import org.knvvl.exam.entities.Text;
import org.knvvl.exam.repos.TextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class TextService
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

    private static List<Text> TEXTS = new ArrayList<>();

    public static Text EXAM_TITLE_B2 = add("exam.title.b2", "KNVvL Schermvliegen - Examen Brevet 2");
    public static Text EXAM_COVER_B2 = add("exam.cover.b2", INTRO);
    public static Text EXAM_TITLE_B3 = add("exam.title.b3", "KNVvL Schermvliegen - Examen Brevet 3");
    public static Text EXAM_COVER_B3 = add("exam.cover.b3", INTRO.replace("brevet 2", "brevet 3"));
    public static Text EXAM_BACK_TITLE = add("exam.back.title", "Einde examenvragen");
    public static Text EXAM_BACK_COVER = add("exam.back.cover", "");

    public static Text EXAM_TITLE_FONTNAME = add("exam.title.fontname", "Calibri");
    public static Text EXAM_TITLE_FONTSIZE = add("exam.title.fontsize", "14");
    public static Text EXAM_BODY_FONTNAME = add("exam.body.fontname", "Calibri");
    public static Text EXAM_BODY_FONTSIZE = add("exam.body.fontsize", "11");

    private static Text add(String key, String label)
    {
        Text text = new Text(key, label);
        TEXTS.add(text);
        return text;
    }

    @Autowired private TextRepository textRepository;

    @Transactional
    public void saveInitialTexts()
    {
        TEXTS.stream()
            .filter(t -> textRepository.findById(t.getKey()).isEmpty())
            .map((t -> new Text(t.getKey(), t.getLabel()))) // Deep copy
            .forEach(textRepository::save);
    }

    public String get(Text staticText)
    {
        return get(staticText.getKey());
    }

    public String get(String key)
    {
        return textRepository.getReferenceById(key).getLabel();
    }

    @Transactional
    public void save(Text text)
    {
        textRepository.save(text);
    }
}
