package org.knvvl.exam.lplus;

import static org.knvvl.exam.meta.Config.LPLUS_CATALOG_ID_EN;
import static org.knvvl.exam.meta.Config.LPLUS_CATALOG_ID_NL;
import static org.knvvl.exam.meta.Config.LPLUS_LANG_ID;
import static org.knvvl.exam.meta.Config.LPLUS_PASSWORD;
import static org.knvvl.exam.meta.Config.LPLUS_TARGET_GROUP_B2;
import static org.knvvl.exam.meta.Config.LPLUS_TARGET_GROUP_B3;
import static org.knvvl.exam.meta.Config.LPLUS_URL;
import static org.knvvl.exam.meta.Config.LPLUS_USERNAME;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knvvl.exam.meta.Config;
import org.knvvl.exam.services.TextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;

/**
 * Spring service that owns the shared, lazy-initialised connection to the LPlus TestStudio API.
 *
 * <p>Each configured language gets its own {@link CatalogContext} containing the catalog ID,
 * question index, topic index, and attachment index — all catalog-specific.
 *
 * <p>The connection is created once per application lifetime (lazy singleton).
 */
@Service
public class LPlusConnection
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LPlusConnection.class);

    @Autowired
    private TextService textService;

    // Shared, lazy-initialised state
    private LPlusClient client;

    /**
     * Per-language catalog context: all indexes for one catalog.
     *
     * @param catalogId       The LPlus catalog ID for this language.
     * @param questionIndex   Bidirectional map: internal question number ↔ LPlus question ID.
     * @param topicIndex      Bidirectional map: requirement subdomain ↔ LPlus topic ID.
     * @param attachmentIndex Bidirectional map: picture ID ↔ LPlus attachment ID.
     */
    public record CatalogContext(
        int                  catalogId,
        LPlusQuestionIndex   questionIndex,
        LPlusTopicIndex      topicIndex,
        LPlusAttachmentIndex attachmentIndex)
    {}

    /**
     * Bundles all resolved LPlus settings and per-language catalog contexts.
     *
     * @param client        The authenticated LPlus API client.
     * @param catalogs      Map of language code → {@link CatalogContext}; may be empty.
     * @param targetGroupB2 LPlus target group ID for brevet B2, or {@code null}.
     * @param targetGroupB3 LPlus target group ID for brevet B3, or {@code null}.
     * @param langId        LPlus language ID for text fields (e.g. {@code "40"} for Dutch).
     */
    public record Context(
        LPlusClient                 client,
        Map<String, CatalogContext> catalogs,
        Integer                     targetGroupB2,
        Integer                     targetGroupB3,
        String                      langId)
    {
        /**
         * Returns the {@link CatalogContext} for the given question language (e.g. {@code "nl"}),
         * or {@code null} if no catalog is configured for that language.
         */
        public CatalogContext forLanguage(String language)
        {
            if (Strings.isNullOrEmpty(language))
                return null;
            return catalogs.get(language);
        }
    }

    /**
     * Returns whether LPlus integration is enabled.
     */
    public boolean isConfigured()
    {
        return !Strings.isNullOrEmpty(textService.get(LPLUS_URL));
    }

    /**
     * Resolves all LPlus settings, lazily creates the client and indexes, and returns
     * a {@link Context} ready for use.
     *
     * @return A fully initialised {@link Context}, or {@code null} if LPlus is not configured.
     * @throws IllegalStateException if the URL is set but credentials are missing.
     */
    public Context getContext()
    {
        String url = textService.get(LPLUS_URL);
        if (Strings.isNullOrEmpty(url))
            return null;

        String username = textService.get(LPLUS_USERNAME);
        String password = textService.get(LPLUS_PASSWORD);
        String langId   = textService.get(LPLUS_LANG_ID);

        requireSetting(LPLUS_USERNAME.key(), username);
        requireSetting(LPLUS_PASSWORD.key(), password);
        requireSetting(LPLUS_LANG_ID.key(),  langId);

        Integer tgB2 = parseOptionalInt(textService.get(LPLUS_TARGET_GROUP_B2));
        Integer tgB3 = parseOptionalInt(textService.get(LPLUS_TARGET_GROUP_B3));

        if (client == null)
        {
            LOGGER.info("Initialising LPlus connection to {}", url);
            client = new LPlusClient(url, username, password);
        }

        Map<String, CatalogContext> catalogs = buildCatalogs();
        return new Context(client, catalogs, tgB2, tgB3, langId);
    }

    /**
     * Returns the authenticated {@link LPlusClient}, or {@code null} if not configured.
     */
    public LPlusClient getClient()
    {
        Context ctx = getContext();
        return ctx == null ? null : ctx.client();
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private Map<String, CatalogContext> buildCatalogs()
    {
        Map<String, CatalogContext> map = new LinkedHashMap<>();
        addCatalog(map, "nl", LPLUS_CATALOG_ID_NL);
        addCatalog(map, "en", LPLUS_CATALOG_ID_EN);
        return map;
    }

    private void addCatalog(Map<String, CatalogContext> map, String language, Config catalogIdConfig)
    {
        String idStr = textService.get(catalogIdConfig);
        if (Strings.isNullOrEmpty(idStr))
            return;
        int catalogId = Integer.parseInt(idStr);
        LPlusQuestionIndex   questionIndex   = client.buildQuestionIndex(catalogId);
        LPlusTopicIndex      topicIndex      = client.buildTopicIndex(catalogId);
        LPlusAttachmentIndex attachmentIndex = client.buildAttachmentIndex(catalogId);
        map.put(language, new CatalogContext(catalogId, questionIndex, topicIndex, attachmentIndex));
        LOGGER.info("Catalog context built for language '{}': catalogId={}", language, catalogId);
    }

    private static void requireSetting(String key, String value)
    {
        if (Strings.isNullOrEmpty(value))
            throw new IllegalStateException(
                "LPlus setting '" + key + "' must be configured when '" + LPLUS_URL.key() + "' is set");
    }

    private static Integer parseOptionalInt(String value)
    {
        return Strings.isNullOrEmpty(value) ? null : Integer.parseInt(value);
    }
}
