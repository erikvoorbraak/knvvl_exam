package org.knvvl.tools.chatgpt;

/**
 * Specifify the various config option for the ChatGPT client.
 * When not specified then often a default will be used.
 * 
 * @see https://platform.openai.com/docs/api-reference/chat/create
 */
public final class ChatGptConfig
{
    public static final float DEFAULT_TEMPERATURE = 1;  // ChatGPT default

    private final String apiKey;
    private final Model model; 
    private final float temperature;

    private ChatGptConfig(ChatGptConfigBuilder builder)
    {
        this.model = builder.model;
        this.temperature = builder.temperature;
        this.apiKey = builder.apiKey;
    }

    /**
     * @return a newly create confic object
     */
    public static ChatGptConfigBuilder create()
    {
        return new ChatGptConfigBuilder(); 
    }

    /**
     * @return The model
     */
    public Model getModel()
    {
        return model;
    }

    /**
     * What sampling temperature to use, between 0 and 2. 
     * @return The temperature
     */
    public float getTemperature()
    {
        return temperature;
    }

    /**
     * @return The api key
     */
    public String getApiKey()
    {
        return apiKey;
    }

    /**
     * Builder for the config
     */
    public static final class ChatGptConfigBuilder
    {
        private Model model = Model.GPT_4O_MINI; 
        private float temperature = DEFAULT_TEMPERATURE;
        private String apiKey;
        
        private ChatGptConfigBuilder()
        {            
        }

        /**
         * @param model The model
         * @return The builder
         */
        public ChatGptConfigBuilder withModel(Model model)
        {
            this.model = model;
            return this;
        }

        /**
         * What sampling temperature to use, between 0 and 2. 
         * Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic.
         * When not specified the default will be 1.
         * 
         * @param temperature value between 0 and 2
         * @return The builder
         */
        public ChatGptConfigBuilder withTemperature(float temperature)
        {
            if (temperature < 0 || temperature > 2)
            {
                throw new IllegalArgumentException("Temperature should be between 0 and 2");
            }

            this.temperature = temperature;
            return this;
        }

        /**
         * @param apiKey The api key
         * @return The builder
         */
        public ChatGptConfigBuilder withApiKey(String apiKey)
        {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * @return The new config
         */
        public ChatGptConfig build()
        {
            return new ChatGptConfig(this);
        }
    }
}
