package org.knvvl.exam.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="t_exam")
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
    }

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "label")
    private String label;

    @Column(name = "certificate")
    private int certificate;

    @Column(name = "filesize")
    private Integer fileSize;

    @Column(name = "file_pdf")
    private byte[] filePdf;

    public Exam()
    {
    }

    public Exam(String label, int certificate)
    {
        this.label = label;
        this.certificate = certificate;
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
}
