package org.knvvl.exam.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="t_picture")
public class Picture implements LabeledEntity
{
    /**
     * To avoid getting the file when retrieving all records
     */
    public interface PictureView
    {
        int getId();
        String getFilename();
        Integer getFileSize();
    }

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "filename")
    private String filename;

    @Column(name = "filesize")
    private Integer fileSize;

    @Column(name = "filedata")
    private byte[] fileData;

    public Integer getId()
    {
        return id;
    }

    @Override
    public String getLabel()
    {
        return filename;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    public byte[] getFileData()
    {
        return fileData;
    }

    public void setFileData(byte[] fileData)
    {
        this.fileData = fileData;
        this.fileSize = fileData.length;
    }

    public int getFileSize()
    {
        return fileSize == null ? 0 : fileSize;
    }
}
