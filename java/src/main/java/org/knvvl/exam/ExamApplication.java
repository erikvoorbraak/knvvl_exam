package org.knvvl.exam;

import java.time.Instant;

import javax.annotation.Nonnull;

import org.knvvl.exam.entities.Change;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExamApplication
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ExamApplication.class);
	private static final Instant started = Change.ChangedByAt.now();

	public static void main(String[] args) {
		logStartupInfo(args);
		SpringApplication.run(ExamApplication.class, args);
	}

	@Nonnull
	public static Instant getStartedInstant()
	{
		return started.minusNanos(started.getNano());
	}

	private static void logStartupInfo(String[] args)
	{
		LOGGER.info("ExamApplication starting");
		LOGGER.info("Command line params: " + String.join(", ", args));
		logEnv("spring.datasource.url");
		logEnv("spring.datasource.username");
		logEnv("spring.datasource.password");
	}

	private static void logEnv(String key)
	{
		String value = System.getenv(key);
		if (value != null && key.endsWith("password"))
			value = "******";
		if (value != null)
			LOGGER.info(key + " = " + value);
	}
}