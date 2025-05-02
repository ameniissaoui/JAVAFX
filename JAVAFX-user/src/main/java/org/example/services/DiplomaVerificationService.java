package org.example.services;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.example.models.Medecin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
public class DiplomaVerificationService {

    private final Tesseract tesseract;
    private final List<String> validDiplomaKeywords = Arrays.asList(
            "diplôme", "médecine", "doctorat", "docteur", "médecin",
            "université", "faculté", "médical", "spécialiste"
    );

    public DiplomaVerificationService() {
        tesseract = new Tesseract();
        try {
            tesseract.setDatapath("C:\\Users\\21628\\OneDrive\\Desktop\\reports\\JAVAFX-user\\tessdata");
            tesseract.setLanguage("fra+ara");
        } catch (Exception e) {
            System.err.println("Failed to initialize Tesseract: " + e.getMessage());
            throw new RuntimeException("Tesseract initialization failed", e);
        }
    }

    public boolean verifyDiploma(String diplomaPath) {
        try {
            File diplomaFile = new File(diplomaPath);
            if (!diplomaFile.exists()) {
                System.out.println("File does not exist: " + diplomaPath);
                return false;
            }

            String extractedText;

            // Handle PDF files differently
            if (diplomaPath.toLowerCase().endsWith(".pdf")) {
                extractedText = extractTextFromPdf(diplomaFile);
            } else {
                // Configure Tesseract for better accuracy
                tesseract.setTessVariable("preserve_interword_spaces", "1");
                tesseract.setTessVariable("user_defined_dpi", "300");

                // Handle image files directly with preprocessing
                BufferedImage originalImage = ImageIO.read(diplomaFile);
                BufferedImage processedImage = preprocessImage(originalImage);
                extractedText = tesseract.doOCR(processedImage);
            }

            System.out.println("Extracted text: " + extractedText);

            // Convert to lowercase for case-insensitive matching
            String lowerCaseText = extractedText.toLowerCase();

            // Print each keyword check for debugging
            System.out.println("Keyword checks:");
            boolean foundAnyKeyword = false;
            for (String keyword : validDiplomaKeywords) {
                boolean contains = lowerCaseText.contains(keyword);
                System.out.println("- " + keyword + ": " + contains);
                if (contains) {
                    foundAnyKeyword = true;
                }
            }

            // Check for degree patterns
            boolean hasDegreePattern = hasDegreeOrQualificationPattern(lowerCaseText);
            System.out.println("Has degree pattern: " + hasDegreePattern);

            // Check for date patterns
            boolean hasDatePattern = hasDatePattern(lowerCaseText);
            System.out.println("Has date pattern: " + hasDatePattern);

            // Use more flexible verification criteria - any two out of three checks
            boolean isVerified = (foundAnyKeyword && hasDegreePattern) ||
                    (foundAnyKeyword && hasDatePattern) ||
                    (hasDegreePattern && hasDatePattern);

            System.out.println("Final verification result: " + isVerified);
            return isVerified;

        } catch (Exception e) {
            System.err.println("Error processing document: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private BufferedImage preprocessImage(BufferedImage image) throws IOException {
        // Convert to grayscale for better OCR
        BufferedImage grayImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = grayImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        // Optional: Increase contrast
        RescaleOp rescaleOp = new RescaleOp(1.2f, 15.0f, null);
        rescaleOp.filter(grayImage, grayImage);

        // For debugging, you can save the preprocessed image
        // ImageIO.write(grayImage, "png", new File("preprocessed_image.png"));

        return grayImage;
    }

    private String extractTextFromPdf(File pdfFile) throws IOException, TesseractException {
        StringBuilder extractedText = new StringBuilder();

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            // Process each page of the PDF
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300); // 300 DPI for better OCR

                // Preprocess the image before OCR
                BufferedImage processedImage = preprocessImage(image);

                // Extract text from the rendered image
                String pageText = tesseract.doOCR(processedImage);
                extractedText.append(pageText).append("\n");
            }
        }

        return extractedText.toString();
    }

    private boolean hasDegreeOrQualificationPattern(String text) {
        // More lenient pattern matching
        Pattern pattern = Pattern.compile(
                "(diplôme|master|mastère|doctorat|docteur|certificat|titre|licence|spécialiste|médecin|faculté).{0,50}(médecine|médical|santé|urgence|chirurgie)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        boolean result = matcher.find();

        if (result) {
            System.out.println("Found degree pattern: " + matcher.group());
        }

        return result;
    }

    private boolean hasDatePattern(String text) {
        // Match various date formats
        String[] datePatterns = {
                "\\b\\d{1,2}[/.-]\\d{1,2}[/.-]\\d{2,4}\\b",         // DD/MM/YYYY, DD-MM-YYYY
                "\\b\\d{1,2}\\s+(?:janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)\\s+\\d{4}\\b", // DD Month YYYY
                "(?:janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)\\s+\\d{4}" // Month YYYY
        };

        for (String patternStr : datePatterns) {
            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                System.out.println("Found date pattern: " + matcher.group());
                return true;
            }
        }

        return false;
    }

    // Add this method to help with testing
    public void testVerification(String filepath) {
        System.out.println("Testing verification for: " + filepath);
        boolean result = verifyDiploma(filepath);
        System.out.println("Verification test result: " + result);
    }
}