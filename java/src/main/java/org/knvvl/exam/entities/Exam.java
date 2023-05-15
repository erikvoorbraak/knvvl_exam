package org.knvvl.exam.entities;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;
import static org.knvvl.exam.services.Languages.LANGUAGE_NL;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.knvvl.exam.services.Languages;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="t_exam")
@Cacheable @Cache(usage = READ_WRITE)
public class Exam
{
    /**
     * To avoid getting the file when retrieving all records
     */
    public interface ExamView
    {
        int getId();
        String getLabel();
        Integer getFileSize();
        int getCertificate();
        String getLanguage();
    }

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "label")
    private String label;

    @Column(name = "certificate")
    private int certificate;

    @Column(name = "language")
    private String language = LANGUAGE_NL;

    @Column(name = "filesize")
    private Integer fileSize;

    @Column(name = "file_pdf")
    private byte[] filePdf;

    public Exam()
    {
    }

    public Exam(String label, int certificate, String language)
    {
        this.label = label;
        this.certificate = certificate;
        this.language = Languages.validate(language);
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public int getCertificate()
    {
        return certificate;
    }

    public void setCertificate(int certificate)
    {
        this.certificate = certificate;
    }

    @Nonnull
    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = Languages.validate(language);
    }

    public byte[] getFilePdf()
    {
        return filePdf;
    }

    public int getFileSize()
    {
        return fileSize == null ? 0 : fileSize;
    }

    public void setFilePdf(byte[] filePdf)
    {
        this.filePdf = filePdf;
        this.fileSize = filePdf.length;
    }

    @Override
    public String toString()
    {
        return label;
    }
}
