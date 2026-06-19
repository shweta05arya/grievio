package com.grievio.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Utility class for generating PDF exports using iText 5.
 * Used across all dashboards (user, society, sector, gov admin).
 */
public class PdfExporter {

    private static final BaseColor DARK_BG   = new BaseColor(13, 27, 42);
    private static final BaseColor HEADER_BG = new BaseColor(21, 101, 192);
    private static final BaseColor ALT_BG    = new BaseColor(18, 41, 74);
    private static final BaseColor TEXT_DARK = new BaseColor(30, 30, 50);
    private static final Font TITLE_FONT  = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.WHITE);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
    private static final Font BODY_FONT   = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(40, 40, 60));
    private static final Font LABEL_FONT  = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, new BaseColor(21, 101, 192));
    private static final Font SUB_FONT    = new Font(Font.FontFamily.HELVETICA, 9,  Font.NORMAL, new BaseColor(90, 90, 120));

    /** Shows a save dialog and returns chosen file, or null if cancelled. */
    public static File chooseSaveFile(String defaultName) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save PDF");
        fc.setInitialFileName(defaultName);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File home = new File(System.getProperty("user.home"));
        fc.setInitialDirectory(home);
        return fc.showSaveDialog(null);
    }

    /** Exports a single complaint detail to PDF. */
    public static void exportComplaintDetail(Map<String, String> fields, List<String> comments, File outFile) throws Exception {
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, new FileOutputStream(outFile));
        doc.open();

        addLetterHead(doc, "COMPLAINT DETAIL REPORT");
        doc.add(Chunk.NEWLINE);

        // Details table
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{35f, 65f});
        table.setSpacingBefore(8);

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            addTableCell(table, entry.getKey(), true);
            addTableCell(table, entry.getValue() != null ? entry.getValue() : "—", false);
        }
        doc.add(table);

        if (comments != null && !comments.isEmpty()) {
            doc.add(Chunk.NEWLINE);
            Paragraph cmtTitle = new Paragraph("COMMENTS & UPDATES", LABEL_FONT);
            cmtTitle.setSpacingBefore(10);
            doc.add(cmtTitle);
            PdfPTable cmtTable = new PdfPTable(1);
            cmtTable.setWidthPercentage(100);
            for (String cmt : comments) {
                PdfPCell cell = new PdfPCell(new Phrase(cmt, BODY_FONT));
                cell.setPadding(6);
                cell.setBackgroundColor(new BaseColor(240, 245, 255));
                cmtTable.addCell(cell);
            }
            doc.add(cmtTable);
        }

        addFooter(doc);
        doc.close();
    }

    /** Exports a list of complaints (rows of field maps) to PDF. */
    public static void exportComplaintList(String reportTitle, String[] headers,
                                           List<String[]> rows, File outFile) throws Exception {
        Document doc = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(doc, new FileOutputStream(outFile));
        doc.open();

        addLetterHead(doc, reportTitle);
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        table.setSpacingBefore(6);

        // Header row
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
            cell.setBackgroundColor(HEADER_BG);
            cell.setPadding(7);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        // Data rows
        boolean alt = false;
        for (String[] row : rows) {
            BaseColor bg = alt ? new BaseColor(235, 240, 255) : BaseColor.WHITE;
            for (String cell : row) {
                PdfPCell c = new PdfPCell(new Phrase(cell != null ? cell : "—", BODY_FONT));
                c.setBackgroundColor(bg);
                c.setPadding(5);
                table.addCell(c);
            }
            alt = !alt;
        }

        if (rows.isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("No records found.", BODY_FONT));
            empty.setColspan(headers.length);
            empty.setHorizontalAlignment(Element.ALIGN_CENTER);
            empty.setPadding(10);
            table.addCell(empty);
        }

        doc.add(table);
        addFooter(doc);
        doc.close();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void addLetterHead(Document doc, String reportTitle) throws DocumentException {
        // Title bar
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        PdfPCell titleCell = new PdfPCell(new Phrase("Grievio — AI Smart Complaint System", TITLE_FONT));
        titleCell.setBackgroundColor(HEADER_BG);
        titleCell.setPadding(12);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setBorder(Rectangle.NO_BORDER);
        header.addCell(titleCell);
        doc.add(header);
        doc.add(Chunk.NEWLINE);

        Paragraph rTitle = new Paragraph(reportTitle, LABEL_FONT);
        rTitle.setAlignment(Element.ALIGN_LEFT);
        doc.add(rTitle);

        Paragraph meta = new Paragraph("Generated: " +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy  HH:mm")), SUB_FONT);
        doc.add(meta);
    }

    private static void addTableCell(PdfPTable table, String value, boolean isLabel) {
        Font f = isLabel ? LABEL_FONT : BODY_FONT;
        PdfPCell cell = new PdfPCell(new Phrase(value, f));
        cell.setPadding(6);
        cell.setBackgroundColor(isLabel ? new BaseColor(230, 240, 255) : BaseColor.WHITE);
        table.addCell(cell);
    }

    private static void addFooter(Document doc) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph(
            "This is a system-generated document from Grievio. For queries contact your society/sector admin.", SUB_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);
    }
}
