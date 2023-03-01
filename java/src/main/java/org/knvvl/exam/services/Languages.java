package org.knvvl.exam.services;

import java.util.List;

public class Languages
{
    public static final String LANGUAGE_EN = "en";
    public static final String LANGUAGE_NL = "nl";
    public static final List<String> LANGUAGES = List.of(LANGUAGE_EN, LANGUAGE_NL);

    public static String validate(String language)
    {
        if (!LANGUAGES.contains(language))
        {
            throw new IllegalArgumentException("Language should be one of " + String.join(",", LANGUAGES));
        }
        return language;
    }
}
