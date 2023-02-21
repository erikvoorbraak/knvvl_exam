package org.knvvl.exam.services;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.knvvl.exam.entities.LabeledEntity;
import org.knvvl.exam.entities.Question;
import org.knvvl.exam.repos.PictureRepository;
import org.knvvl.exam.repos.RequirementRepository;
import org.knvvl.exam.repos.TopicRepository;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jakarta.annotation.Nonnull;

public abstract class QuestionField
{
    protected final String field;

    QuestionField(@Nonnull String field)
    {
        this.field = field;
    }

    public String getField()
    {
        return field;
    }

    public String getValueField()
    {
        return getField();
    }

    public boolean isMandatory()
    {
        return "topic".equals(field)
            || "requirement".equals(field)
            || "question".equals(field)
            || field.startsWith("answer");
    }

    @Override
    public String toString()
    {
        return field;
    }

    public abstract String toStringValue(Question question);

    public abstract void writeJson(Question question, JsonObject jsonObject);
    public abstract void readJson(Question question, JsonElement jsonElement);

    static class QuestionFieldString extends QuestionField
    {
        private final Function<Question, String> getter;
        private final BiConsumer<Question, String> setter;

        QuestionFieldString(String field, Function<Question, String> getter, BiConsumer<Question, String> setter)
        {
            super(field);
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public String toStringValue(Question question)
        {
            String value = getter.apply(question);
            return value == null ? "" : value;
        }

        @Override
        public void writeJson(Question question, JsonObject jsonObject)
        {
            jsonObject.addProperty(field, getter.apply(question));
        }

        @Override
        public void readJson(@Nonnull Question question, @Nonnull JsonElement jsonElement)
        {
            setter.accept(question, jsonElement.getAsString());
        }
    }

    static class QuestionFieldBoolean extends QuestionField
    {
        private final Function<Question, Boolean> getter;
        private final BiConsumer<Question, Boolean> setter;

        QuestionFieldBoolean(String field, Function<Question, Boolean> getter, BiConsumer<Question, Boolean> setter)
        {
            super(field);
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public String toStringValue(Question question)
        {
            return getter.apply(question) ? "true" : "false";
        }

        @Override
        public void writeJson(Question question, JsonObject jsonObject)
        {
            jsonObject.addProperty(field, getter.apply(question));
        }

        @Override
        public void readJson(Question question, JsonElement jsonElement)
        {
            setter.accept(question, jsonElement.getAsBoolean());
        }
    }

    static abstract class QuestionFieldEntity extends QuestionField
    {
        QuestionFieldEntity(String field)
        {
            super(field);
        }

        public abstract LabeledEntity getEntity(Question question);

        @Override
        public String getValueField()
        {
            return getField() + "Id";
        }

        @Override
        public String toStringValue(Question question)
        {
            LabeledEntity entity = getEntity(question);
            return entity == null ? "" : entity.getLabel();
        }

        @Override
        public void writeJson(Question question, JsonObject jsonObject)
        {
            LabeledEntity entity = getEntity(question);
            if (entity != null)
            {
                jsonObject.addProperty(field, entity.getLabel());
                jsonObject.addProperty(getValueField(), entity.getId());
            }
        }
    }

    static class QuestionFieldTopic extends QuestionFieldEntity
    {
        private final TopicRepository topicRepository;

        QuestionFieldTopic(TopicRepository topicRepository)
        {
            super("topic");
            this.topicRepository = topicRepository;
        }

        @Override
        public void readJson(Question question, JsonElement jsonElement)
        {
            question.setTopic(topicRepository.getReferenceById(jsonElement.getAsInt()));
        }

        @Override
        public LabeledEntity getEntity(Question question)
        {
            return question.getTopic();
        }
    }

    static class QuestionFieldRequirement extends QuestionFieldEntity
    {
        private final RequirementRepository requirementRepository;

        QuestionFieldRequirement(RequirementRepository requirementRepository)
        {
            super("requirement");
            this.requirementRepository = requirementRepository;
        }

        @Override
        public void readJson(Question question, JsonElement jsonElement)
        {
            question.setRequirement(requirementRepository.getReferenceById(jsonElement.getAsInt()));
        }

        @Override
        public LabeledEntity getEntity(Question question)
        {
            return question.getRequirement();
        }
    }

    static class QuestionFieldPicture extends QuestionFieldEntity
    {
        private final PictureRepository pictureRepository;

        QuestionFieldPicture(PictureRepository pictureRepository)
        {
            super("picture");
            this.pictureRepository = pictureRepository;
        }

        @Override
        public void readJson(Question question, JsonElement jsonElement)
        {
            question.setPicture(pictureRepository.getReferenceById(jsonElement.getAsInt()));
        }

        @Override
        public LabeledEntity getEntity(Question question)
        {
            return question.getPicture();
        }
    }
}
