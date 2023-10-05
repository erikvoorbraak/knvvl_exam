package org.knvvl.exam.services;

import java.time.Instant;

import org.knvvl.exam.entities.Change.ChangedByAt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ChangeDetector
{
    private final UserService userService;
    private ChangedByAt lastChanged;
    private int numChanges = 0;

    @Autowired
    public ChangeDetector(@Lazy UserService userService)
    {
        this.userService = userService;
    }

    public void changed()
    {
        lastChanged = new ChangedByAt(userService.getCurrentUser(), Instant.now());
        numChanges++;
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
