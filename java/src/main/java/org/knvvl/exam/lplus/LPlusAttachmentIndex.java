package org.knvvl.exam.lplus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Holds the bidirectional mapping between LPlus attachment IDs and attachment file names.
 *
 * <p>LPlus attachment ID — the {@code id} field in the attachments API response (e.g. {@code 669}).
 *
 * <p>File name — the {@code name} field in the attachments API response (e.g. {@code "Aer001.jpg"}),
 * which corresponds to {@link org.knvvl.exam.entities.Picture#getFilename()}.
 *
 * <p>Built by {@link LPlusClient#buildAttachmentIndex(int)}.
 */
public class LPlusAttachmentIndex
{
    /** Maps file name -> LPlus attachment ID. */
    private final Map<String, Integer> nameToId = new HashMap<>();

    /** Maps LPlus attachment ID -> file name. */
    private final Map<Integer, String> idToName = new HashMap<>();

    /**
     * Strips the file extension from a name, returning only the base name.
     *
     * <p>Examples:
     * <pre>
     *   "Aer001.jpg"  -> "Aer001"
     *   "photo.png"   -> "photo"
     *   "noextension" -> "noextension"
     * </pre>
     *
     * @param name A file name, with or without an extension.
     * @return The name without its extension.
     */
    public static String stripExtension(String name)
    {
        if (name == null)
            return null;
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(0, dot) : name;
    }

    /**
     * Records a mapping between a file name and an LPlus attachment ID.
     * The extension is stripped from the name before storage.
     *
     * @param name    The attachment file name (e.g. {@code "Aer001.jpg"} or {@code "Aer001"}).
     * @param lplusId The LPlus attachment ID (e.g. {@code 669}).
     */
    public void put(String name, int lplusId)
    {
        String base = stripExtension(name);
        nameToId.put(base, lplusId);
        idToName.put(lplusId, base);
    }

    /**
     * Returns the LPlus attachment ID for the given file name.
     * The extension is stripped before lookup.
     *
     * @param name The attachment file name (e.g. {@code "Aer001.jpg"} or {@code "Aer001"}).
     * @return The LPlus attachment ID, or empty if not found.
     */
    public Optional<Integer> getLPlusId(String name)
    {
        return Optional.ofNullable(nameToId.get(stripExtension(name)));
    }

    /**
     * Returns the base name (without extension) for the given LPlus attachment ID.
     *
     * @param lplusId The LPlus attachment ID.
     * @return The base name, or empty if not found.
     */
    public Optional<String> getName(int lplusId)
    {
        return Optional.ofNullable(idToName.get(lplusId));
    }

    /**
     * Returns whether the given file name has a known LPlus mapping.
     * The extension is stripped before lookup.
     *
     * @param name The attachment file name (with or without extension).
     */
    public boolean hasName(String name)
    {
        return nameToId.containsKey(stripExtension(name));
    }

    /**
     * Returns whether the given LPlus attachment ID has a known mapping.
     *
     * @param lplusId The LPlus attachment ID.
     */
    public boolean hasLPlusId(int lplusId)
    {
        return idToName.containsKey(lplusId);
    }

    /**
     * @return The set of all known file names.
     */
    public Set<String> names()
    {
        return Collections.unmodifiableSet(nameToId.keySet());
    }

    /**
     * @return The set of all known LPlus attachment IDs.
     */
    public Set<Integer> lplusIds()
    {
        return Collections.unmodifiableSet(idToName.keySet());
    }

    /**
     * @return The total number of mapped attachments.
     */
    public int size()
    {
        return nameToId.size();
    }
}
