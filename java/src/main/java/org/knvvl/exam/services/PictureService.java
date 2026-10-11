package org.knvvl.exam.services;

import org.knvvl.exam.entities.Picture;
import org.knvvl.exam.lplus.LPlusPictureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Thin service that exposes LPlus picture operations to the rest layer.
 * All logic lives in {@link LPlusPictureService}.
 */
@Service
public class PictureService
{
    @Autowired
    private LPlusPictureService lPlusPictureService;

    /** Uploads the picture to LPlus if configured. No-op otherwise. */
    public void uploadToLPlus(Picture picture)
    {
        lPlusPictureService.upload(picture);
    }

    /** Throws if LPlus is configured (updating existing attachments is not supported). */
    public void validateNoLPlusOnUpdate()
    {
        lPlusPictureService.validateNoUpdateAllowed();
    }
}
