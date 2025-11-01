package com.kapamejlbka.objectmanager.service;

import java.io.IOException;
import java.io.InputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class PdfFontProvider {

    private static final String FONT_RESOURCE_PATH = "fonts/NotoSans-Regular.ttf";
    private final Resource fontResource = new ClassPathResource(FONT_RESOURCE_PATH);

    public PDFont loadFont(PDDocument document) {
        if (fontResource.exists()) {
            try (InputStream inputStream = fontResource.getInputStream()) {
                return PDType0Font.load(document, inputStream, true);
            } catch (IOException ex) {
                // fall through to default font
            }
        }
        return PDType1Font.HELVETICA;
    }
}
