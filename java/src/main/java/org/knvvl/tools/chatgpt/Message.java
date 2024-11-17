package org.knvvl.tools.chatgpt;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.google.gson.annotations.SerializedName;

/**
 * Message class to construct system and user messages
 */
public record Message(@Nonnull Role role, @Nonnull String content)
{
    public enum Role
    {
        /**
         * See https://platform.openai.com/docs/api-reference/chat/create
         */
        @SerializedName("user") USER("user"), // Specify the message from the user
        @SerializedName("system") SYSTEM("system"), // Specify the message from the system
        @SerializedName("assistant") ASSISTANT("assistant");  // Specify the message from the chatgpt assistant

        final String id;

        Role(String id)
        {
            this.id = id;
        }

        String id()
        {
            return id;
        }
    }

    public Message(@Nonnull Role role, @Nonnull String content)
    {
        this.role = Objects.requireNonNull(role);
        this.content = Objects.requireNonNull(content);
    }

    /**
     * Creates a user message
     * 
     * @param content The content of the message
     * @return The message
     */
    static Message systemMessage(String content)
    {
        return new Message(Role.SYSTEM, content);
    }

    /**
     * Creates a system message
     * 
     * @param content The content of the message
     * @return The message
     */
    static Message userMessage(String content)
    {
        return new Message(Role.USER, content);
    }

    /**
     * Creates a assistant message
     * 
     * @param content The content of the message
     * @return The message
     */
    static Message assistantMessage(String content)
    {
        return new Message(Role.ASSISTANT, content);
    }
}
