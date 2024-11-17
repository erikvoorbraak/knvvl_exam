package org.knvvl.exam.services;

import static java.util.stream.Collectors.joining;

import java.util.List;

import javax.annotation.Nonnull;

public class Languages
{
    public static final Language LANGUAGE_EN = new Language("en", "English");
    public static final Language LANGUAGE_NL = new Language("nl", "Dutch");
    public static final List<Language> LANGUAGES = List.of(LANGUAGE_EN, LANGUAGE_NL);
    private static final String LANGUAGES_JOINED = LANGUAGES.stream().map(Language::id).collect(joining(","));

    public record Language(String id, String label)
    {
    }

    @Nonnull
    public static Language get(String language)
    {
        return LANGUAGES.stream()
            .filter(l -> l.id().equals(language))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Language should be one of " + LANGUAGES_JOINED));
    }

    @Nonnull
    public static String validate(String language)
    {
        return LANGUAGES.stream()
            .map(Language::id)
            .filter(language::equals)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Language should be one of " + LANGUAGES_JOINED));
    }
}
