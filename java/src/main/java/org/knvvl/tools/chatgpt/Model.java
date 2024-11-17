package org.knvvl.tools.chatgpt;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;

import com.google.gson.annotations.SerializedName;

/**
 * Possible ChatGPT models. Model change often so needs updating and testing here.
 * 
 * @see https://platform.openai.com/docs/models
 */
public enum Model
{
    @SerializedName("gpt-4o") GPT_4O("gpt-4o"),
    @SerializedName("gpt-4o-mini") GPT_4O_MINI("gpt-4o-mini"),
    @SerializedName("gpt-4-turbo") GPT_4_TURBO("gpt-4-turbo"),
    @SerializedName("gpt-4-turbo") GPT_4("gpt-4-turbo"),
    @SerializedName("gpt-3.5-turbo") GPT_3_5_TURBO("gpt-3.5-turbo");
    //@SerializedName("o1-preview") O1_PREVIEW,
    //@SerializedName("o1-mini") O1_MINI

    final String id;
    Model(String id)
    {
        this.id = id;
    }

    public String id()
    {
        return id;
    }

    public static String ids()
    {
        return Arrays.stream(values()).map(Model::id).collect(joining(", "));
    }
}
