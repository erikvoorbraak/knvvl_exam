package org.knvvl.exam.lplus;

import com.google.gson.annotations.SerializedName;

/**
 * Maps the JSON body returned by the LPlus TestStudio
 * {@code POST /ltseditor/api/api/catalogs/{catalogId}/attachments} endpoint.
 *
 * <p>Example response:
 * <pre>{@code
 * {
 *   "id": 668,
 *   "description": "test",
 *   "fileSize": 14540,
 *   "revisionNumber": 1,
 *   "isLastRevision": true,
 *   "itemType": "Image",
 *   "lastEditedDateUTC": "2026-10-09T16:19:42.4021582Z",
 *   "lastEditedDate":    "2026-10-09T18:19:42.4021582",
 *   "mediaType": "image/jpeg",
 *   "metadata": { "dimensions": "301 x 469", "height": 469, "width": 301, ... },
 *   "name": "4.jpg",
 *   "isDeleted": false,
 *   "printable": false,
 *   "thumbnailMediaType": "image/jpeg",
 *   ...
 * }
 * }</pre>
 */
public class LPlusAttachmentResponse
{
    @SerializedName("id")
    private int id;

    @SerializedName("description")
    private String description;

    @SerializedName("fileSize")
    private long fileSize;

    @SerializedName("revisionNumber")
    private int revisionNumber;

    @SerializedName("isLastRevision")
    private boolean lastRevision;

    @SerializedName("itemType")
    private String itemType;

    @SerializedName("lastEditedDateUTC")
    private String lastEditedDateUTC;

    @SerializedName("lastEditedDate")
    private String lastEditedDate;

    @SerializedName("mediaType")
    private String mediaType;

    @SerializedName("name")
    private String name;

    @SerializedName("isDeleted")
    private boolean deleted;

    @SerializedName("printable")
    private boolean printable;

    @SerializedName("thumbnailMediaType")
    private String thumbnailMediaType;

    /** The LPlus attachment ID, used to link the attachment to a question. */
    public int getId()              { return id; }
    public String getDescription()  { return description; }
    public long getFileSize()       { return fileSize; }
    public int getRevisionNumber()  { return revisionNumber; }
    public boolean isLastRevision() { return lastRevision; }
    public String getItemType()     { return itemType; }
    public String getLastEditedDateUTC() { return lastEditedDateUTC; }
    public String getLastEditedDate()    { return lastEditedDate; }
    public String getMediaType()    { return mediaType; }
    public String getName()         { return name; }
    public boolean isDeleted()      { return deleted; }
    public boolean isPrintable()    { return printable; }
    public String getThumbnailMediaType() { return thumbnailMediaType; }
}
