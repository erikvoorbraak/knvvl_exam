package org.knvvl.tools.chatgpt;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.knvvl.tools.chatgpt.Json.ChatResponse;
import org.knvvl.tools.chatgpt.Json.ModelsResponse;
import org.knvvl.tools.generic.http.Request;
import org.knvvl.tools.generic.http.SimpleHttpClient;

/**
 * Simple ChatGpt client
 */
public class SimpleChatGptClient extends SimpleHttpClient
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleChatGptClient.class);
    private static final String ROOT_URL = "https://api.openai.com/v1/";

    private final ChatGptConfig config;

    /**
     * Create a client with the default model: gpt-4o-mini
     * The key is needed for authentication.
     * 
     * @param apiKey The chatgpt api key
     */
    public SimpleChatGptClient(@Nonnull String apiKey)
    {
        this(ChatGptConfig.create().withApiKey(apiKey).build());
    }

    /**
     * Create a client with the specified model.
     * The key is needed for authentication.
     * 
     * @param apiKey The chatgpt api key
     * @param model The chatgpt model to use
     */
    public SimpleChatGptClient(@Nonnull String apiKey, @Nonnull Model model)
    {
        this(ChatGptConfig.create().withApiKey(apiKey).withModel(model).build());
    }

    /**
     * Create a client with the specified config
     * 
     * @param config Additional config
     */
    public SimpleChatGptClient(@Nonnull ChatGptConfig config)
    {
        super(ROOT_URL);

        setDefaultHeaders(Map.of(Request.HEADER_AUTHORIZATION, Request.BEARER_PREFIX + config.getApiKey()));

        this.config = config;
    }

    /**
     * <p>
     * Sent the specified messages to chatgpt via the completions api 
     *     (<a href="https://platform.openai.com/docs/guides/chat-completions">Completions API</a>)
     * </p>
     * @param messages The messages that start the conversation
     * @return The message from the assistant as plain text
     */
    public String sendChatMessages(Message... messages)
    {
        return Markdown.toPlain(sendChatMessagesForMarkDown(messages));
    }


    /**
     * <p>
     * Sent the specified messages to chatgpt via the completions api 
     *     (<a href="https://platform.openai.com/docs/guides/chat-completions">Completions API</a>)
     * </p>
     * @param messages The messages that start the conversation
     * @return The message from the assistant as html text (Without html and body tags)
     */
    public String sendChatMessagesForHtml(Message... messages)
    {
        return Markdown.toHtml(sendChatMessagesForMarkDown(messages));
    }

    /**
     * <p>
     * Sent the specified messages to chatgpt via the completions api 
     *     (<a href="https://platform.openai.com/docs/guides/chat-completions">Completions API</a>)
     * </p>
     * @param messages The messages that start the conversation
     * @return The message from the assistant as markdown (default of OpenAi API)
     */
    private String sendChatMessagesForMarkDown(Message... messages)
    {
        try
        {
            String request = Json.createRequest(config, messages);
            LOGGER.debug("ChatGPT Request: {}", request);
            String response = postForString("chat/completions", request, Request.CONTENT_TYPE_JSON);
            LOGGER.debug("ChatGPT Response {}", response);
            // By default the completions api always returns 1 choice (completion). So for now only return the first message
            return ChatResponse.fromJson(response).choices().get(0).message().content();
        }
        catch (RuntimeException e)
        {
            throw ChatGptException.from(e);
        }
    }

    /**
     * @return The available models
     */
    public List<String> getModelIds()
    {
        try
        {
            String json = super.getForString("models");
            ModelsResponse response = Json.ModelsResponse.fromJson(json);
            return response.data().stream().map(m -> m.id()).toList();
        }
        catch (RuntimeException e)
        {
            throw ChatGptException.from(e);
        }
    }
}
