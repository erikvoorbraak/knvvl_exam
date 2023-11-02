package org.knvvl.exam.services;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import net.coobird.thumbnailator.Thumbnails;

public class ResizeTest
{
    @Disabled
    @Test
    void resize() throws IOException
    {
        Thumbnails.of(new File("c:/temp/Foto.jpg")) // Original: 4032 x 2268, 2493 kb
            .size(1600, 1600)
            .toFile(new File("c:/temp/Resized.jpg")); // Resized: 1600 x 900, 310 kb
    }
}
