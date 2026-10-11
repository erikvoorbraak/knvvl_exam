package org.knvvl.exam.lplus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.knvvl.exam.entities.Change;
import org.knvvl.exam.entities.Change.ChangedByAt;
import org.knvvl.exam.entities.Question;
import org.knvvl.exam.repos.ChangeRepository;
import org.knvvl.exam.repos.QuestionRepository;
import org.knvvl.exam.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Spring service responsible for all LPlus-related operations on {@link Question} entities.
 *
 * <p>Questions are exported to the LPlus catalog matching their {@link Question#getLanguage()} field.
 * If the language is blank, or no catalog is configured for that language, the export is silently skipped.
 */
@Service
public class LPlusQuestionService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LPlusQuestionService.class);

    static final String LPLUS_CREATED = "lplusCreated";
    static final String LPLUS_UPDATED = "lplusUpdated";

    @Autowired
    private LPlusConnection lPlusConnection;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private ChangeRepository changeRepository;
    @Autowired
    private UserService userService;

    /**
     * Exports the question to LPlus if configured and a catalog exists for the question's language.
     * Creates the question in LPlus if new, updates it if it already exists there.
     * Saves a {@code "lplusCreated"} or {@code "lplusUpdated"} change log record on success.
     * Silently skips if LPlus is not configured, or no catalog is configured for this language.
     */
    public void exportToLPlus(Question question, ChangedByAt changedByAt)
    {
        LPlusConnection.Context ctx = lPlusConnection.getContext();
        if (ctx == null)
            return;

        LPlusConnection.CatalogContext catalog = ctx.forLanguage(question.getLanguage());
        if (catalog == null)
        {
            LOGGER.debug("No LPlus catalog configured for language '{}' — skipping question {}",
                question.getLanguage(), question.getId());
            return;
        }

        Optional<Integer> lplusId = catalog.questionIndex().getLPlusId(question.getId());
        if (lplusId.isPresent())
        {
            ctx.client().exportQuestion(catalog.catalogId(), lplusId.get(), question,
                catalog.topicIndex(), catalog.attachmentIndex(),
                ctx.targetGroupB2(), ctx.targetGroupB3(), ctx.langId());
            changeRepository.save(new Change(changedByAt, question, LPLUS_UPDATED,
                String.valueOf(lplusId.get()), String.valueOf(lplusId.get())));
        }
        else
        {
            createInLPlusWithContext(question, changedByAt, ctx, catalog);
        }
    }

    /**
     * Exports the question to LPlus only if the latest change record is not already
     * an LPlus upload event — meaning the question has changed since its last upload,
     * or has never been uploaded.
     * Silently skips if LPlus is not configured or no catalog is configured for this language.
     */
    public void exportIfNeeded(Question question)
    {
        LPlusConnection.Context ctx = lPlusConnection.getContext();
        if (ctx == null)
            return;

        if (ctx.forLanguage(question.getLanguage()) == null)
            return;

        Change latest = changeRepository.findTopByChangeKeyQuestionIdOrderByChangeKeyChangedAtDesc(question.getId());
        if (latest != null && isLPlusEvent(latest))
            return;

        exportToLPlus(question, new ChangedByAt(userService.getCurrentUser()));
    }

    /**
     * Exports all questions that need it (not yet up-to-date in LPlus), in ascending ID order.
     * Questions whose language has no configured catalog are counted as skipped.
     * Errors per question are caught and collected; processing continues on failure.
     *
     * @return An {@link ExportResult} with counts and any per-question error messages.
     */
    public ExportResult exportMissing()
    {
        if (!lPlusConnection.isConfigured())
            return new ExportResult(0, 0, List.of());

        LPlusConnection.Context ctx = lPlusConnection.getContext();

        List<Question> questions = questionRepository.findAll(Sort.by("id"));
        int exported = 0;
        int skipped  = 0;
        List<String> errors = new ArrayList<>();

        for (Question question : questions)
        {
            if (ctx.forLanguage(question.getLanguage()) == null)
            {
                LOGGER.debug("No catalog for language '{}' — skipping question {}", question.getLanguage(), question.getId());
                skipped++;
                continue;
            }
            Change latest = changeRepository.findTopByChangeKeyQuestionIdOrderByChangeKeyChangedAtDesc(question.getId());
            if (latest != null && isLPlusEvent(latest))
            {
                LOGGER.debug("Question {} already up-to-date in LPlus — skipping", question.getId());
                skipped++;
                continue;
            }
            try
            {
                exportToLPlus(question, new ChangedByAt(userService.getCurrentUser()));
                exported++;
                LOGGER.info("Bulk question export progress: {}/{} exported so far", exported, questions.size() - skipped);
            }
            catch (Exception e)
            {
                LOGGER.warn("Failed to export question {} to LPlus: {}", question.getId(), e.getMessage());
                errors.add("Question " + question.getId() + ": " + e.getMessage());
            }
        }

        LOGGER.info("exportMissing complete: {} exported, {} skipped, {} errors", exported, skipped, errors.size());
        return new ExportResult(exported, skipped, errors);
    }

    /**
     * Returns an HTML section for the admin page.
     * Shows the export button only when LPlus is configured.
     */
    public String getAdminSectionHtml()
    {
        if (!lPlusConnection.isConfigured())
            return """
                <h2>Export questions to LPlus</h2>
                LPlus is not configured (exam.lplus.url is not set).
                """;
        return """
            <h2>Export questions to LPlus</h2>
            Exports all questions whose last change record is not an LPlus upload event.<br/>
            Questions with no configured catalog for their language are skipped.<br/>
            <form method="POST" action="/api/exportQuestionsToLPlus">
            <input type="submit" name="submit" value="Export missing questions to LPlus"/>
            </form>
            """;
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private void createInLPlusWithContext(Question question, ChangedByAt changedByAt,
        LPlusConnection.Context ctx, LPlusConnection.CatalogContext catalog)
    {
        int lplusId = ctx.client().createQuestion(catalog.catalogId(), question,
            catalog.topicIndex(), catalog.attachmentIndex(),
            ctx.targetGroupB2(), ctx.targetGroupB3(), ctx.langId());
        catalog.questionIndex().put(question.getId(), lplusId);
        changeRepository.save(new Change(changedByAt, question, LPLUS_CREATED, "", String.valueOf(lplusId)));
    }

    private static boolean isLPlusEvent(Change change)
    {
        String field = change.getChangeKey().getField();
        return LPLUS_CREATED.equals(field) || LPLUS_UPDATED.equals(field);
    }

    /**
     * Result of a bulk LPlus question export.
     *
     * @param exported The number of questions successfully exported.
     * @param skipped  The number of questions skipped (already up-to-date or no catalog for language).
     * @param errors   A list of human-readable error messages, one per failed question.
     */
    public record ExportResult(int exported, int skipped, List<String> errors) {}
}
