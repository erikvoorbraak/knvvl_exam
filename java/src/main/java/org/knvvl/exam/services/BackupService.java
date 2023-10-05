package org.knvvl.exam.services;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import org.knvvl.exam.meta.EntityHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import jakarta.transaction.Transactional;

@Service
public class BackupService
{
    private final ExamRepositories examRepositories;

    @Autowired
    public BackupService(ExamRepositories examRepositories)
    {
        this.examRepositories = examRepositories;
    }

    public String exportAll(Path file)
    {
        StringBuilder b = new StringBuilder();
        try (JsonWriter writer = new JsonWriter(new FileWriter(file.toFile())))
        {
            writer.setIndent("  ");
            writer.beginObject();
            examRepositories.getEntityHandlers().forEach(h ->
            {
                try
                {
                    int count = h.exportAll(writer);
                    b.append("Exported ").append(h.fieldName()).append(": ").append(count).append("<br/>");
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Cannot write data for " + h.fieldName(), e);
                }
            });
            writer.endObject();
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Cannot write to file " + file + ": " + e.getMessage(), e);
        }
        return b.toString();
    }

    public boolean canRestore()
    {
        return examRepositories.isEmpty();
    }

    @Transactional
    public String importFrom(Path file)
    {
        StringBuilder b = new StringBuilder();
        try (var fileReader = new FileReader(file.toFile());
            var reader = new JsonReader(fileReader) )
        {
            reader.beginObject();
            while (reader.hasNext())
            {
                String repoName = reader.nextName();
                int count = getHandler(repoName).importAll(reader);
                b.append("Imported ").append(repoName).append(": ").append(count).append("<br/>");
            }
            reader.endObject();
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Cannot write to file " + file + ": " + e.getMessage(), e);
        }
        return b.toString();
    }

    private EntityHandler<?> getHandler(String repoName)
    {
        return examRepositories.getEntityHandlers().stream()
            .filter(eh -> repoName.equals(eh.fieldName()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown element type field: " + repoName));
    }
}
