package org.knvvl.exam.entities;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;
import static org.knvvl.exam.services.Languages.LANGUAGE_NL;

import java.util.List;

import org.hibernate.annotations.Cache;
import org.knvvl.exam.meta.IdEntity;
import org.knvvl.exam.meta.KnvvlEntity;
import org.knvvl.exam.meta.EntityField;
import org.knvvl.exam.meta.EntityFields;
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
public class Exam implements IdEntity
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

    public static EntityFields<Exam> getFields()
    {
        return new EntityFields<>(List.of(
            new EntityField.EntityFieldString<>("label", Exam::getLabel, Exam::setLabel),
            new EntityField.EntityFieldInteger<>("certificate", Exam::getCertificate, Exam::setCertificate),
            new EntityField.EntityFieldString<>("language", Exam::getLanguage, Exam::setLanguage),
            new EntityField.EntityFieldBytes<>("filePdf", Exam::getFilePdf, Exam::setFilePdf)));
    }

    public Exam()
    {
    }

    public Exam(String label, int certificate, String language)
    {
        this.label = label;
        this.certificate = certificate;
        this.language = Languages.validate(language);
    }

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
