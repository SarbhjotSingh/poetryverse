package com.poetryverse.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.poetryverse.entity.JournalEntry;
import com.poetryverse.entity.Poem;
import com.poetryverse.entity.PoemCollection;
import com.poetryverse.entity.User;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportImportService {

    private final PoemService poemService;
    private final CollectionService collectionService;
    private final JournalService journalService;

    private static final Color COLOR_IVORY = new Color(245, 240, 229);       // #F5F0E5
    private static final Color COLOR_DEEP_OLIVE = new Color(53, 54, 38);     // #353626
    private static final Color COLOR_MUTED_OLIVE = new Color(96, 96, 77);    // #60604D
    private static final Color COLOR_ANTIQUE_GOLD = new Color(161, 139, 80); // #A18B50
    private static final Color COLOR_CHARCOAL = new Color(37, 37, 30);       // #25251E

    public ExportImportService(PoemService poemService,
                               CollectionService collectionService,
                               JournalService journalService) {
        this.poemService = poemService;
        this.collectionService = collectionService;
        this.journalService = journalService;
    }

    /**
     * Custom PDF page event that paints the warm ivory manuscript background
     * and an antique gold double-ruled botanical frame on every page.
     */
    private static class ManuscriptPageBorder extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte canvas = writer.getDirectContentUnder();
            Rectangle pageSize = document.getPageSize();

            // Fill Warm Ivory Background
            canvas.saveState();
            canvas.setColorFill(COLOR_IVORY);
            canvas.rectangle(0, 0, pageSize.getWidth(), pageSize.getHeight());
            canvas.fill();

            // Outer Antique Gold Border
            canvas.setColorStroke(COLOR_ANTIQUE_GOLD);
            canvas.setLineWidth(1.4f);
            canvas.rectangle(28, 28, pageSize.getWidth() - 56, pageSize.getHeight() - 56);
            canvas.stroke();

            // Inner Fine Hairline Border
            canvas.setLineWidth(0.5f);
            canvas.rectangle(33, 33, pageSize.getWidth() - 66, pageSize.getHeight() - 66);
            canvas.stroke();
            canvas.restoreState();
        }
    }

    public byte[] exportPoemToPdf(User user, Poem poem) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 72, 72, 76, 76);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new ManuscriptPageBorder());
            document.open();

            addPoemToPdfDocument(document, user, poem);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF manuscript: " + e.getMessage(), e);
        }
    }

    public byte[] exportCollectionToPdf(User user, PoemCollection collection, List<Poem> poems) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 72, 72, 76, 76);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new ManuscriptPageBorder());
            document.open();

            Font brandFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 11, COLOR_ANTIQUE_GOLD);
            Font titleFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 28, Font.BOLD, COLOR_DEEP_OLIVE);
            Font subFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 14, COLOR_MUTED_OLIVE);
            Font bodyFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, COLOR_CHARCOAL);

            Paragraph header = new Paragraph("POETRYVERSE ANTHOLOGY ARCHIVE", brandFont);
            header.setAlignment(Element.ALIGN_CENTER);
            header.setSpacingAfter(25f);
            document.add(header);

            Paragraph colTitle = new Paragraph(collection.getName().toUpperCase(), titleFont);
            colTitle.setAlignment(Element.ALIGN_CENTER);
            colTitle.setSpacingAfter(10f);
            document.add(colTitle);

            if (collection.getSubtitle() != null && !collection.getSubtitle().isBlank()) {
                Paragraph colSub = new Paragraph(collection.getSubtitle(), subFont);
                colSub.setAlignment(Element.ALIGN_CENTER);
                colSub.setSpacingAfter(18f);
                document.add(colSub);
            }

            if (collection.getEpigraph() != null && !collection.getEpigraph().isBlank()) {
                Paragraph ep = new Paragraph("\u201C" + collection.getEpigraph() + "\u201D", subFont);
                ep.setAlignment(Element.ALIGN_CENTER);
                ep.setSpacingAfter(24f);
                document.add(ep);
            }

            if (collection.getDescription() != null && !collection.getDescription().isBlank()) {
                Paragraph desc = new Paragraph(collection.getDescription(), bodyFont);
                desc.setAlignment(Element.ALIGN_CENTER);
                desc.setSpacingAfter(36f);
                document.add(desc);
            }

            Paragraph byline = new Paragraph("Curated by " + user.getFullName() + " (" + user.getPenName() + ")", subFont);
            byline.setAlignment(Element.ALIGN_CENTER);
            document.add(byline);

            for (Poem poem : poems) {
                document.newPage();
                addPoemToPdfDocument(document, user, poem);
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Collection Anthology PDF: " + e.getMessage(), e);
        }
    }

    private void addPoemToPdfDocument(Document document, User user, Poem poem) throws DocumentException {
        Font headerFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 9, Font.NORMAL, COLOR_ANTIQUE_GOLD);
        Font titleFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 24, Font.NORMAL, COLOR_DEEP_OLIVE);
        Font subFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 13, Font.ITALIC, COLOR_MUTED_OLIVE);
        Font authorFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, Font.BOLD, COLOR_ANTIQUE_GOLD);
        Font verseFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 13, Font.NORMAL, COLOR_CHARCOAL);
        Font footerFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 9, Font.ITALIC, COLOR_MUTED_OLIVE);

        int align = Element.ALIGN_LEFT;
        if ("CENTER".equalsIgnoreCase(poem.getTextAlign())) align = Element.ALIGN_CENTER;
        else if ("RIGHT".equalsIgnoreCase(poem.getTextAlign())) align = Element.ALIGN_RIGHT;

        Paragraph rubric = new Paragraph("POETRYVERSE MANUSCRIPT  •  " +
                (poem.getGenre() != null ? poem.getGenre().toUpperCase() : "FREE VERSE"), headerFont);
        rubric.setAlignment(align);
        rubric.setSpacingAfter(14f);
        document.add(rubric);

        Paragraph title = new Paragraph(poem.getTitle(), titleFont);
        title.setAlignment(align);
        title.setSpacingAfter(6f);
        document.add(title);

        if (poem.getSubtitle() != null && !poem.getSubtitle().isBlank()) {
            Paragraph sub = new Paragraph(poem.getSubtitle(), subFont);
            sub.setAlignment(align);
            sub.setSpacingAfter(10f);
            document.add(sub);
        }

        String poetName = (user.getPenName() != null && !user.getPenName().isBlank())
                ? user.getPenName() : user.getFullName();
        Paragraph byline = new Paragraph("— " + poetName.toUpperCase() + " —", authorFont);
        byline.setAlignment(align);
        byline.setSpacingAfter(22f);
        document.add(byline);

        if (poem.getDedication() != null && !poem.getDedication().isBlank()) {
            Paragraph ded = new Paragraph("For " + poem.getDedication(), subFont);
            ded.setAlignment(align);
            ded.setSpacingAfter(18f);
            document.add(ded);
        }

        String cleanVerse = poem.getContent() != null ? poem.getContent().replaceAll("<[^>]*>", "") : "";
        Paragraph verse = new Paragraph(cleanVerse, verseFont);
        verse.setAlignment(align);
        verse.setLeading(23f);
        verse.setSpacingAfter(28f);
        document.add(verse);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        String dateStr = poem.getCreatedAt() != null ? poem.getCreatedAt().format(fmt) : "Archive Edition";
        Paragraph colophon = new Paragraph(
                String.format("Composed on %s  •  %d words, %d lines  •  Mood: %s  •  PoetryVerse",
                        dateStr, poem.getWordCount(), poem.getLineCount(),
                        poem.getMood() != null ? poem.getMood() : "Contemplative"),
                footerFont
        );
        colophon.setAlignment(align);
        document.add(colophon);
    }

    public byte[] exportPoemToDocx(User user, Poem poem) {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            ParagraphAlignment align = ParagraphAlignment.LEFT;
            if ("CENTER".equalsIgnoreCase(poem.getTextAlign())) align = ParagraphAlignment.CENTER;
            else if ("RIGHT".equalsIgnoreCase(poem.getTextAlign())) align = ParagraphAlignment.RIGHT;

            // Header rubric
            XWPFParagraph rubricP = doc.createParagraph();
            rubricP.setAlignment(align);
            XWPFRun rubricRun = rubricP.createRun();
            rubricRun.setText("POETRYVERSE MANUSCRIPT • " + (poem.getGenre() != null ? poem.getGenre().toUpperCase() : "VERSE"));
            rubricRun.setFontFamily("Georgia");
            rubricRun.setFontSize(9);
            rubricRun.setColor("A18B50");

            // Title
            XWPFParagraph titleP = doc.createParagraph();
            titleP.setAlignment(align);
            titleP.setSpacingAfter(120);
            XWPFRun titleRun = titleP.createRun();
            titleRun.setText(poem.getTitle());
            titleRun.setFontFamily("Georgia");
            titleRun.setFontSize(22);
            titleRun.setBold(true);
            titleRun.setColor("353626");

            // Subtitle
            if (poem.getSubtitle() != null && !poem.getSubtitle().isBlank()) {
                XWPFParagraph subP = doc.createParagraph();
                subP.setAlignment(align);
                subP.setSpacingAfter(160);
                XWPFRun subRun = subP.createRun();
                subRun.setText(poem.getSubtitle());
                subRun.setFontFamily("Georgia");
                subRun.setFontSize(13);
                subRun.setItalic(true);
                subRun.setColor("60604D");
            }

            // Author
            String poetName = (user.getPenName() != null && !user.getPenName().isBlank())
                    ? user.getPenName() : user.getFullName();
            XWPFParagraph authP = doc.createParagraph();
            authP.setAlignment(align);
            authP.setSpacingAfter(360);
            XWPFRun authRun = authP.createRun();
            authRun.setText("By " + poetName);
            authRun.setFontFamily("Georgia");
            authRun.setFontSize(11);
            authRun.setColor("A18B50");

            // Verse Lines
            String cleanText = poem.getContent() != null ? poem.getContent().replaceAll("<[^>]*>", "") : "";
            String[] lines = cleanText.split("\\r?\\n");
            XWPFParagraph verseP = doc.createParagraph();
            verseP.setAlignment(align);
            verseP.setSpacingBetween(1.5);
            for (int i = 0; i < lines.length; i++) {
                XWPFRun lineRun = verseP.createRun();
                lineRun.setText(lines[i]);
                lineRun.setFontFamily("Georgia");
                lineRun.setFontSize(12);
                lineRun.setColor("25251E");
                if (i < lines.length - 1) {
                    lineRun.addBreak();
                }
            }

            // Footer Colophon
            XWPFParagraph footP = doc.createParagraph();
            footP.setAlignment(align);
            footP.setSpacingBefore(480);
            XWPFRun footRun = footP.createRun();
            footRun.setText(String.format("Words: %d  |  Lines: %d  |  Genre: %s  |  Exported from PoetryVerse",
                    poem.getWordCount(), poem.getLineCount(), poem.getGenre()));
            footRun.setFontFamily("Georgia");
            footRun.setFontSize(9);
            footRun.setItalic(true);
            footRun.setColor("60604D");

            doc.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export DOCX manuscript: " + e.getMessage(), e);
        }
    }

    public byte[] exportPoemToTxt(User user, Poem poem) {
        StringBuilder sb = new StringBuilder();
        String poetName = (user.getPenName() != null && !user.getPenName().isBlank())
                ? user.getPenName() : user.getFullName();

        sb.append("====================================================================\n");
        sb.append("  ").append(poem.getTitle().toUpperCase()).append("\n");
        if (poem.getSubtitle() != null && !poem.getSubtitle().isBlank()) {
            sb.append("  ").append(poem.getSubtitle()).append("\n");
        }
        sb.append("  By ").append(poetName).append("\n");
        sb.append("====================================================================\n\n");
        if (poem.getDedication() != null && !poem.getDedication().isBlank()) {
            sb.append("  Dedication: ").append(poem.getDedication()).append("\n\n");
        }
        String cleanVerse = poem.getContent() != null ? poem.getContent().replaceAll("<[^>]*>", "") : "";
        sb.append(cleanVerse).append("\n\n");
        sb.append("--------------------------------------------------------------------\n");
        sb.append("Genre: ").append(poem.getGenre()).append(" | Mood: ").append(poem.getMood()).append("\n");
        sb.append("Words: ").append(poem.getWordCount()).append(" | Lines: ").append(poem.getLineCount())
                .append(" | Stanzas: ").append(poem.getStanzaCount()).append("\n");
        if (!poem.getTagsCsv().isEmpty()) {
            sb.append("Tags: ").append(poem.getTagsCsv()).append("\n");
        }
        sb.append("Archived from PoetryVerse — Where Words Find Their Forever.\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportFullBackupArchive(User user) {
        List<Poem> poems = poemService.getUserPoems(user, null, null, null, null, null, false, "newest");
        List<PoemCollection> cols = collectionService.getUserCollections(user);
        List<JournalEntry> journals = journalService.getJournalEntries(user, null, null, null);

        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append("  POETRYVERSE — COMPLETE PERSONAL ARCHIVE & BACKUP\n");
        sb.append("  Poet: ").append(user.getFullName()).append(" (").append(user.getPenName()).append(")\n");
        sb.append("  Username: @").append(user.getUsername()).append(" | Email: ").append(user.getEmail()).append("\n");
        sb.append("  Total Poems: ").append(poems.size()).append(" | Collections: ").append(cols.size())
                .append(" | Journal Entries: ").append(journals.size()).append("\n");
        sb.append("================================================================================\n\n");

        sb.append("SECTION I: COLLECTIONS (").append(cols.size()).append(")\n");
        sb.append("--------------------------------------------------------------------------------\n");
        for (PoemCollection c : cols) {
            sb.append("• ").append(c.getName());
            if (c.getSubtitle() != null) sb.append(" — ").append(c.getSubtitle());
            sb.append(" (").append(c.getPoemCount()).append(" poems)\n");
            if (c.getDescription() != null) sb.append("  ").append(c.getDescription()).append("\n");
            sb.append("\n");
        }

        sb.append("\nSECTION II: POEMS (").append(poems.size()).append(")\n");
        sb.append("--------------------------------------------------------------------------------\n\n");
        for (Poem p : poems) {
            sb.append("### ").append(p.getTitle()).append("\n");
            if (p.getSubtitle() != null && !p.getSubtitle().isBlank()) {
                sb.append("Subtitle: ").append(p.getSubtitle()).append("\n");
            }
            sb.append("Genre: ").append(p.getGenre()).append(" | Mood: ").append(p.getMood())
                    .append(" | Status: ").append(p.getStatus()).append("\n\n");
            sb.append(p.getContent() != null ? p.getContent().replaceAll("<[^>]*>", "") : "").append("\n\n");
            sb.append("................................................................................\n\n");
        }

        sb.append("\nSECTION III: WRITING JOURNAL (").append(journals.size()).append(")\n");
        sb.append("--------------------------------------------------------------------------------\n\n");
        for (JournalEntry j : journals) {
            sb.append("[").append(j.getEntryDate()).append("] ").append(j.getTitle())
                    .append(" (Mood: ").append(j.getMood()).append(")\n");
            sb.append(j.getContent()).append("\n\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public Poem importPoemFromTxtFile(User user, MultipartFile file, String genre, Long collectionId) {
        try {
            String raw = new String(file.getBytes(), StandardCharsets.UTF_8).trim();
            if (raw.isEmpty()) {
                throw new IllegalArgumentException("The uploaded TXT file is empty.");
            }
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "Imported Manuscript.txt";
            String fallbackTitle = filename.replaceAll("\\.[tT][xX][tT]$", "").replace('_', ' ').trim();

            String[] lines = raw.split("\\r?\\n");
            String title = fallbackTitle;
            String body = raw;

            if (lines.length >= 2 && lines[0].trim().length() > 0 && lines[0].trim().length() < 90 && lines[1].trim().isEmpty()) {
                title = lines[0].trim().replaceAll("^#+\\s*", "");
                StringBuilder bodyBuilder = new StringBuilder();
                for (int i = 2; i < lines.length; i++) {
                    if (bodyBuilder.length() > 0) bodyBuilder.append("\n");
                    bodyBuilder.append(lines[i]);
                }
                body = bodyBuilder.toString().trim();
            }

            List<Long> colIds = (collectionId != null) ? List.of(collectionId) : List.of();
            return poemService.saveOrUpdatePoem(
                    user,
                    null,
                    title,
                    "Imported from manuscript file",
                    body,
                    body.replace("\n", "<br/>"),
                    (genre != null && !genre.isBlank()) ? genre : "Free Verse",
                    "Contemplative",
                    "PUBLISHED",
                    "Cormorant Garamond",
                    20,
                    1.85,
                    "LEFT",
                    "IVORY_MANUSCRIPT",
                    "BOTANICAL_GOLD",
                    null,
                    null,
                    null,
                    "Imported via TXT Manuscript Archiver",
                    null,
                    "imported, manuscript",
                    colIds,
                    false
            );
        } catch (Exception e) {
            throw new RuntimeException("Could not import TXT file: " + e.getMessage(), e);
        }
    }
}
