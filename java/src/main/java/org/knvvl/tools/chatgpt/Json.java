package org.knvvl.tools.chatgpt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

/**
 * Internal helper class that does the json marshalling of the protocol for the completion (chat) api.
 * 
 * <a href="https://platform.openai.com/docs/guides/chat-completions">Doc</a>
 * 
 * Keep this internal.
 */
final class Json
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Json.class);
    private static Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private Json()
    {
    }

    record Error(String message, String type, String code)
    {
    }

    record ErrorResponse(Error error)
    {        
    }
    
    /**
     * Choice in the result is the completion of the chat.
     * By default only one choice will be returned.
     */
    record Choice(Message message)
    {
    }
    
    /**
     * Usage statistics for the completion.
     * Total tokens = prompt tokens + completion tokens
     * @param prompt_tokens number of tokens in the prompt
     * @param completion_tokens number of tokens in the completion
     * @param total_tokens total number of tokens (prompt + completion)
     */
    record UsageStats(int prompt_tokens, int completion_tokens, int total_tokens)
    {
    }
   
    record ChatRequest(Model model, float temperature, Message... messages)
    {   
        public String toJson()
        {
            return GSON.toJson(this);
        }
    }

    record ChatResponse(List<Choice> choices, UsageStats usage)
    {
        public static ChatResponse fromJson(String json)
        {
            return GSON.fromJson(json, ChatResponse.class);
        }  
    }

    public static String createRequest(ChatGptConfig config, Message... messages)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
        try (JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)))
        {
            jsonWriter.beginObject();
            jsonWriter.name("model").value(config.getModel().id()); 

            float temperature = config.getTemperature();
            if (areNotEqual(temperature, ChatGptConfig.DEFAULT_TEMPERATURE))
            {
                jsonWriter.name("temperature").value(temperature);
            }

            jsonWriter.name("messages").beginArray();
            for (Message message : messages)
            {
                jsonWriter.beginObject();
                jsonWriter.name("role").value(message.role().id());
                jsonWriter.name("content").value(message.content());
                jsonWriter.endObject();
            }
            jsonWriter.endArray();            
            jsonWriter.endObject(); // End the JSON array
        }
        catch (IOException e)
        {
            throw new ChatGptException("Failed to create request", e);
        }
        return outputStream.toString(StandardCharsets.UTF_8);
    }

    /**
     * Error response.
     */
    public static Error parseError(String json)
    {
        try
        {
            return GSON.fromJson(json, ErrorResponse.class).error();
        }
        catch (RuntimeException e)
        {
            LOGGER.warn("Failed to parse error response", e);
            return new Error(json, null, null);
        }   
    }

    record ModelInfo(String id)
    {
    }

    /**
     * Models response.
     */
    record ModelsResponse(List<ModelInfo> data)
    {
        public static ModelsResponse fromJson(String json)
        {
            return GSON.fromJson(json, ModelsResponse.class);
        }
    }

    // Utility method for comparing floats with an epsilon
    public static boolean areNotEqual(float a, float b)
    {
        return Math.abs(a - b) > 0.000001f;
    }
}
