package org.knvvl.exam.lplus;

import org.knvvl.exam.entities.Question;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Converts a question {@link JsonObject} from the LPlus TestStudio GET response format
 * into the PUT request payload format.
 *
 * <p>The two formats differ in several consistent ways:
 * <ul>
 *   <li>Several fields are renamed (e.g. {@code text} → {@code questionText},
 *       {@code number} → {@code questionNumber}, {@code type} → {@code questionType})</li>
 *   <li>Relation objects are flattened to IDs
 *       ({@code topic} object → {@code topicId}, {@code targetGroups} objects → int array)</li>
 *   <li>Read-only server fields are stripped
 *       ({@code id}, {@code catalogId}, {@code isLastRevision}, layout positions, etc.)</li>
 *   <li>{@code revisionNumber} is set to {@code null} to signal a new revision</li>
 * </ul>
 */
public class LPlusQuestionConverter
{
    // HTML span template for question text (black)
    private static final String QUESTION_SPAN = "<p><span style=\"font-size: 12pt; font-family: Arial; color: #000000; line-height: 1\">%s</span></p>";

    // HTML span template for answer text (blue)
    private static final String ANSWER_SPAN = "<p><span style=\"font-size: 12pt; font-family: Arial; color: #0066CC; line-height: 1\">%s</span></p>";

    /** The LPlus language ID used for all text fields (e.g. {@code "40"} for Dutch). */
    private final String langId;

    /**
     * Creates a converter using the given language ID.
     *
     * @param langId The LPlus language ID (e.g. {@code "40"} for Dutch).
     */
    public LPlusQuestionConverter(String langId)
    {
        this.langId = langId;
    }

    /**
     * Converts a GET question response to a PUT request payload.
     *
     * @param get The {@link JsonObject} as returned by
     *            {@code GET /ltseditor/api/api/catalogs/{id}/questions/{id}}.
     * @return A new {@link JsonObject} ready to be sent as the body of
     *         {@code PUT /ltseditor/api/api/catalogs/{id}/questions/{id}}.
     */
    public JsonObject toPutPayload(JsonObject get)
    {
        JsonObject put = new JsonObject();

        // Question text: "text" -> "questionText"
        put.add("questionText", get.get("text"));

        // Question number: "number" -> "questionNumber"
        put.addProperty("questionNumber", get.get("number").getAsInt());

        // Question type string: "type" -> "questionType" (the integer code "questionType" is not sent)
        put.addProperty("questionType", get.get("type").getAsString());

        // Topic: full object -> just the ID as "topicId"
        put.addProperty("topicId", get.getAsJsonObject("topic").get("id").getAsInt());

        // Target groups: array of full objects -> array of IDs
        JsonArray targetGroupIds = new JsonArray();
        for (JsonElement tg : get.getAsJsonArray("targetGroups"))
        {
            targetGroupIds.add(tg.getAsJsonObject().get("id").getAsInt());
        }
        put.add("targetGroups", targetGroupIds);

        // Options: passed through unchanged
        put.add("options", get.get("options"));

        // Answers: rename fields, strip layout/read-only metadata
        JsonArray putAnswers = new JsonArray();
        for (JsonElement elem : get.getAsJsonArray("answers"))
        {
            putAnswers.add(convertAnswer(elem.getAsJsonObject()));
        }
        put.add("answers", putAnswers);

        // Passthrough fields (same name, same structure)
        put.add("attachments",      get.get("attachments"));
        put.add("hintText",         get.get("hintText"));
        put.add("inlineImage",      get.get("inlineImage"));
        put.add("remarks",          get.get("remarks"));
        put.add("reference",        get.get("reference"));
        put.addProperty("importKey", get.get("importKey").getAsString());
        put.addProperty("source",   get.get("source").getAsString());
        put.addProperty("locked",   get.get("locked").getAsBoolean());

        // Renamed passthrough fields
        put.add("workFiles",        get.get("questionWorkFiles"));
        put.add("solutionFiles",    get.get("questionSolutionFiles"));

        // Disassociation: flatten objects to IDs
        JsonArray disassociationIds = new JsonArray();
        for (JsonElement dq : get.getAsJsonArray("disassociationQuestions"))
        {
            disassociationIds.add(dq.getAsJsonObject().get("id").getAsInt());
        }
        put.add("disassociationIds", disassociationIds);

        // Signal to the server to create a new revision
        put.add("revisionNumber", com.google.gson.JsonNull.INSTANCE);

        // Additional PUT-only field
        put.addProperty("isOverlapInvalid", false);

        return put;
    }

    /**
     * Builds a POST payload to create a new question in LPlus from a local {@link Question}.
     *
     * <p>Unlike {@link #toPutPayload(JsonObject)}, no prior GET is required — the full
     * payload is constructed directly from the local question's fields.
     *
     * @param question      The local question to create in LPlus.
     * @param topicIndex    Index mapping {@link org.knvvl.exam.entities.Requirement#getSubdomain()}
     *                      to LPlus topic IDs. Must contain the question's subdomain.
     * @param targetGroupB2 The LPlus target group ID for brevet B2 (e.g. {@code 706}),
     *                      or {@code null} if not configured.
     * @param targetGroupB3 The LPlus target group ID for brevet B3 (e.g. {@code 707}),
     *                      or {@code null} if not configured.
     * @return A new {@link JsonObject} ready to POST to
     *         {@code POST /ltseditor/api/api/catalogs/{catalogId}/questions}.
     * @throws IllegalArgumentException if the question's requirement subdomain is not found in the topic index.
     */
    public JsonObject toPostPayload(Question question, LPlusTopicIndex topicIndex,
        LPlusAttachmentIndex attachmentIndex, Integer targetGroupB2, Integer targetGroupB3)
    {
        JsonObject post = new JsonObject();

        // Our internal question ID becomes the LPlus "questionNumber"
        post.addProperty("questionNumber", question.getId());

        post.addProperty("questionType", "MultipleChoiceResponsiveLayout");
        post.add("questionText", langMap(QUESTION_SPAN, question.getQuestion()));

        // Remarks
        JsonObject remarks = new JsonObject();
        remarks.addProperty(langId, question.getRemarks() != null ? question.getRemarks() : "");
        post.add("remarks", remarks);

        // locked <- ignore
        post.addProperty("locked", question.isIgnore());

        // Topic
        String subdomain = question.getRequirement().getSubdomain();
        int topicId = topicIndex.getLPlusId(subdomain).orElseThrow(() ->
            new IllegalArgumentException(
                "Cannot find LPlus topic for subdomain '" + subdomain
                + "' (question " + question.getId() + "). "
                + "Check that the topic tree is loaded for the correct catalog."));
        post.addProperty("topicId", topicId);

        // Target groups
        JsonArray targetGroups = new JsonArray();
        if (question.isAllowB2() && targetGroupB2 != null)
            targetGroups.add(targetGroupB2);
        if (question.isAllowB3() && targetGroupB3 != null)
            targetGroups.add(targetGroupB3);
        post.add("targetGroups", targetGroups);

        // Options
        JsonObject mcOptions = new JsonObject();
        mcOptions.addProperty("forceSingleChoice", true);
        mcOptions.addProperty("pairingAnswerChoices", false);
        mcOptions.addProperty("randomizeAnswerChoices", false);
        mcOptions.add("partMatrixScoring", com.google.gson.JsonNull.INSTANCE);

        JsonObject options = new JsonObject();
        options.addProperty("evaluationMode", "Default");
        options.addProperty("points", 1);
        options.addProperty("timeTarget", 7200);
        options.addProperty("externFileRequired", false);
        options.add("multipleChoiceOptions", mcOptions);
        post.add("options", options);

        // Answers
        post.add("answers", buildAnswers(question));

        // Empty / fixed fields
        post.add("attachments",      new JsonObject());
        post.add("hintText",         new JsonObject());
        post.add("inlineImage",      buildInlineImage(question, attachmentIndex));
        post.add("reference",        new JsonObject());
        post.add("workFiles",        new JsonObject());
        post.add("solutionFiles",    new JsonObject());
        post.add("disassociationIds", new JsonArray());
        post.addProperty("importKey", "");
        post.addProperty("source",   "");
        post.addProperty("isOverlapInvalid", false);
        post.add("revisionNumber",   com.google.gson.JsonNull.INSTANCE);

        return post;
    }

    /**
     * Overwrites the question text, answer options, remarks, topic, and target groups in a
     * PUT payload with the values from the given {@link Question}.
     *
     * <p>Plain text from {@code Question} is wrapped in the LPlus HTML span format.
     * Answer order is positional: answer 1 = A, 2 = B, 3 = C, 4 = D.
     *
     * @param put           A PUT payload previously produced by {@link #toPutPayload(JsonObject)}.
     * @param question      The local question whose values should be applied.
     * @param topicIndex    Index mapping {@link org.knvvl.exam.entities.Requirement#getSubdomain()}
     *                      to LPlus topic IDs. Must contain the question's subdomain.
     * @param targetGroupB2 The LPlus target group ID for brevet B2 (e.g. {@code 706}),
     *                      or {@code null} if not configured.
     * @param targetGroupB3 The LPlus target group ID for brevet B3 (e.g. {@code 707}),
     *                      or {@code null} if not configured.
     * @throws IllegalArgumentException if the question's requirement subdomain is not found in the topic index.
     */
    public void applyQuestion(JsonObject put, Question question, LPlusTopicIndex topicIndex,
        LPlusAttachmentIndex attachmentIndex, Integer targetGroupB2, Integer targetGroupB3)
    {
        // Overwrite question text
        put.add("questionText", langMap(QUESTION_SPAN, question.getQuestion()));

        // Overwrite remarks (plain text, not HTML-wrapped)
        JsonObject remarks = new JsonObject();
        remarks.addProperty(langId, question.getRemarks() != null ? question.getRemarks() : "");
        put.add("remarks", remarks);

        // Map ignore -> locked (ignored questions are locked in LPlus)
        put.addProperty("locked", question.isIgnore());

        // Map requirement.subdomain -> topicId
        String subdomain = question.getRequirement().getSubdomain();
        int topicId = topicIndex.getLPlusId(subdomain).orElseThrow(() ->
            new IllegalArgumentException(
                "Cannot find LPlus topic for subdomain '" + subdomain
                + "' (question " + question.getId() + "). "
                + "Check that the topic tree is loaded for the correct catalog."));
        put.addProperty("topicId", topicId);

        // Map allowB2 / allowB3 to target group IDs
        JsonArray targetGroups = new JsonArray();
        if (question.isAllowB2() && targetGroupB2 != null)
            targetGroups.add(targetGroupB2);
        if (question.isAllowB3() && targetGroupB3 != null)
            targetGroups.add(targetGroupB3);
        put.add("targetGroups", targetGroups);

        // Overwrite answers from local question fields
        put.add("answers", buildAnswers(question));

        // Inline image: map question.picture filename to LPlus attachment ID
        put.add("inlineImage", buildInlineImage(question, attachmentIndex));
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    /**
     * Builds the {@code inlineImage} object for a question payload.
     *
     * <p>If the question has an associated {@link org.knvvl.exam.entities.Picture} whose
     * filename is present in the attachment index, returns
     * {@code {"40": <attachmentId>}}. Otherwise returns an empty object {@code {}}.
     */
    private JsonObject buildInlineImage(Question question, LPlusAttachmentIndex attachmentIndex)
    {
        JsonObject inlineImage = new JsonObject();
        if (question.getPicture() == null)
            return inlineImage;

        // LPlus filename is "<pictureId>.<ext>" (e.g. "42.jpg"), so look up by picture ID (string without extension)
        String pictureIdKey = String.valueOf(question.getPicture().getId());
        int attachmentId = attachmentIndex.getLPlusId(pictureIdKey).orElseThrow(() ->
            new IllegalArgumentException(
                "Cannot find LPlus attachment for picture ID '" + pictureIdKey
                + "' (original filename: '" + question.getPicture().getFilename()
                + "', question " + question.getId() + "). "
                + "Upload the image to LPlus first and rebuild the attachment index."));
        inlineImage.addProperty(langId, attachmentId);
        return inlineImage;
    }

    /** Builds the four answer objects from the local question's A/B/C/D fields. */
    private JsonArray buildAnswers(Question question)
    {
        String correctLetter = question.getAnswer();
        String[] texts = {
            question.getAnswerA(),
            question.getAnswerB(),
            question.getAnswerC(),
            question.getAnswerD()
        };

        JsonArray answers = new JsonArray();
        for (int i = 0; i < 4; i++)
        {
            int answerNumber = i + 1;
            boolean isCorrect = correctLetter.equalsIgnoreCase(letterForIndex(answerNumber));

            JsonObject answerOptions = new JsonObject();
            answerOptions.addProperty("correctAnswerChoice", isCorrect);
            answerOptions.addProperty("points", isCorrect ? 1 : 0);

            JsonObject answer = new JsonObject();
            answer.addProperty("answerNumber", answerNumber);
            answer.add("answerText",  langMap(ANSWER_SPAN, texts[i]));
            answer.add("feedbackText", new JsonObject());
            answer.add("feedbackLink", new JsonObject());
            answer.add("inlineImage",  new JsonObject());
            answer.add("options",      answerOptions);
            answers.add(answer);
        }
        return answers;
    }

    private JsonObject convertAnswer(JsonObject getAnswer)
    {
        JsonObject putAnswer = new JsonObject();

        // "number" -> "answerNumber"
        putAnswer.addProperty("answerNumber", getAnswer.get("number").getAsInt());

        // "text" -> "answerText"
        putAnswer.add("answerText", getAnswer.get("text"));

        // Passthrough fields
        putAnswer.add("feedbackText", getAnswer.get("feedbackText"));
        putAnswer.add("feedbackLink", getAnswer.get("feedbackLink"));
        putAnswer.add("inlineImage",  getAnswer.get("inlineImage"));
        putAnswer.add("options",      getAnswer.get("options"));

        return putAnswer;
    }

    /** Wraps {@code text} in the given HTML template and returns a {@code {<langId>: "..."}} object. */
    private JsonObject langMap(String template, String text)
    {
        JsonObject map = new JsonObject();
        map.addProperty(langId, String.format(template, escapeHtml(text)));
        return map;
    }

    /** Returns "A", "B", "C", or "D" for answer numbers 1–4. */
    private static String letterForIndex(int answerNumber)
    {
        return String.valueOf((char) ('A' + answerNumber - 1));
    }

    /** Escapes the minimal set of HTML special characters present in plain question text. */
    private static String escapeHtml(String text)
    {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }
}
