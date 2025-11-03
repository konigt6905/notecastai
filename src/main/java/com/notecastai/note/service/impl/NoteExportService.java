package com.notecastai.note.service.impl;

import com.notecastai.note.api.dto.NoteDTO;
import com.notecastai.tag.api.dto.TagDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NoteExportService {

    public byte[] exportAsMarkdown(NoteDTO note, String content) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# ").append(note.getTitle()).append("\n\n");
        markdown.append("**Created:** ").append(note.getCreatedDate()).append("\n");
        markdown.append("**Modified:** ").append(note.getUpdatedDate()).append("\n");
        markdown.append("**Format:** ").append(note.getCurrentFormate()).append("\n\n");

        if (!note.getTags().isEmpty()) {
            markdown.append("**Tags:** ");
            markdown.append(note.getTags().stream()
                    .map(TagDTO::getName)
                    .collect(Collectors.joining(", ")));
            markdown.append("\n\n");
        }

        markdown.append("---\n\n");
        markdown.append(content);

        return markdown.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public byte[] exportAsText(NoteDTO note, String content) {
        StringBuilder text = new StringBuilder();
        text.append(note.getTitle()).append("\n");
        text.append("=".repeat(note.getTitle().length())).append("\n\n");
        text.append("Created: ").append(note.getCreatedDate()).append("\n");
        text.append("Modified: ").append(note.getUpdatedDate()).append("\n");
        text.append("Format: ").append(note.getCurrentFormate()).append("\n\n");

        if (!note.getTags().isEmpty()) {
            text.append("Tags: ");
            text.append(note.getTags().stream()
                    .map(TagDTO::getName)
                    .collect(Collectors.joining(", ")));
            text.append("\n\n");
        }

        text.append("-".repeat(40)).append("\n\n");
        // Strip any markdown from content for plain text
        String plainContent = content
                .replaceAll("#+ ", "")
                .replaceAll("\\*\\*(.+?)\\*\\*", "$1")
                .replaceAll("\\*(.+?)\\*", "$1")
                .replaceAll("\\[(.+?)\\]\\(.+?\\)", "$1");
        text.append(plainContent);

        return text.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public byte[] exportAsHtml(NoteDTO note, String content) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<title>").append(note.getTitle()).append("</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }\n");
        html.append("h1 { color: #333; }\n");
        html.append(".metadata { color: #666; font-size: 0.9em; margin-bottom: 20px; }\n");
        html.append(".tags { display: flex; gap: 10px; flex-wrap: wrap; margin: 10px 0; }\n");
        html.append(".tag { background: #e0e0e0; padding: 5px 10px; border-radius: 5px; }\n");
        html.append("</style>\n");
        html.append("</head>\n<body>\n");
        html.append("<h1>").append(note.getTitle()).append("</h1>\n");
        html.append("<div class=\"metadata\">\n");
        html.append("<p>Created: ").append(note.getCreatedDate()).append("</p>\n");
        html.append("<p>Modified: ").append(note.getUpdatedDate()).append("</p>\n");
        html.append("<p>Format: ").append(note.getCurrentFormate()).append("</p>\n");
        html.append("</div>\n");

        if (!note.getTags().isEmpty()) {
            html.append("<div class=\"tags\">\n");
            for (TagDTO tag : note.getTags()) {
                html.append("<span class=\"tag\">").append(tag.getName()).append("</span>\n");
            }
            html.append("</div>\n");
        }

        html.append("<hr>\n");
        // Convert markdown to HTML (basic conversion)
        String htmlContent = convertMarkdownToHtml(content);
        html.append(htmlContent);
        html.append("\n</body>\n</html>");

        return html.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String convertMarkdownToHtml(String markdown) {
        // Basic markdown to HTML conversion
        String html = markdown
                .replaceAll("### (.+)", "<h3>$1</h3>")
                .replaceAll("## (.+)", "<h2>$1</h2>")
                .replaceAll("# (.+)", "<h1>$1</h1>")
                .replaceAll("\\*\\*(.+?)\\*\\*", "<strong>$1</strong>")
                .replaceAll("\\*(.+?)\\*", "<em>$1</em>")
                .replaceAll("\\[(.+?)\\]\\((.+?)\\)", "<a href=\"$2\">$1</a>")
                .replaceAll("\n\n", "</p><p>")
                .replaceAll("\n", "<br>");
        return "<p>" + html + "</p>";
    }

    public byte[] exportAsPdf(NoteDTO note, String content) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Page dimensions and margins
            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float width = page.getMediaBox().getWidth() - 2 * margin;
            float yPosition = yStart;
            float lineHeight = 14;

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Title
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(note.getTitle());
            contentStream.endText();
            yPosition -= 30;

            // Metadata section
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Created: " + note.getCreatedDate());
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Modified: " + note.getUpdatedDate());
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Type: " + (note.getType() != null ? note.getType().name() : "N/A"));
            contentStream.endText();
            yPosition -= lineHeight;

            if (!note.getTags().isEmpty()) {
                String tags = note.getTags().stream()
                        .map(TagDTO::getName)
                        .collect(Collectors.joining(", "));
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Tags: " + tags);
                contentStream.endText();
                yPosition -= lineHeight;
            }

            // Separator line
            yPosition -= 10;
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
            contentStream.stroke();
            yPosition -= 20;

            // Content - strip markdown and handle line wrapping
            contentStream.setFont(PDType1Font.HELVETICA, 11);
            String plainContent = content
                    .replaceAll("#+ ", "")
                    .replaceAll("\\*\\*(.+?)\\*\\*", "$1")
                    .replaceAll("\\*(.+?)\\*", "$1")
                    .replaceAll("\\[(.+?)\\]\\(.+?\\)", "$1");

            String[] paragraphs = plainContent.split("\n\n");
            PDType1Font font = PDType1Font.HELVETICA;
            float fontSize = 11;

            for (String paragraph : paragraphs) {
                if (paragraph.trim().isEmpty()) continue;

                List<String> lines = wrapText(paragraph, font, fontSize, width);
                for (String line : lines) {
                    // Check if we need a new page
                    if (yPosition < margin + 20) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = yStart;
                        contentStream.setFont(PDType1Font.HELVETICA, 11);
                    }

                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(line);
                    contentStream.endText();
                    yPosition -= lineHeight;
                }
                yPosition -= 6; // Extra space between paragraphs
            }

            contentStream.close();
            document.save(outputStream);
            log.info("PDF export completed for note {}", note.getId());
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Failed to export note {} as PDF", note.getId(), e);
            throw com.notecastai.common.exeption.BusinessException.of(
                    com.notecastai.common.exeption.BusinessException.BusinessCode.INTERNAL_ERROR
                            .append(" Failed to generate PDF: " + e.getMessage())
            );
        }
    }

    private List<String> wrapText(String text, PDType1Font font, float fontSize, float width) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            float textWidth = font.getStringWidth(testLine) / 1000 * fontSize;

            if (textWidth > width) {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // Word is too long, split it
                    lines.add(word);
                }
            } else {
                currentLine = new StringBuilder(testLine);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    public byte[] exportAsDocx(NoteDTO note, String content) {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Title
            XWPFParagraph titleParagraph = document.createParagraph();
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText(note.getTitle());
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            titleRun.setFontFamily("Arial");
            titleParagraph.setSpacingAfter(200);

            // Metadata section
            XWPFParagraph metadataParagraph = document.createParagraph();
            XWPFRun metadataRun = metadataParagraph.createRun();
            metadataRun.setFontSize(10);
            metadataRun.setFontFamily("Arial");
            metadataRun.setColor("666666");

            metadataRun.setText("Created: " + note.getCreatedDate());
            metadataRun.addBreak();
            metadataRun.setText("Modified: " + note.getUpdatedDate());
            metadataRun.addBreak();
            metadataRun.setText("Type: " + (note.getType() != null ? note.getType().name() : "N/A"));

            if (!note.getTags().isEmpty()) {
                metadataRun.addBreak();
                String tags = note.getTags().stream()
                        .map(TagDTO::getName)
                        .collect(Collectors.joining(", "));
                metadataRun.setText("Tags: " + tags);
            }

            metadataParagraph.setSpacingAfter(200);

            // Separator
            XWPFParagraph separatorParagraph = document.createParagraph();
            XWPFRun separatorRun = separatorParagraph.createRun();
            separatorRun.setText("________________________________________");
            separatorRun.setColor("CCCCCC");
            separatorParagraph.setSpacingAfter(200);

            // Content - strip markdown and add paragraphs
            String plainContent = content
                    .replaceAll("#+ ", "")
                    .replaceAll("\\*\\*(.+?)\\*\\*", "$1")
                    .replaceAll("\\*(.+?)\\*", "$1")
                    .replaceAll("\\[(.+?)\\]\\(.+?\\)", "$1");

            String[] paragraphs = plainContent.split("\n\n");

            for (String paragraphText : paragraphs) {
                if (paragraphText.trim().isEmpty()) continue;

                XWPFParagraph contentParagraph = document.createParagraph();
                XWPFRun contentRun = contentParagraph.createRun();
                contentRun.setFontSize(11);
                contentRun.setFontFamily("Arial");

                // Handle line breaks within paragraph
                String[] lines = paragraphText.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    contentRun.setText(lines[i]);
                    if (i < lines.length - 1) {
                        contentRun.addBreak();
                    }
                }

                contentParagraph.setSpacingAfter(150);
            }

            document.write(outputStream);
            log.info("DOCX export completed for note {}", note.getId());
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Failed to export note {} as DOCX", note.getId(), e);
            throw com.notecastai.common.exeption.BusinessException.of(
                    com.notecastai.common.exeption.BusinessException.BusinessCode.INTERNAL_ERROR
                            .append(" Failed to generate DOCX: " + e.getMessage())
            );
        }
    }
}
