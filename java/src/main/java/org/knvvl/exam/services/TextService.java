package org.knvvl.exam.services;

import static org.knvvl.exam.entities.Text.WRITE_ONLY_VALUE;
import static org.knvvl.exam.meta.Config.EXAM_TARGET_LANGUAGE;
import static org.knvvl.exam.meta.Config.READ_ONLY_CONFIGS;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.List;

import org.knvvl.exam.entities.Text;
import org.knvvl.exam.meta.Config;
import org.knvvl.exam.repos.TextRepository;
import org.knvvl.exam.values.Languages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class TextService
{

    @Autowired
    private TextRepository textRepository;
    @Autowired
    private ChangeDetector changeDetector;

    @Transactional
    public void saveInitialTexts()
    {
        Config.CONFIGS.stream()
            .filter(t -> textRepository.findById(t.key()).isEmpty())
            .map((t -> new Text(t.key(), t.defaultValue()))) // Deep copy
            .forEach(textRepository::save);
    }

    public String get(Config staticText)
    {
        return get(staticText.key());
    }

    public String get(String key)
    {
        return textRepository.getReferenceById(key).getValue();
    }

    @Transactional
    public void save(String textKey, String value)
    {
        if (READ_ONLY_CONFIGS.stream().anyMatch(t -> t.key().equals(textKey)))
        {
            throw new IllegalArgumentException("This setting is read-only: " + textKey);
        }
        if (WRITE_ONLY_VALUE.equals(value))
        {
            return; // User accidentally saving an obfuscated password/key value
        }
        if (EXAM_TARGET_LANGUAGE.key().equals(textKey) && !isNullOrEmpty(value))
        {
            Languages.validate(value);
        }
        Text text = textRepository.getReferenceById(textKey);
        text.setValue(value);
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
