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

    private static final List<Text> TEXTS = new ArrayList<>();

    public static final Text EXAM_TITLE_B2 = add("exam.title.b2", "KNVvL Schermvliegen - Examen Brevet 2");
    public static final Text EXAM_COVER_B2 = add("exam.cover.b2", INTRO);
    public static final Text EXAM_TITLE_B3 = add("exam.title.b3", "KNVvL Schermvliegen - Examen Brevet 3");
    public static final Text EXAM_COVER_B3 = add("exam.cover.b3", INTRO.replace("brevet 2", "brevet 3"));
    public static final Text EXAM_BACK_TITLE = add("exam.back.title", "Einde examenvragen");
    public static final Text EXAM_BACK_COVER = add("exam.back.cover", "");

    public static final Text EXAM_TITLE_FONTNAME = add("exam.title.fontname", "calibri");
    public static final Text EXAM_TITLE_FONTSIZE = add("exam.title.fontsize", "14");
    public static final Text EXAM_BODY_FONTNAME = add("exam.body.fontname", "calibri");
    public static final Text EXAM_BODY_FONTSIZE = add("exam.body.fontsize", "11");

    public static final Text EXAM_LAST_CHANGED = add("exam.last-changed", "");
    public static final Text EXAM_LAST_BACKUP = add("exam.last-backup", "");

    public static final List<Text> READ_ONLY_TEXTS = List.of(
        EXAM_LAST_CHANGED, EXAM_LAST_BACKUP);

    private static Text add(String key, String label)
    {
        Text text = new Text(key, label);
        TEXTS.add(text);
        return text;
    }

    @Autowired
    private TextRepository textRepository;
    @Autowired
    private ChangeDetector changeDetector;

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
    public void save(String textKey, String value)
    {
        if (READ_ONLY_TEXTS.stream().anyMatch(t -> t.getKey().equals(textKey)))
        {
            throw new IllegalArgumentException("This setting is read-only: " + textKey);
        }
        Text text = textRepository.getReferenceById(textKey);
        text.setLabel(value);
        textRepository.save(text);
        changeDetector.changed();
    }

    public List<Text> findAll()
    {
        return textRepository.findAll();
    }

    public Text getReferenceById(String textKey)
    {
        return textRepository.getReferenceById(textKey);
    }
}
