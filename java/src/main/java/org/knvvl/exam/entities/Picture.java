package org.knvvl.exam.entities;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;

import java.util.List;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.knvvl.exam.meta.LabeledEntity;
import org.knvvl.exam.meta.EntityField.EntityFieldBytes;
import org.knvvl.exam.meta.EntityField.EntityFieldString;
import org.knvvl.exam.meta.EntityFields;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="t_picture")
@BatchSize(size=20)
@Cacheable @Cache(usage = READ_WRITE)
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

    public static EntityFields<Picture> getFields()
    {
        return new EntityFields<>(List.of(
            new EntityFieldString<>("filename", Picture::getFilename, Picture::setFilename),
            new EntityFieldBytes<>("fileData", Picture::getFileData, Picture::setFileData)));
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

    @Override
    public Integer getId()
    {
        return id;
    }

    @Override
    public void setId(int id)
    {
        this.id = id;
    }

    @Override
    public String getLabel()
    {
        return filename;
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

    @Override
    public String toString()
    {
        return filename;
    }
}
