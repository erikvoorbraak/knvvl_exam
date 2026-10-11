package org.knvvl.exam.lplus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Holds the bidirectional mapping between LPlus topic IDs and topic numbers.
 *
 * <p>LPlus topic ID — the {@code id} field in the flat-tree API response (e.g. {@code 11244}).
 *
 * <p>Topic number — the {@code number} field in the flat-tree API response
 * (e.g. {@code "5.09.02"}), which corresponds to
 * {@link org.knvvl.exam.entities.Requirement#getDomain()} hierarchy notation.
 *
 * <p>Built by {@link LPlusClient#buildTopicIndex(int)}.
 */
public class LPlusTopicIndex
{
    /** Maps topic number -> LPlus topic ID. */
    private final Map<String, Integer> numberToId = new HashMap<>();

    /** Maps LPlus topic ID -> topic number. */
    private final Map<Integer, String> idToNumber = new HashMap<>();

    /**
     * Normalizes a topic number by stripping leading zeros from every
     * dot-separated segment, making comparisons insensitive to formatting
     * differences between systems.
     *
     * <p>Examples:
     * <pre>
     *   "1.01.01.01" -> "1.1.1.1"
     *   "5.09.02"    -> "5.9.2"
     *   "1.01.05.2"  -> "1.1.5.2"
     * </pre>
     *
     * @param number A raw topic number string.
     * @return The normalized topic number.
     */
    public static String normalize(String number)
    {
        if (number == null)
            return null;
        String[] segments = number.split("\\.", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segments.length; i++)
        {
            if (i > 0)
                sb.append('.');
            // Strip leading zeros, but keep at least one digit
            String stripped = segments[i].replaceFirst("^0+(?=\\d)", "");
            sb.append(stripped.isEmpty() ? "0" : stripped);
        }
        return sb.toString();
    }

    /**
     * Records a mapping between a topic number and an LPlus topic ID.
     * The number is normalized before storage.
     *
     * @param number  The topic number (e.g. {@code "5.09.02"} or {@code "5.9.2"}).
     * @param lplusId The LPlus topic ID (e.g. {@code 11244}).
     */
    void put(String number, int lplusId)
    {
        String normalized = normalize(number);
        numberToId.put(normalized, lplusId);
        idToNumber.put(lplusId, normalized);
    }

    /**
     * Returns the LPlus topic ID for the given topic number.
     * The number is normalized before lookup.
     *
     * @param number The topic number (e.g. {@code "5.09.02"} or {@code "5.9.2"}).
     * @return The LPlus topic ID, or empty if not found.
     */
    public Optional<Integer> getLPlusId(String number)
    {
        return Optional.ofNullable(numberToId.get(normalize(number)));
    }

    /**
     * Returns the topic number for the given LPlus topic ID.
     *
     * @param lplusId The LPlus topic ID.
     * @return The normalized topic number, or empty if not found.
     */
    public Optional<String> getNumber(int lplusId)
    {
        return Optional.ofNullable(idToNumber.get(lplusId));
    }

    /**
     * Returns whether the given topic number has a known LPlus mapping.
     * The number is normalized before lookup.
     *
     * @param number The topic number (e.g. {@code "5.09.02"} or {@code "5.9.2"}).
     */
    public boolean hasNumber(String number)
    {
        return numberToId.containsKey(normalize(number));
    }

    /**
     * Returns whether the given LPlus topic ID has a known mapping.
     *
     * @param lplusId The LPlus topic ID.
     */
    public boolean hasLPlusId(int lplusId)
    {
        return idToNumber.containsKey(lplusId);
    }

    /**
     * @return The set of all known topic numbers.
     */
    public Set<String> numbers()
    {
        return Collections.unmodifiableSet(numberToId.keySet());
    }

    /**
     * @return The set of all known LPlus topic IDs.
     */
    public Set<Integer> lplusIds()
    {
        return Collections.unmodifiableSet(idToNumber.keySet());
    }

    /**
     * @return The total number of mapped topics.
     */
    public int size()
    {
        return numberToId.size();
    }
}
