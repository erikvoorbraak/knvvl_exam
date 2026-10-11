package org.knvvl.exam.lplus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Holds the bidirectional mapping between LPlus question IDs and internal question numbers.
 *
 * <p>LPlus question ID — the identifier used by the LPlus API
 * (e.g. {@code 31290} in {@code GET .../questions/31290}).
 *
 * <p>Internal question number — the {@code number} field in the LPlus list response,
 * which corresponds to the {@code id} field of {@link org.knvvl.exam.entities.Question}.
 *
 * <p>Built by {@link LPlusClient#buildQuestionIndex(int)}.
 */
public class LPlusQuestionIndex
{
    private final int catalogId;

    /** Maps internal question number -> LPlus question ID. */
    private final Map<Integer, Integer> numberToLPlusId = new HashMap<>();

    /** Maps LPlus question ID -> internal question number. */
    private final Map<Integer, Integer> lPlusIdToNumber = new HashMap<>();

    public LPlusQuestionIndex(int catalogId)
    {
        this.catalogId = catalogId;
    }

    /** @return The LPlus catalog ID this index was built for. */
    public int getCatalogId()
    {
        return catalogId;
    }

    /**
     * Records a mapping between an internal question number and an LPlus question ID.
     *
     * @param questionNumber The internal question number (the {@code number} field in LPlus list responses).
     * @param lplusId        The LPlus question ID (the {@code id} field in LPlus API calls).
     */
    public void put(int questionNumber, int lplusId)
    {
        numberToLPlusId.put(questionNumber, lplusId);
        lPlusIdToNumber.put(lplusId, questionNumber);
    }

    /**
     * Returns the LPlus question ID for the given internal question number.
     *
     * @param questionNumber The internal question number.
     * @return The LPlus question ID, or empty if not found.
     */
    public Optional<Integer> getLPlusId(int questionNumber)
    {
        return Optional.ofNullable(numberToLPlusId.get(questionNumber));
    }

    /**
     * Returns the internal question number for the given LPlus question ID.
     *
     * @param lplusId The LPlus question ID.
     * @return The internal question number, or empty if not found.
     */
    public Optional<Integer> getQuestionNumber(int lplusId)
    {
        return Optional.ofNullable(lPlusIdToNumber.get(lplusId));
    }

    /**
     * Returns whether the given internal question number has a known LPlus mapping.
     *
     * @param questionNumber The internal question number.
     */
    public boolean hasQuestionNumber(int questionNumber)
    {
        return numberToLPlusId.containsKey(questionNumber);
    }

    /**
     * Returns whether the given LPlus question ID has a known mapping.
     *
     * @param lplusId The LPlus question ID.
     */
    public boolean hasLPlusId(int lplusId)
    {
        return lPlusIdToNumber.containsKey(lplusId);
    }

    /**
     * @return The set of all known internal question numbers.
     */
    public Set<Integer> questionNumbers()
    {
        return Collections.unmodifiableSet(numberToLPlusId.keySet());
    }

    /**
     * @return The set of all known LPlus question IDs.
     */
    public Set<Integer> lplusIds()
    {
        return Collections.unmodifiableSet(lPlusIdToNumber.keySet());
    }

    /**
     * @return The total number of mapped questions.
     */
    public int size()
    {
        return numberToLPlusId.size();
    }
}
