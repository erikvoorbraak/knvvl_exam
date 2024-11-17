package org.knvvl.tools.generic.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigBuilder
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigBuilder.class);
    private static final String PROP_IGNORE_SSL_WARNINGS = "ignoreSslWarnings";
    private static final Path DEV_FILE_PATH = Path.of(System.getProperty("user.home"), ".simplehttpclient.properties");
    private static volatile Properties devProperties = null;

    private boolean supportCookies;
    private boolean followRedirects;
    private boolean ignoreSslWarnings;
    private int maxNumberOfRetries;
    private final Map<String, String> fixedHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    ConfigBuilder()
    {
        Properties p = getOrloadDefaults();
        ignoreSslWarnings = Boolean.valueOf(p.getProperty(PROP_IGNORE_SSL_WARNINGS, Boolean.FALSE.toString()));
        supportCookies = false;
        followRedirects = true;
        maxNumberOfRetries = 10;
    }

    private static Properties getOrloadDefaults()
    {
        if (devProperties == null)
        {
            loadDefaults();
        }
        return devProperties;
    }

    private static synchronized void loadDefaults()
    {
        if (devProperties == null)
        {
            Properties p = new Properties();
            if (!Files.exists(DEV_FILE_PATH))
            {
                writeDefaultDevFile();
            }

            try (Reader reader = Files.newBufferedReader(DEV_FILE_PATH))
            {
                p.load(reader);
            }
            catch (IOException e)
            {
                LOGGER.warn("Failed to read: {}", DEV_FILE_PATH, e);
            }

            LOGGER.info("## For disabling ssl checks modify: {}", DEV_FILE_PATH);

            devProperties = p;
        }
    }

    private static void writeDefaultDevFile()
    {
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(DEV_FILE_PATH)))
        {
            w.println("# DEV settings for SimpleHttpClient");
            w.println(String.format("# Created at: %s", LocalDateTime.now()));
            w.println(String.format("#%s = true", PROP_IGNORE_SSL_WARNINGS));
        }
        catch (IOException e)
        {
            LOGGER.warn("Failed to write: {}", DEV_FILE_PATH);
            LOGGER.debug("Failed to write file", e);
        }
    }

    /**
     * @return The build object
     */
    public SimpleHttpClient.Config build()
    {
        return new ConfigImpl(this);
    }

    /**
     * Should we support/store cookies
     * 
     * @param supportCookies Default: true
     * @return The builder
     */
    public ConfigBuilder withSupportCookies(boolean supportCookies)
    {
        this.supportCookies = supportCookies;
        return this;
    }

    /**
     * Should we follow redirects.
     * 
     * @param followRedirects Default: true
     * @return The builder
     */
    public ConfigBuilder withFollowRedirects(boolean followRedirects)
    {
        this.followRedirects = followRedirects;
        return this;
    }

    /**
     * Never use this in production.
     * Can be useful in some cases like man in the middle security software on laptop or invalid certificates.
     * 
     * @param ignoreSslWarnings Ignore the SSL warnings. Default: false
     * @return The builder
     */
    public ConfigBuilder withIgnoreSslWarnings(boolean ignoreSslWarnings)
    {
        this.ignoreSslWarnings = ignoreSslWarnings;
        return this;
    }

    /**
     * When the client returns a 503 (Server not available) it will retry the request for this number of times.
     * @param maxNumberOfRetries Max number of retries. Default: 10
     * @return The builder
     */
    public ConfigBuilder withMaxNumberOfRetries(int maxNumberOfRetries)
    {
        this.maxNumberOfRetries = Objects.checkIndex(maxNumberOfRetries, 10000);
        return this;
    }

    /**
     * Add a header that will always be added to every request
     * 
     * @param key Header key
     * @param value Header value
     * @return
     */
    public ConfigBuilder withFixedHeader(String key, String value)
    {
        this.fixedHeaders.put(key, value);
        return this;
    }

    final class ConfigImpl implements SimpleHttpClient.Config
    {
        private final boolean supportCookies;
        private final boolean followRedirects;
        private final boolean ignoreSslWarnings;
        private final int maxNumberOfRetries;
        private final Map<String, String> fixedHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        ConfigImpl(ConfigBuilder builder)
        {
            supportCookies = builder.supportCookies;
            followRedirects = builder.followRedirects;
            ignoreSslWarnings = builder.ignoreSslWarnings;
            maxNumberOfRetries = builder.maxNumberOfRetries;
            fixedHeaders.putAll(builder.fixedHeaders);
        }

        @Override
        public boolean supportCookies()
        {
            return supportCookies;
        }

        @Override
        public boolean followRedirects()
        {
            return followRedirects;
        }

        @Override
        public boolean ignoreSslWarnings()
        {
            return ignoreSslWarnings;
        }

        @Override
        public int getMaxNumberOfRetries()
        {
            return maxNumberOfRetries;
        }

        @Override
        public Map<String, String> getFixedHeaders()
        {
            return fixedHeaders;
        }
    }
}
