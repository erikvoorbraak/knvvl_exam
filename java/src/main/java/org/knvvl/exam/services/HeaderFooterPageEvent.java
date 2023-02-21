package org.knvvl.exam.services;

import static org.knvvl.exam.services.ExamGenerator.BOX_NAME;

import static com.itextpdf.text.Element.ALIGN_CENTER;

import com.itextpdf.text.Document;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class HeaderFooterPageEvent extends PdfPageEventHelper
{
    String topic = "";
    int pageNumber = 1;

    public void onEndPage(PdfWriter writer,Document document)
    {
        Rectangle rect = writer.getBoxSize(BOX_NAME);
        float mid = (rect.getRight() + rect.getLeft()) / 2;
        String footer = (topic.isEmpty() ? "" : topic + " - ") + pageNumber;
        ColumnText.showTextAligned(writer.getDirectContent(), ALIGN_CENTER, new Phrase(footer), mid, rect.getBottom(), 0);
        pageNumber++;
    }
}