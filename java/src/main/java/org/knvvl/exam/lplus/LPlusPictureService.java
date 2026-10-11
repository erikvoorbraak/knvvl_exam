package org.knvvl.exam.lplus;

import java.util.ArrayList;
import java.util.List;

import org.knvvl.exam.entities.Picture;
import org.knvvl.exam.repos.PictureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Spring service responsible for all LPlus-related operations on {@link Picture} entities.
 *
 * <p>Handles uploading new pictures and bulk-uploading missing ones to LPlus TestStudio,
 * and provides the admin page HTML section for the picture export button.
 */
@Service
public class LPlusPictureService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LPlusPictureService.class);

    @Autowired
    private LPlusConnection lPlusConnection;
    @Autowired
    private PictureRepository pictureRepository;
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Uploads the given picture to LPlus TestStudio if LPlus is configured.
     * The LPlus filename is {@code "<pictureId>.jpg"} and the original filename is the description.
     * No-op if LPlus is not configured.
     *
     * @param picture The picture to upload. Must have a non-null ID.
     */
    public void upload(Picture picture)
    {
        LPlusConnection.Context ctx = lPlusConnection.getContext();
        if (ctx == null)
            return;

        uploadToAllCatalogs(ctx, picture);
    }

    /**
     * Uploads all local pictures not yet present in LPlus, in ascending ID order.
     * Uses the lightweight {@code PictureView} projection to avoid flooding the L1 cache,
     * loading each picture's bytes individually and evicting it immediately after upload.
     * Errors per picture are caught and collected; processing continues on failure.
     *
     * @return An {@link UploadResult} with the count of successful uploads and any per-picture errors.
     */
    public UploadResult uploadMissing()
    {
        LPlusConnection.Context ctx = lPlusConnection.getContext();
        if (ctx == null)
            return new UploadResult(0, List.of());

        List<Picture.PictureView> views = pictureRepository.getPictureByOrderById();
        int uploaded = 0;
        List<String> errors = new ArrayList<>();

        for (Picture.PictureView view : views)
        {
            // Skip if already present in ALL configured catalogs
            String key = String.valueOf(view.getId());
            boolean presentEverywhere = ctx.catalogs().values().stream()
                .allMatch(catalog -> catalog.attachmentIndex().hasName(key));
            if (presentEverywhere)
            {
                LOGGER.debug("Picture {} ('{}') already in all LPlus catalogs — skipping", view.getId(), view.getFilename());
                continue;
            }
            Picture picture = pictureRepository.getReferenceById(view.getId());
            try
            {
                uploadToAllCatalogs(ctx, picture);
                uploaded++;
                LOGGER.info("Bulk upload progress: {}/{} pictures uploaded so far", uploaded, views.size());
            }
            catch (Exception e)
            {
                LOGGER.warn("Failed to upload picture {} ('{}') to LPlus: {}", view.getId(), view.getFilename(), e.getMessage());
                errors.add("Picture " + view.getId() + " ('" + view.getFilename() + "'): " + e.getMessage());
            }
            finally
            {
                entityManager.detach(picture);
            }
        }

        LOGGER.info("uploadMissing complete: {} uploaded, {} already present, {} errors",
            uploaded, views.size() - uploaded - errors.size(), errors.size());
        return new UploadResult(uploaded, errors);
    }

    /**
     * Throws {@link UnsupportedOperationException} if LPlus is configured.
     * No-op if LPlus is not configured.
     */
    public void validateNoUpdateAllowed()
    {
        if (lPlusConnection.isConfigured())
            throw new UnsupportedOperationException(
                "Updating an existing picture is not supported when LPlus is configured. "
                + "Delete the picture, re-upload it, and update any questions that reference it.");
    }

    /**
     * Returns an HTML section for the admin page.
     * Shows the export button only when LPlus is configured.
     */
    public String getAdminSectionHtml()
    {
        if (!lPlusConnection.isConfigured())
            return """
                <h2>Export pictures to LPlus</h2>
                LPlus is not configured (exam.lplus.url is not set).
                """;
        return """
            <h2>Export pictures to LPlus</h2>
            Uploads all local pictures that are not yet present in the LPlus catalog as an attachment.<br/>
            Each picture is stored under the filename &lt;pictureId&gt;.jpg with the original filename as description.<br/>
            <form method="POST" action="/api/exportPicturesToLPlus">
            <input type="submit" name="submit" value="Export missing pictures to LPlus"/>
            </form>
            """;
    }

    /**
     * Result of a bulk LPlus picture upload.
     *
     * @param uploaded The number of pictures successfully uploaded.
     * @param errors   A list of human-readable error messages, one per failed picture.
     */
    public record UploadResult(int uploaded, List<String> errors) {}

    // ------------------------------------------------------------------
    // Package-private upload methods (delegated to from LPlusClient)
    // ------------------------------------------------------------------

    /**
     * Uploads a {@link Picture} to LPlus, deriving the LPlus filename as
     * {@code "<pictureId>.<ext>"} and using the original filename as description.
     */
    static LPlusAttachmentResponse uploadAttachment(LPlusClient client, int catalogId, Picture picture)
    {
        String originalName  = picture.getFilename();
        String extension     = extensionOf(originalName);
        if (extension.isEmpty())
            extension = extensionFromBytes(picture.getFileData());
        String lplusFileName = picture.getId() + extension;   // e.g. "42.jpg"
        return uploadAttachment(client, catalogId, lplusFileName, originalName, picture.getFileData());
    }

    /**
     * Builds and POSTs the attachment JSON payload to LPlus via the given {@link LPlusClient}.
     */
    static LPlusAttachmentResponse uploadAttachment(LPlusClient client, int catalogId,
        String fileName, String description, byte[] data)
    {
        String path = "/ltseditor/api/api/catalogs/" + catalogId + "/attachments";
        LOGGER.info("Uploading attachment '{}' (description='{}', {} bytes) to catalogId={}",
            fileName, description, data.length, catalogId);

        String mimeType = mimeType(fileName, data);
        String base64   = java.util.Base64.getEncoder().encodeToString(data);
        String content  = "data:" + mimeType + ";base64," + base64;

        com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
        payload.addProperty("description",  description);
        payload.addProperty("fileName",     fileName);
        payload.add("width",    com.google.gson.JsonNull.INSTANCE);
        payload.add("height",   com.google.gson.JsonNull.INSTANCE);
        payload.add("duration", com.google.gson.JsonNull.INSTANCE);
        payload.addProperty("printable", false);
        payload.addProperty("content",   content);
        payload.addProperty("itemType",  "Image");

        String responseJson = client.sendForString(
            org.knvvl.tools.generic.http.Request.buildPost()
                .withPath(path)
                .withJsonBody(new com.google.gson.Gson().toJson(payload))
                .build());

        LPlusAttachmentResponse response = new com.google.gson.Gson()
            .fromJson(responseJson, LPlusAttachmentResponse.class);
        LOGGER.info("Uploaded attachment '{}': catalogId={}, attachmentId={}, mediaType={}, fileSize={} bytes",
            fileName, catalogId, response.getId(), response.getMediaType(), response.getFileSize());
        return response;
    }

    // ------------------------------------------------------------------
    // Package-private file-type utilities (used by LPlusClient)
    // ------------------------------------------------------------------

    /**
     * Returns the file extension of a file name, including the leading dot,
     * or an empty string if there is no extension.
     *
     * <p>Examples: {@code "Aer001.jpg"} → {@code ".jpg"},  {@code "noext"} → {@code ""}
     */
    static String extensionOf(String fileName)
    {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot) : "";
    }

    /**
     * Derives a file extension (including leading dot) from the magic bytes of the data.
     * Returns {@code ".bin"} when the format cannot be determined.
     */
    static String extensionFromBytes(byte[] data)
    {
        return switch (mimeTypeFromBytes(data))
        {
            case "image/jpeg" -> ".jpg";
            case "image/png"  -> ".png";
            case "image/gif"  -> ".gif";
            case "image/bmp"  -> ".bmp";
            case "image/webp" -> ".webp";
            default           -> ".bin";
        };
    }

    /**
     * Returns the MIME type for the given file, first by inspecting the filename extension,
     * then by sniffing the leading magic bytes of the data if the extension is absent or
     * unrecognised.
     * Defaults to {@code "application/octet-stream"} when neither method yields a result.
     */
    static String mimeType(String fileName, byte[] data)
    {
        if (fileName != null)
        {
            String lower = fileName.toLowerCase();
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
            if (lower.endsWith(".png"))  return "image/png";
            if (lower.endsWith(".gif"))  return "image/gif";
            if (lower.endsWith(".bmp"))  return "image/bmp";
            if (lower.endsWith(".webp")) return "image/webp";
            if (lower.endsWith(".svg"))  return "image/svg+xml";
        }
        return mimeTypeFromBytes(data);
    }

    /**
     * Detects the MIME type from the leading magic bytes of the given data.
     * Recognises JPEG, PNG, GIF, BMP and WebP.
     * Returns {@code "application/octet-stream"} for unrecognised content.
     */
    static String mimeTypeFromBytes(byte[] data)
    {
        if (data == null || data.length < 4)
            return "application/octet-stream";

        // JPEG: FF D8 FF
        if ((data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF)
            return "image/jpeg";
        // PNG: 89 50 4E 47
        if ((data[0] & 0xFF) == 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G')
            return "image/png";
        // GIF: 47 49 46 38 (GIF8)
        if (data[0] == 'G' && data[1] == 'I' && data[2] == 'F' && data[3] == '8')
            return "image/gif";
        // BMP: 42 4D (BM)
        if (data[0] == 'B' && data[1] == 'M')
            return "image/bmp";
        // WebP: RIFF....WEBP (bytes 0-3 and 8-11)
        if (data.length >= 12
            && data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
            && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P')
            return "image/webp";

        return "application/octet-stream";
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private void uploadToAllCatalogs(LPlusConnection.Context ctx, Picture picture)
    {
        String key = String.valueOf(picture.getId());
        for (LPlusConnection.CatalogContext catalog : ctx.catalogs().values())
        {
            if (catalog.attachmentIndex().hasName(key))
            {
                LOGGER.debug("Picture {} already in catalog {} — skipping", picture.getId(), catalog.catalogId());
                continue;
            }
            LPlusAttachmentResponse response = uploadAttachment(ctx.client(), catalog.catalogId(), picture);
            catalog.attachmentIndex().put(key, response.getId());
            LOGGER.info("Uploaded picture {} ('{}') to LPlus as attachment {} in catalog {}",
                picture.getId(), picture.getFilename(), response.getId(), catalog.catalogId());
        }
    }
}
