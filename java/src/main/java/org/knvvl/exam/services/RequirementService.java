package org.knvvl.exam.services;

import java.util.List;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.Nullable;
import org.knvvl.exam.entities.Requirement;
import org.knvvl.exam.meta.EntityField;
import org.knvvl.exam.repos.RequirementRepository;
import org.knvvl.exam.repos.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jakarta.transaction.Transactional;

@Service
public class RequirementService
{
    static final Sort SORT_BY_SUBDOMAIN = Sort.by("subdomain");

    @Autowired
    private RequirementRepository requirementRepository;
    @Autowired
    private TopicRepository topicRepository;

    public List<Requirement> getAll()
    {
        return requirementRepository.findAll(SORT_BY_SUBDOMAIN);
    }

    public Requirement getById(int id)
    {
        return requirementRepository.getReferenceById(id);
    }

    @Transactional
    @Nonnull
    public CreateEntityResult createRequirement(JsonObject form)
    {
        Requirement requirement = new Requirement();
        String error = updateRequirement(form, requirement);
        if (error != null)
            return new CreateEntityResult(null, error);
        requirement.setId(getNewRequirementId());
        requirementRepository.save(requirement);
        return new CreateEntityResult(requirement, null);
    }

    @Transactional
    public String updateRequirement(int requirementId, JsonObject form)
    {
        var requirement = requirementRepository.getReferenceById(requirementId);
        String error = updateRequirement(form, requirement);
        if (error != null)
            return error;
        requirementRepository.save(requirement);
        return null;
    }

    @Nullable
    private String updateRequirement(JsonObject form, Requirement requirement)
    {
        for (EntityField<Requirement> field : Requirement.getFields(topicRepository).getFields())
        {
            JsonElement jsonElement = form.get(field.getValueField());
            if (jsonElement == null) {
                if (field.isMandatory()) {
                    return "Field is mandatory: " + field;
                }
            }
            else {
                field.readJson(requirement, jsonElement);
            }
        }
        return null;
    }

    private int getNewRequirementId()
    {
        Requirement requirement = requirementRepository.findTopByOrderByIdDesc();
        return requirement == null ? 1 : requirement.getId() + 1;
    }

    @Transactional
    public void deleteRequirement(int id)
    {
        requirementRepository.deleteById(id);
    }
}
