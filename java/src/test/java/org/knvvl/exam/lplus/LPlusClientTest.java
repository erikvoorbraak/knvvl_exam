package org.knvvl.exam.lplus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.knvvl.exam.entities.Question;
import org.knvvl.exam.entities.Requirement;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * Live integration tests against the real LPlus TestStudio editor API.
 *
 * Run with:  mvn test -pl java -Dtest=LPlusClientTest
 */
@Disabled
class LPlusClientTest
{
    private static final String BASE_URL = "https://knvvl.lplus-teststudio.de";
    private static final String USERNAME = "evoorbraak@hotmail.com";
    private static final String PASSWORD = "Voorbraak@Lplus1409";

    private static final int CATALOG_ID       = 131;
    private static final int QUESTION_ID      = 31290;
    private static final int QUESTION_ID_LIST = 31179; // Our question 407

    private LPlusClient client;

    @BeforeEach
    void setUp()
    {
        client = new LPlusClient(BASE_URL, USERNAME, PASSWORD);
    }

    @Test
    void authenticate_returnsValidToken()
    {
        LPlusTokenResponse token = client.getTokenResponse();

        assertNotNull(token.getAccessToken(),    "access_token must not be null");
        assertFalse(token.getAccessToken().isBlank(), "access_token must not be blank");
        assertEquals("Bearer", token.getTokenType(), "token_type must be 'Bearer'");
        assertTrue(token.getExpiresIn() > 0,     "expires_in must be positive");
        assertNotNull(token.getRefreshToken(),   "refresh_token must not be null");
        assertEquals(USERNAME, token.getUsername(), "username must match the login");
        assertNotNull(token.getTokenExpiresAt(), "token_expires_at must not be null");
        assertNotNull(token.getLtsAccessToken(), "lts_access_token must not be null");
        assertNotNull(token.getLtsRefreshToken(),"lts_refresh_token must not be null");

        System.out.println("Authenticated as  : " + token.getUsername());
        System.out.println("Token expires at  : " + token.getTokenExpiresAt());
        System.out.println("Expires in (sec)  : " + token.getExpiresIn());
        System.out.println("Bearer token      : " + token.getAccessToken().substring(0, 20) + "...");
    }

    @Test
    void getQuestions_returnsPagedList()
    {
        JsonObject response = client.getQuestions(CATALOG_ID, 0, 2, 40);

        assertTrue(response.get("count").getAsInt() > 0, "count must be positive");

        JsonArray data = response.getAsJsonArray("data");
        assertNotNull(data, "data must be present");
        assertEquals(2, data.size(), "must return exactly 2 questions (take=2)");

        for (int i = 0; i < data.size(); i++)
        {
            JsonObject q = data.get(i).getAsJsonObject();
            assertTrue(q.get("id").getAsInt() > 0,           "id must be positive");
            assertFalse(q.get("isInvalid").getAsBoolean(),   "question must not be invalid");
            assertNotNull(q.get("questionText").getAsString(),"questionText must be present");

            System.out.printf("  [%d] id=%-6d  number=%-5d  topic=%-50s  text=%s%n",
                i + 1,
                q.get("id").getAsInt(),
                q.get("number").getAsInt(),
                q.getAsJsonObject("topic").get("completeName").getAsString(),
                q.get("questionText").getAsString());
        }

        System.out.println("Total questions in catalog: " + response.get("count").getAsInt());
    }

    @Test
    void getQuestion_returnsExpectedQuestion()
    {
        JsonObject question = client.getQuestion(CATALOG_ID, QUESTION_ID);

        assertEquals(QUESTION_ID, question.get("id").getAsInt(),        "id must match");
        assertEquals(CATALOG_ID,  question.get("catalogId").getAsInt(), "catalogId must match");
        assertFalse(question.get("isInvalid").getAsBoolean(),           "question must not be invalid");
        assertTrue(question.get("isLastRevision").getAsBoolean(),       "must be last revision");
        assertEquals("MultipleChoiceResponsiveLayout", question.get("type").getAsString(), "type must match");

        JsonObject topic = question.getAsJsonObject("topic");
        assertNotNull(topic, "topic must be present");

        JsonArray answers = question.getAsJsonArray("answers");
        assertNotNull(answers, "answers must be present");
        assertTrue(answers.size() > 0, "must have at least one answer");

        JsonObject text = question.getAsJsonObject("text");
        assertNotNull(text, "text must be present");
        assertFalse(text.entrySet().isEmpty(), "text must have at least one language entry");
        String questionText = text.entrySet().iterator().next().getValue().getAsString();
        assertFalse(questionText.isBlank(), "question text must not be blank");

        System.out.println("Topic            : " + topic.get("completeName").getAsString());
        System.out.println("Question number  : " + question.get("number").getAsInt());
        System.out.println("Type             : " + question.get("type").getAsString());
        System.out.println("Revision         : " + question.get("revisionNumber").getAsInt());
        System.out.println("Answers          : " + answers.size());
        System.out.println("Text             : " + questionText);
    }

    @Test
    void convertQuestion_mapsGetResponseToPutPayload()
    {
        JsonObject get = client.getQuestion(CATALOG_ID, QUESTION_ID_LIST);
        JsonObject put = new LPlusQuestionConverter("40").toPutPayload(get);

        // Field renames
        assertEquals(get.get("text"),                     put.get("questionText"),   "text -> questionText");
        assertEquals(get.get("number").getAsInt(),        put.get("questionNumber").getAsInt(), "number -> questionNumber");
        assertEquals(get.get("type").getAsString(),       put.get("questionType").getAsString(), "type -> questionType");
        assertEquals(get.get("questionWorkFiles"),        put.get("workFiles"),      "questionWorkFiles -> workFiles");
        assertEquals(get.get("questionSolutionFiles"),    put.get("solutionFiles"),  "questionSolutionFiles -> solutionFiles");

        // Topic flattened to topicId
        int expectedTopicId = get.getAsJsonObject("topic").get("id").getAsInt();
        assertEquals(expectedTopicId, put.get("topicId").getAsInt(), "topic.id -> topicId");

        // Target groups flattened to array of IDs
        JsonArray getGroups = get.getAsJsonArray("targetGroups");
        JsonArray putGroups = put.getAsJsonArray("targetGroups");
        assertEquals(getGroups.size(), putGroups.size(), "targetGroups count must match");
        for (int i = 0; i < getGroups.size(); i++)
        {
            assertEquals(
                getGroups.get(i).getAsJsonObject().get("id").getAsInt(),
                putGroups.get(i).getAsInt(),
                "targetGroups[" + i + "] must be flattened to ID");
        }

        // revisionNumber must be null
        assertEquals(JsonNull.INSTANCE, put.get("revisionNumber"), "revisionNumber must be null");

        // isOverlapInvalid must be false
        assertFalse(put.get("isOverlapInvalid").getAsBoolean(), "isOverlapInvalid must be false");

        // Passthrough fields
        assertEquals(get.get("options"),     put.get("options"),     "options must pass through");
        assertEquals(get.get("attachments"), put.get("attachments"), "attachments must pass through");
        assertEquals(get.get("hintText"),    put.get("hintText"),    "hintText must pass through");
        assertEquals(get.get("remarks"),     put.get("remarks"),     "remarks must pass through");
        assertEquals(get.get("locked").getAsBoolean(), put.get("locked").getAsBoolean(), "locked must pass through");

        // Answer conversion
        JsonArray getAnswers = get.getAsJsonArray("answers");
        JsonArray putAnswers = put.getAsJsonArray("answers");
        assertEquals(getAnswers.size(), putAnswers.size(), "answer count must match");
        for (int i = 0; i < getAnswers.size(); i++)
        {
            JsonObject ga = getAnswers.get(i).getAsJsonObject();
            JsonObject pa = putAnswers.get(i).getAsJsonObject();
            assertEquals(ga.get("number").getAsInt(), pa.get("answerNumber").getAsInt(), "answer number -> answerNumber");
            assertEquals(ga.get("text"),              pa.get("answerText"),              "answer text -> answerText");
            assertEquals(ga.get("options"),           pa.get("options"),                 "answer options must pass through");
        }

        // Read-only fields must NOT be present in PUT payload
        assertFalse(put.has("id"),                          "id must be stripped");
        assertFalse(put.has("catalogId"),                   "catalogId must be stripped");
        assertFalse(put.has("isLastRevision"),              "isLastRevision must be stripped");
        assertFalse(put.has("isResponsiveLayoutAvailable"), "isResponsiveLayoutAvailable must be stripped");
        assertFalse(put.has("languageStates"),              "languageStates must be stripped");

        System.out.println("PUT payload keys : " + put.keySet());
        System.out.println("Answer count     : " + putAnswers.size());
        System.out.println("Target group IDs : " + putGroups);
        System.out.println("Topic ID         : " + put.get("topicId").getAsInt());
        System.out.println("Question type    : " + put.get("questionType").getAsString());
        System.out.println("Revision number  : " + put.get("revisionNumber"));
    }

    @Test
    void updateQuestion_setsRemarksToCurrentDateTime()
    {
        // 1. Fetch the current question
        JsonObject get = client.getQuestion(CATALOG_ID, QUESTION_ID_LIST);
        String remarksBefore = get.getAsJsonObject("remarks").entrySet().stream()
            .findFirst().map(e -> e.getValue().getAsString()).orElse("(none)");

        // 2. Build the PUT payload and overwrite "remarks"
        JsonObject put = new LPlusQuestionConverter("40").toPutPayload(get);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String newRemarks = "Saved at: " + timestamp;
        JsonObject remarks = new JsonObject();
        remarks.addProperty("40", newRemarks);
        put.add("remarks", remarks);

        // 3. Send the update
        client.updateQuestion(CATALOG_ID, QUESTION_ID_LIST, put);

        // 4. Fetch again and verify the remarks were persisted
        JsonObject updated = client.getQuestion(CATALOG_ID, QUESTION_ID_LIST);
        String remarksAfter = updated.getAsJsonObject("remarks").get("40").getAsString();

        assertEquals(newRemarks, remarksAfter, "remarks must be updated to the new value");

        System.out.println("Remarks before   : " + remarksBefore);
        System.out.println("Remarks after    : " + remarksAfter);
    }

    @Test
    void exportQuestion_updatesQuestionTextAndAnswers()
    {
        // Build a local Question with known content matching question 31179
        // Topic for this question in catalog 131: "4.05.02 Algemeen" (LPlus topic ID 11163)
        Requirement requirement = new Requirement();
        requirement.setSubdomain("4.05.02");

        Question q = new Question();
        q.setId(QUESTION_ID_LIST);
        q.setRequirement(requirement);
        q.setQuestion("Welke documenten dien je volgens het Reglement Schermvliegen tijdens of kort na het paragliden bij je te hebben?");
        q.setAnswerA("Je brevet, medische verklaring en logboek.");
        q.setAnswerB("Je brevet en een ICAO kaart van het gebied waarin je vliegt.");
        q.setAnswerC("Je brevet, medische verklaring en verzekeringsbewijs.");
        q.setAnswerD("Geen van bovenstaande antwoorden is correct.");
        q.setAnswer("A");

        // Build the topic index and export
        LPlusTopicIndex topicIndex = client.buildTopicIndex(CATALOG_ID);
        client.exportQuestion(CATALOG_ID, QUESTION_ID_LIST, q, topicIndex, null, 706, 707, "40");

        // Fetch back and verify text was persisted
        JsonObject updated = client.getQuestion(CATALOG_ID, QUESTION_ID_LIST);

        String updatedQuestionHtml = updated.getAsJsonObject("text").get("40").getAsString();
        assertTrue(updatedQuestionHtml.contains("Reglement Schermvliegen"), "question text must be present in HTML");

        JsonArray answers = updated.getAsJsonArray("answers");
        assertEquals(4, answers.size(), "must have 4 answers");

        // Answer 1 (A) must be marked correct
        JsonObject answer1 = answers.get(0).getAsJsonObject();
        assertTrue(answer1.getAsJsonObject("options").get("correctAnswerChoice").getAsBoolean(), "answer A must be correct");
        assertTrue(answer1.getAsJsonObject("text").get("40").getAsString().contains("logboek"), "answer A text must be present");

        // Answers 2, 3, 4 must be incorrect
        for (int i = 1; i < 4; i++)
        {
            JsonObject answer = answers.get(i).getAsJsonObject();
            assertFalse(answer.getAsJsonObject("options").get("correctAnswerChoice").getAsBoolean(),
                "answer " + (i + 1) + " must not be correct");
        }

        System.out.println("Question text    : " + updatedQuestionHtml);
        for (int i = 0; i < answers.size(); i++)
        {
            JsonObject a = answers.get(i).getAsJsonObject();
            int num = a.get("number").getAsInt(); // GET response uses "number", not "answerNumber"
            System.out.printf("Answer %d (%s)   : correct=%-5s  text=%s%n",
                num,
                letterForIndex(num),
                a.getAsJsonObject("options").get("correctAnswerChoice").getAsBoolean(),
                a.getAsJsonObject("text").get("40").getAsString());
        }
    }

    @Test
    void buildTopicIndex_mapsAllTopics()
    {
        LPlusTopicIndex index = client.buildTopicIndex(CATALOG_ID);

        assertTrue(index.size() > 0, "index must not be empty");

        // Normalization: all three variants must resolve to the same LPlus ID
        int knownId = 11244;
        assertEquals(knownId, index.getLPlusId("5.09.02").orElseThrow(),  "LPlus format must resolve");
        assertEquals(knownId, index.getLPlusId("5.9.2").orElseThrow(),    "normalized format must resolve");
        assertEquals(knownId, index.getLPlusId("5.09.002").orElseThrow(), "extra zeros must also resolve");

        // Stored number is always the normalized form
        assertEquals("5.9.2", index.getNumber(knownId).orElseThrow(), "stored number must be normalized");

        // hasNumber is also normalization-aware
        assertTrue(index.hasNumber("5.09.02"), "hasNumber must work with original format");
        assertTrue(index.hasNumber("5.9.2"),   "hasNumber must work with normalized format");

        // Lookups for unknown values return empty
        assertTrue(index.getLPlusId("9.99.99").isEmpty(), "unknown number must return empty");
        assertTrue(index.getNumber(-1).isEmpty(),          "unknown LPlus ID must return empty");

        // Standalone normalization examples
        assertEquals("1.1.1.1", LPlusTopicIndex.normalize("1.01.01.01"), "leading zeros stripped");
        assertEquals("5.9.2",   LPlusTopicIndex.normalize("5.09.02"),    "leading zeros stripped");
        assertEquals("1.1.5.2", LPlusTopicIndex.normalize("1.01.05.2"),  "mixed segments normalized");

        System.out.println("Total mapped     : " + index.size());
        System.out.println("5.09.02 -> LPlus ID " + index.getLPlusId("5.09.02").orElseThrow());
        System.out.println("LPlus ID " + knownId + "  -> number " + index.getNumber(knownId).orElseThrow());
    }

    @Test
    void buildQuestionIndex_mapsAllQuestions()
    {
        LPlusQuestionIndex index = client.buildQuestionIndex(CATALOG_ID);

        assertTrue(index.size() > 0, "index must not be empty");

        // Known mapping from catalog 131: LPlus ID 31179 <-> internal number 407
        int knownLPlusId        = QUESTION_ID_LIST; // 31179
        int knownQuestionNumber = 407;

        assertTrue(index.hasLPlusId(knownLPlusId),
            "index must contain LPlus ID " + knownLPlusId);
        assertTrue(index.hasQuestionNumber(knownQuestionNumber),
            "index must contain question number " + knownQuestionNumber);

        assertEquals(knownLPlusId,        index.getLPlusId(knownQuestionNumber).orElseThrow(),
            "number " + knownQuestionNumber + " must map to LPlus ID " + knownLPlusId);
        assertEquals(knownQuestionNumber, index.getQuestionNumber(knownLPlusId).orElseThrow(),
            "LPlus ID " + knownLPlusId + " must map to number " + knownQuestionNumber);

        // Lookups for unknown values return empty
        assertTrue(index.getLPlusId(-1).isEmpty(),       "unknown number must return empty");
        assertTrue(index.getQuestionNumber(-1).isEmpty(), "unknown LPlus ID must return empty");

        System.out.println("Total mapped     : " + index.size());
        System.out.println("LPlus ID " + knownLPlusId + " -> number " + index.getQuestionNumber(knownLPlusId).orElseThrow());
        System.out.println("Number " + knownQuestionNumber + "    -> LPlus ID " + index.getLPlusId(knownQuestionNumber).orElseThrow());
    }

    @Test
    void buildAttachmentIndex_mapsAllAttachments()
    {
        LPlusAttachmentIndex index = client.buildAttachmentIndex(CATALOG_ID);

        assertTrue(index.size() > 0, "index must not be empty");

        // Known attachment uploaded during earlier tests: id=669, name="Aer001.jpg"
        int    knownId       = 669;
        String knownName     = "Aer001.jpg";
        String knownBaseName = "Aer001";       // extension-stripped form stored in the index

        assertTrue(index.hasLPlusId(knownId),       "index must contain LPlus ID " + knownId);

        // hasName and getLPlusId accept both the full filename and the base name
        assertTrue(index.hasName(knownName),         "hasName must work with extension");
        assertTrue(index.hasName(knownBaseName),     "hasName must work without extension");

        assertEquals(knownId, index.getLPlusId(knownName).orElseThrow(),
            "lookup with extension must resolve to LPlus ID " + knownId);
        assertEquals(knownId, index.getLPlusId(knownBaseName).orElseThrow(),
            "lookup without extension must resolve to LPlus ID " + knownId);

        // getName returns the stored base name (no extension)
        assertEquals(knownBaseName, index.getName(knownId).orElseThrow(),
            "stored name must be the base name without extension");

        // Standalone stripExtension examples
        assertEquals("Aer001",  LPlusAttachmentIndex.stripExtension("Aer001.jpg"),  "jpg stripped");
        assertEquals("photo",   LPlusAttachmentIndex.stripExtension("photo.png"),   "png stripped");
        assertEquals("archive", LPlusAttachmentIndex.stripExtension("archive"),     "no extension unchanged");

        // Lookups for unknown values return empty
        assertTrue(index.getLPlusId("does-not-exist.jpg").isEmpty(), "unknown name must return empty");
        assertTrue(index.getName(-1).isEmpty(),                       "unknown LPlus ID must return empty");

        System.out.println("Total mapped     : " + index.size());
        System.out.println("Name " + knownName + " -> LPlus ID " + index.getLPlusId(knownName).orElseThrow());
        System.out.println("LPlus ID " + knownId + "    -> name " + index.getName(knownId).orElseThrow());
    }

    @Test
    void uploadAttachment_returnsAttachmentResponse() throws IOException
    {
        byte[] imageBytes;
        try (InputStream is = getClass().getResourceAsStream("/Aer001.jpg"))
        {
            assertNotNull(is, "Test resource Aer001.jpg must exist on classpath");
            imageBytes = is.readAllBytes();
        }

        // Use a timestamp in the filename to avoid "already exists" rejection from the API
        String fileName = "Aer001_" + System.currentTimeMillis() + ".jpg";
        LPlusAttachmentResponse response = LPlusPictureService.uploadAttachment(client, CATALOG_ID, fileName, fileName, imageBytes);

        assertTrue(response.getId() > 0,              "id must be positive");
        assertEquals("Image",    response.getItemType(),  "itemType must be 'Image'");
        assertEquals("image/jpeg", response.getMediaType(), "mediaType must be image/jpeg");
        assertTrue(response.getFileSize() > 0,        "fileSize must be positive");
        assertTrue(response.isLastRevision(),          "must be last revision");
        assertFalse(response.isDeleted(),              "must not be deleted");
        assertNotNull(response.getName(),              "name must not be null");
        assertNotNull(response.getLastEditedDateUTC(), "lastEditedDateUTC must not be null");

        System.out.println("Attachment ID    : " + response.getId()); // Example: 668
        System.out.println("Name             : " + response.getName());
        System.out.println("Media type       : " + response.getMediaType());
        System.out.println("File size        : " + response.getFileSize() + " bytes");
        System.out.println("Revision         : " + response.getRevisionNumber());
        System.out.println("Last edited UTC  : " + response.getLastEditedDateUTC());
    }

    private static String letterForIndex(int answerNumber)
    {
        return String.valueOf((char) ('A' + answerNumber - 1));
    }
}
