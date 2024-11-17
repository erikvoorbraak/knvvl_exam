package org.knvvl.tools.generic;

import javax.annotation.Nullable;

/**
 * Some general String util methods
 * 
 * @author gevmic0
 *
 */
public final class StringUtils
{
    private StringUtils()
    {   
    }

    /**
     * Truncate the specified string to the specified max
     * 
     * @param str The string
     * @param max Max chars. Positive value.
     * @return The truncated string
     */
    public static String truncate(@Nullable String str, int max)
    {
        return str != null && str.length() > max ? str.substring(0, max) : str;
    }
}
