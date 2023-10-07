package org.knvvl.exam.services;

import static org.knvvl.exam.services.TextService.EXAM_LAST_BACKUP;
import static org.knvvl.exam.services.TextService.EXAM_LAST_CHANGED;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.time.Instant;

import org.knvvl.exam.entities.Change.ChangedByAt;
import org.knvvl.exam.entities.Text;
import org.knvvl.exam.repos.TextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeDetector
{
    private final UserService userService;
    private final TextRepository textRepository;
    private ChangedByAt lastChanged;
    private int numChanges = 0;

    @Autowired
    public ChangeDetector(@Lazy UserService userService, @Lazy TextRepository textRepository)
    {
        this.userService = userService;
        this.textRepository = textRepository;
    }

    @Transactional
    public void changed()
    {
        lastChanged = new ChangedByAt(userService.getCurrentUser(), Instant.now());
        numChanges++;
        saveCurrentDateTime(EXAM_LAST_CHANGED);
    }

    public record NeedsBackup(boolean needsBackup, String message) {}

    public NeedsBackup needsBackup()
    {
        var lastChangedString = textRepository.getReferenceById(EXAM_LAST_CHANGED.getKey()).getLabel();
        if (isNullOrEmpty(lastChangedString))
        {
            return new NeedsBackup(false, "No changes found");
        }
        var lastBackupString = textRepository.getReferenceById(EXAM_LAST_BACKUP.getKey()).getLabel();
        var message = "lastChanged = " + lastChangedString + ", lastBackup = " + lastBackupString;
        if (isNullOrEmpty(lastBackupString))
        {
            return new NeedsBackup(true, message);
        }
        boolean requiresBackup = Instant.parse(lastChangedString).isAfter(Instant.parse(lastBackupString));
        return new NeedsBackup(requiresBackup, message);
    }

    @Transactional
    public void createdBackup()
    {
        saveCurrentDateTime(EXAM_LAST_BACKUP);
    }

    private void saveCurrentDateTime(Text text)
    {
        Text fromDB = textRepository.getReferenceById(text.getKey());
        fromDB.setLabel(Instant.now().toString());
        textRepository.save(fromDB);
    }

    public ChangedByAt getLastChanged()
    {
        return lastChanged;
    }

    public int getNumChanges()
    {
        return numChanges;
    }
}
