package org.knvvl.tools.chatgpt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * Helper class to create specific message chains
 */
public final class Messages
{
    private final List<Message> msgs = new ArrayList<>();

    private Messages()
    {        
    }

    /**
     * Create this builder
     * 
     * @return new builder
     */
    public static Messages create()
    {
        return new Messages();
    }

    /**
     * Adds a system message
     * 
     * @param content The system message content
     * @return This builder
     */
    public Messages withSystemMessage(String content)
    {
        msgs.add(Message.systemMessage(content));
        return this;
    }

    /**
     * Adds an user message
     * 
     * @param content The user message
     * @return This builder 
     */
    public Messages withUserMessage(String content)
    {
        msgs.add(Message.userMessage(content));
        return this;
    }

    /**
     * Adds an assistant message
     * 
     * @param content The assistant message
     * @return This builder 
     */
    public Messages withAssistantMessage(String content)
    {
        msgs.add(Message.assistantMessage(content));
        return this;
    }

    /**
     * Add a message to send to ChatGPT that contains the text in an InputStream representing a UTF_8-encoded text.
     * The reason for using Supplier&lt;InputStream&gt; is that we can later optimize memory usage by sending its contents
     * to ChatGPT efficiently.
     * Once the stream has been retrieved from the Supplier, ChatGPT client will close it after reading it.
     * 
     * @param textStream UTF_8 encoding
     * @return This instance
     */
    public Messages withTextStreamSupplier(@Nonnull Supplier<InputStream> textStream)
    {
        try
        {
            var content = new String(textStream.get().readAllBytes(), StandardCharsets.UTF_8);
            return withUserMessage(content);
        }
        catch (IOException e)
        {
            throw new ChatGptException("Error reading stream: " + e.getMessage(), e);
        }
    }

    /**
     * Give instruction to summarize the user messages
     * 
     * @return This instance
     */
    public Messages withSummarize()
    {
        return withSystemMessage("You are a helpful and concise assistant. Your task is to read the user's input and provide a clear," +  
            "coherent, and concise summary. Ensure that the summary captures the main points and relevant details, " +
            "but omit any unnecessary information. Keep the tone neutral and focus on providing an accurate reflection " + 
            "of the original content. When summarizing complex topics, break them down into easily understandable parts without " + 
            "losing essential information.");
    }

    /**
     * Rewrites the message to make it better readable.
     * - Fix spelling error
     * - Fix language construction
     * 
     * @return
     */
    public Messages withRewrite()
    {
        return withSystemMessage("You are a helpful assistant whose task is to rewrite text provided by the user. " + 
            "Your goals are to fix grammatical and spelling errors, improve sentence structure, enhance clarity, " + 
            "and ensure the writing flows smoothly. Preserve the original meaning while making the text more concise, " +
            "readable, and polished. When necessary, rephrase awkward or unclear sentences. Avoid adding new information " + 
            "or changing the intended message.");
    }

    /**
     * @return This instance 
     */
    public Message[] build()
    {
        // Add system messages at the beginning
        List<Message> ordered = new ArrayList<>();
        ordered.addAll(msgs.stream().filter(m -> m.role() == Message.Role.SYSTEM).toList());
        ordered.addAll(msgs.stream().filter(m -> m.role() != Message.Role.SYSTEM).toList());
        return ordered.toArray(new Message[0]);
    }
}
