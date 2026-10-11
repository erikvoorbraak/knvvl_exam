package org.knvvl.exam.lplus;

import static org.knvvl.exam.meta.Config.LPLUS_PASSWORD;
import static org.knvvl.exam.meta.Config.LPLUS_URL;
import static org.knvvl.exam.meta.Config.LPLUS_USERNAME;

import java.util.Map;

import javax.annotation.Nonnull;

import org.knvvl.exam.entities.Question;
import org.knvvl.exam.services.TextService;
import org.knvvl.tools.generic.http.Request;
import org.knvvl.tools.generic.http.SimpleHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
/**
 * HTTP client for the LPlus TestStudio editor API.
 *
 * <p>On construction the client authenticates via
 * {@code POST /ltseditor/api/api/auth} (form-urlencoded, username + password).
 * The returned JWT bearer token is stored as the default {@code Authorization}
 * header so every subsequent request is automatically authenticated.
 *
 * <p>Credentials and the base URL are read from the application's {@link TextService}
 * (keys: {@code exam.lplus.url}, {@code exam.lplus.username},
 * {@code exam.lplus.password}).
 *
 * <p>Usage example:
 * <pre>{@code
 * LPlusClient client = LPlusClient.create(textService);
 * JsonObject question = client.getQuestion(131, 31290);
 * }</pre>
 */
public class LPlusClient extends SimpleHttpClient
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LPlusClient.class);

    private static final String AUTH_PATH = "/ltseditor/api/api/auth";

    private final LPlusTokenResponse tokenResponse;

    /**
     * Creates and authenticates an {@code LPlusClient} using credentials stored
     * in the application configuration.
     *
     * @param textService The service used to read application configuration values.
     * @return An authenticated client ready for use.
     * @throws IllegalStateException if required configuration values are missing.
     */
    public static LPlusClient create(@Nonnull TextService textService)
    {
        String baseUrl  = textService.get(LPLUS_URL);
        String username = textService.get(LPLUS_USERNAME);
        String password = textService.get(LPLUS_PASSWORD);

        validateConfig("exam.lplus.url",      baseUrl);
        validateConfig("exam.lplus.username", username);
        validateConfig("exam.lplus.password", password);

        return new LPlusClient(baseUrl, username, password);
    }

    /**
     * Creates and authenticates an {@code LPlusClient} with the given credentials.
     *
     * @param baseUrl   The root URL of the LPlus TestStudio API
     *                  (e.g. {@code https://knvvl.lplus-teststudio.de}).
     * @param username  The login e-mail address.
     * @param password  The login password.
     */
    public LPlusClient(
        @Nonnull String baseUrl,
        @Nonnull String username,
        @Nonnull String password)
    {
        super(baseUrl);

        LOGGER.info("Authenticating to LPlus TestStudio editor at {}", baseUrl);

        String responseBody = sendForString(
            Request.buildPost()
                .withPath(AUTH_PATH)
                .withFormParams(Map.of(
                    "grant_type", "password",
                    "username",   username,
                    "password",   password))
                .build());

        tokenResponse = new Gson().fromJson(responseBody, LPlusTokenResponse.class);

        setDefaultHeaders(Map.of(
            Request.HEADER_AUTHORIZATION, tokenResponse.toBearerHeader()));

        LOGGER.info("Authenticated as {} (expires={})",
            tokenResponse.getUsername(),
            tokenResponse.getTokenExpiresAt());
    }

    /** @return The full token response received after authentication. */
    public LPlusTokenResponse getTokenResponse()
    {
        return tokenResponse;
    }

    /**
     * Fetches a single question from the LPlus TestStudio catalog.
     *
     * <p>Calls {@code GET /ltseditor/api/api/catalogs/{catalogId}/questions/{questionId}}.
     *
     * @param catalogId  The numeric catalog identifier (e.g. {@code 131}).
     * @param questionId The numeric question identifier (e.g. {@code 31290}).
     * @return The question as a {@link JsonObject}.
     */
    public JsonObject getQuestion(int catalogId, int questionId)
    {
        String path = "/ltseditor/api/api/catalogs/" + catalogId + "/questions/" + questionId;
        LOGGER.debug("Fetching LPlus question: catalogId={}, questionId={}", catalogId, questionId);
        String json = getForString(path);
        return new Gson().fromJson(json, JsonObject.class);
    }

    /**
     * Fetches a paged list of questions from the LPlus TestStudio catalog.     *
     * <p>Calls {@code GET /ltseditor/api/api/catalogs/{catalogId}/questions}
     * with paging, count, and language parameters.
     *
     * <p>The returned {@link JsonObject} contains:
     * <ul>
     *   <li>{@code count} — total number of matching questions in the catalog</li>
     *   <li>{@code data}  — JSON array of question summary objects for this page</li>
     * </ul>
     *
     * @param catalogId The numeric catalog identifier (e.g. {@code 131}).
     * @param skip      Number of questions to skip (0-based offset).
     * @param take      Maximum number of questions to return.
     * @param langId    Language ID filter (e.g. {@code 40} for Dutch).
     * @return A {@link JsonObject} with {@code count} and {@code data} fields.
     */
    public JsonObject getQuestions(int catalogId, int skip, int take, int langId)
    {
        String path = "/ltseditor/api/api/catalogs/" + catalogId + "/questions";
        LOGGER.debug("Fetching LPlus questions: catalogId={}, skip={}, take={}, lang={}", catalogId, skip, take, langId);
        String json = sendForString(
            Request.buildGet()
                .withPath(path)
                .withQueryParam("skip",               String.valueOf(skip))
                .withQueryParam("take",               String.valueOf(take))
                .withQueryParam("requireTotalCount",  "true")
                .withQueryParam("lang",               String.valueOf(langId))
                .build());
        return new Gson().fromJson(json, JsonObject.class);
    }

    /**
     * Fetches the flat topic tree for the given catalog and builds a bidirectional
     * index between LPlus topic IDs and topic numbers.
     *
     * <p>Calls {@code GET /ltseditor/api/api/catalogs/{catalogId}/flatTree}.
     *
     * @param catalogId The numeric catalog identifier (e.g. {@code 131}).
     * @return A populated {@link LPlusTopicIndex}.
     */
    public LPlusTopicIndex buildTopicIndex(int catalogId)
    {
        String path = "/ltseditor/api/api/catalogs/" + catalogId + "/flatTree";
        LOGGER.info("Building topic index for catalogId={}", catalogId);

        String json = getForString(path);
        com.google.gson.JsonObject response = new Gson().fromJson(json, com.google.gson.JsonObject.class);

        LPlusTopicIndex index = new LPlusTopicIndex();
        for (com.google.gson.JsonElement elem : response.getAsJsonArray("topics"))
        {
            com.google.gson.JsonObject topic = elem.getAsJsonObject();
            int    id     = topic.get("id").getAsInt();
            String number = topic.get("number").getAsString();
            index.put(number, id);
        }

        LOGGER.info("Topic index built: {} topics mapped for catalogId={}", index.size(), catalogId);
        return index;
    }

    /**
     * Fetches all questions from the given catalog in batches of 100 and builds a
     * bidirectional index between LPlus question IDs and internal question numbers.
     *
     * <p>The LPlus question ID is the {@code id} field used by the API
     * (e.g. {@code 31290}).  The internal question number is the {@code number} field
     * in the list response, which corresponds to {@link org.knvvl.exam.entities.Question#getId()}.
     *
     * @param catalogId The numeric catalog identifier (e.g. {@code 131}).
     * @return A populated {@link LPlusQuestionIndex}.
     */
    public LPlusQuestionIndex buildQuestionIndex(int catalogId)
    {
        final int batchSize = 100;
        LPlusQuestionIndex index = new LPlusQuestionIndex(catalogId);
        int skip = 0;

        LOGGER.info("Building question index for catalogId={}", catalogId);

        while (true)
        {
            JsonObject response = getQuestions(catalogId, skip, batchSize, 40);
            com.google.gson.JsonArray data = response.getAsJsonArray("data");

            if (data == null || data.isEmpty())
                break;

            for (com.google.gson.JsonElement elem : data)
            {
                com.google.gson.JsonObject q = elem.getAsJsonObject();
                int lplusId        = q.get("id").getAsInt();
                int questionNumber = q.get("number").getAsInt();
                index.put(questionNumber, lplusId);
            }

            skip += data.size();
            LOGGER.debug("Question index progress: {} so far", skip);

            // A partial batch means we have reached the last page
            if (data.size() < batchSize)
                break;
        }

        LOGGER.info("Question index built: {} questions mapped for catalogId={}", index.size(), catalogId);
        return index;
    }

    /**
     * Creates a new question in the LPlus TestStudio catalog from a local {@link Question}.
     *
     * <p>Calls {@code POST /ltseditor/api/api/catalogs/{catalogId}/questions}.
     * Our internal question ID is passed as the LPlus {@code questionNumber}.
     *
     * @param catalogId      The numeric catalog identifier (e.g. {@code 131}).
     * @param question       The local question to create in LPlus.
     * @param topicIndex     Index mapping requirement subdomains to LPlus topic IDs.
     * @param attachmentIndex Index mapping picture filenames to LPlus attachment IDs.
     * @param targetGroupB2  The LPlus target group ID for brevet B2, or {@code null} if not configured.
     * @param targetGroupB3  The LPlus target group ID for brevet B3, or {@code null} if not configured.
     * @return The LPlus question ID assigned to the newly created question.
     * @throws IllegalArgumentException if the question's requirement subdomain is not in the topic index,
     *                                  or if the question has a picture whose filename is not in the attachment index.
     */
    public int createQuestion(int catalogId, Question question,
        LPlusTopicIndex topicIndex, LPlusAttachmentIndex attachmentIndex,
        Integer targetGroupB2, Integer targetGroupB3, String langId)
    {
        LOGGER.info("Creating question {} in LPlus catalogId={}", question.getId(), catalogId);

        String path = "/ltseditor/api/api/catalogs/" + catalogId + "/questions";
        JsonObject payload = new LPlusQuestionConverter(langId).toPostPayload(
            question, topicIndex, attachmentIndex, targetGroupB2, targetGroupB3);

        String responseJson = sendForString(
            Request.buildPost()
                .withPath(path)
                .withJsonBody(new Gson().toJson(payload))
                .build());

        int lplusId = new Gson().fromJson(responseJson, JsonObject.class).get("id").getAsInt();
        LOGGER.info("Created LPlus question: catalogId={}, lplusQuestionId={}, questionNumber={}",
            catalogId, lplusId, question.getId());
        return lplusId;
    }

    /**
     * Exports a local {@link Question} to an existing LPlus TestStudio question.
     *
     * <p>The method performs three steps:
     * <ol>
     *   <li>Fetches the current LPlus question via GET.</li>
     *   <li>Converts it to a PUT payload and overwrites question text and answer options
     *       with the values from the local {@link Question}.</li>
     *   <li>Sends the updated payload back via PUT.</li>
     * </ol>
     *
     * @param catalogId       The numeric catalog identifier (e.g. {@code 131}).
     * @param lplusQuestionId The numeric LPlus question identifier (e.g. {@code 31290}).
     * @param question        The local question whose text and answers should be exported.
     * @param topicIndex      Index mapping requirement subdomains to LPlus topic IDs.
     * @param attachmentIndex Index mapping picture filenames to LPlus attachment IDs.
     * @param targetGroupB2   The LPlus target group ID for brevet B2, or {@code null} if not configured.
     * @param targetGroupB3   The LPlus target group ID for brevet B3, or {@code null} if not configured.
     * @throws IllegalArgumentException if the question's requirement subdomain is not in the topic index,
     *                                  or if the question has a picture whose filename is not in the attachment index.
     */
    public void exportQuestion(int catalogId, int lplusQuestionId, Question question,
        LPlusTopicIndex topicIndex, LPlusAttachmentIndex attachmentIndex,
        Integer targetGroupB2, Integer targetGroupB3, String langId)
    {
        LOGGER.info("Exporting question {} to LPlus catalogId={}, lplusQuestionId={}",
            question.getId(), catalogId, lplusQuestionId);

        LPlusQuestionConverter converter = new LPlusQuestionConverter(langId);

        JsonObject get = getQuestion(catalogId, lplusQuestionId);
        JsonObject put = converter.toPutPayload(get);
        converter.applyQuestion(put, question, topicIndex, attachmentIndex, targetGroupB2, targetGroupB3);
        updateQuestion(catalogId, lplusQuestionId, put);
    }

    /**
     * Updates a question in the LPlus TestStudio catalog.
     *
     * <p>Calls {@code PUT /ltseditor/api/api/catalogs/{catalogId}/questions/{questionId}}
     * with the provided payload as JSON body.
     *
     * <p>Use {@link LPlusQuestionConverter#toPutPayload(JsonObject)} to build the payload
     * from a GET response before modifying fields.
     *
     * @param catalogId  The numeric catalog identifier (e.g. {@code 131}).
     * @param questionId The numeric question identifier (e.g. {@code 31179}).
     * @param payload    The PUT request body as a {@link JsonObject}.
     */
    public void updateQuestion(int catalogId, int questionId, JsonObject payload)
    {
        String path = "/ltseditor/api/api/catalogs/" + catalogId + "/questions/" + questionId;
        LOGGER.debug("Updating LPlus question: catalogId={}, questionId={}", catalogId, questionId);
        sendForString(
            Request.buildPut()
                .withPath(path)
                .withJsonBody(new Gson().toJson(payload))
                .build());
        LOGGER.info("Updated LPlus question: catalogId={}, questionId={}", catalogId, questionId);
    }

    /**
     * Fetches all attachments from the given catalog in batches of 100 and builds a
     * bidirectional index between LPlus attachment IDs and file names.
     *
     * <p>Calls {@code GET /ltseditor/api/api/catalogs/{catalogId}/attachments}
     * with {@code skip}/{@code take} pagination. Uses {@code totalCount} from the
     * response to determine when all pages have been fetched.
     *
     * @param catalogId The numeric catalog identifier (e.g. {@code 131}).
     * @return A populated {@link LPlusAttachmentIndex}.
     */
    public LPlusAttachmentIndex buildAttachmentIndex(int catalogId)
    {
        final int batchSize = 100;
        LPlusAttachmentIndex index = new LPlusAttachmentIndex();
        int skip = 0;

        LOGGER.info("Building attachment index for catalogId={}", catalogId);

        while (true)
        {
            String path = "/ltseditor/api/api/catalogs/" + catalogId + "/attachments";
            String json = sendForString(
                Request.buildGet()
                    .withPath(path)
                    .withQueryParam("skip", String.valueOf(skip))
                    .withQueryParam("take", String.valueOf(batchSize))
                    .build());

            JsonObject response = new Gson().fromJson(json, JsonObject.class);
            com.google.gson.JsonArray data = response.getAsJsonArray("data");

            if (data == null || data.isEmpty())
                break;

            for (com.google.gson.JsonElement elem : data)
            {
                com.google.gson.JsonObject a = elem.getAsJsonObject();
                int    lplusId = a.get("id").getAsInt();
                String name    = a.get("name").getAsString();
                index.put(name, lplusId);
            }

            skip += data.size();
            LOGGER.debug("Attachment index progress: {} so far", skip);

            // A partial batch means we have reached the last page
            if (data.size() < batchSize)
                break;
        }

        LOGGER.info("Attachment index built: {} attachments mapped for catalogId={}", index.size(), catalogId);
        return index;
    }

    /**
     * Uploads a {@link org.knvvl.exam.entities.Picture} to the LPlus TestStudio catalog.
     * Delegates to {@link LPlusPictureService#uploadAttachment(LPlusClient, int, org.knvvl.exam.entities.Picture)}.
     */
    public LPlusAttachmentResponse uploadAttachment(int catalogId, org.knvvl.exam.entities.Picture picture)
    {
        return LPlusPictureService.uploadAttachment(this, catalogId, picture);
    }

    /**
     * Uploads a raw image attachment to the LPlus TestStudio catalog.
     * Delegates to {@link LPlusPictureService#uploadAttachment(LPlusClient, int, String, String, byte[])}.
     */
    public LPlusAttachmentResponse uploadAttachment(int catalogId, String fileName, String description, byte[] data)
    {
        return LPlusPictureService.uploadAttachment(this, catalogId, fileName, description, data);
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private static void validateConfig(String key, String value)
    {
        if (Strings.isNullOrEmpty(value))
        {
            throw new IllegalStateException(
                "LPlus configuration value is missing or empty: " + key);
        }
    }
}
