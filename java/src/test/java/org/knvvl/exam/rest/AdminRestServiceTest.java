package org.knvvl.exam.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.knvvl.exam.rest.AdminRestService.FORMATTER;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class AdminRestServiceTest
{
    @Test
    void dateFormat()
    {
        LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(1696529051, 123456, ZoneOffset.UTC);
        assertEquals("2023-10-05_180411", localDateTime.format(FORMATTER));
    }
}