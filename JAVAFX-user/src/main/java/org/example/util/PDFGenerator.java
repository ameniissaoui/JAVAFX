package org.example.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.example.models.Recommandation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

public class PDFGenerator {

    private static final String[] BREAKFAST_VARIATIONS = {
        "Avec des fruits frais",
        "Avec des noix et graines",
        "Version légère",
        "Avec une touche de cannelle",
        "Riche en protéines",
        "Avec du miel",
        "Sans sucre ajouté",
        "Avec des baies fraîches",
        "Avec du yaourt grec",
        "Version énergisante"
    };

    private static final String[] LUNCH_VARIATIONS = {
        "Avec une salade verte",
        "Avec des légumes grillés",
        "Version allégée",
        "Riche en protéines",
        "Avec des graines",
        "Servi avec quinoa",
        "Servi avec riz complet",
        "Version méditerranéenne",
        "Avec des herbes fraîches",
        "Avec sauce légère"
    };

    private static final String[] DINNER_VARIATIONS = {
        "Avec des légumes de saison",
        "Version légère",
        "Avec des herbes fraîches",
        "Saveur méditerranéenne",
        "Riche en fibres",
        "Version à faible teneur en sel",
        "Avec une touche d'épices",
        "Préparé à l'huile d'olive",
        "Servi avec purée de légumes",
        "Accompagné de légumes verts"
    };

    private static final String[] ACTIVITY_VARIATIONS = {
        "Intensité modérée",
        "En plein air",
        "Le matin",
        "En soirée",
        "Avec échauffement prolongé",
        "Avec étirements après",
        "En groupe",
        "Variation d'intensité",
        "Avec périodes de récupération",
        "En musique"
    };

    /**
     * Generate a randomized weekly meal plan based on a recommendation
     * @param recommandation The recommendation to base the meal plan on
     * @param outputPath The file path where the PDF should be saved
     * @return The File object of the generated PDF
     */
    public static File generateWeeklyMealPlan(Recommandation recommandation, String outputPath) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        File outputFile = new File(outputPath);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));

        // Add header and footer
        writer.setPageEvent(new HeaderFooterPageEvent());

        document.open();
        
        // Add title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("Plan Alimentaire Hebdomadaire", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        // Add recommendation details
        Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY);
        Paragraph subtitlePara = new Paragraph("Basé sur votre recommandation médicale", subtitleFont);
        subtitlePara.setAlignment(Element.ALIGN_CENTER);
        subtitlePara.setSpacingAfter(20);
        document.add(subtitlePara);
        
        // Add date range
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Font dateRangeFont = new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC, BaseColor.DARK_GRAY);
        Paragraph dateRange = new Paragraph(
                "Période: " + today.format(formatter) + " - " + endDate.format(formatter),
                dateRangeFont);
        dateRange.setAlignment(Element.ALIGN_CENTER);
        dateRange.setSpacingAfter(30);
        document.add(dateRange);
        
        // Add daily meal plans
        LocalDate currentDate = today;
        Random random = new Random();

        Font dayFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY);
        Font mealTypeFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.DARK_GRAY);
        Font mealFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
        Font notesFont = new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, BaseColor.DARK_GRAY);
        
        for (int i = 0; i < 7; i++) {
            String dayName = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRANCE);
            Paragraph dayPara = new Paragraph(dayName.toUpperCase() + " - " + currentDate.format(formatter), dayFont);
            dayPara.setSpacingBefore(15);
            dayPara.setSpacingAfter(10);
            document.add(dayPara);
            
            // Breakfast
            document.add(new Paragraph("PETIT DÉJEUNER:", mealTypeFont));
            String breakfastVariation = BREAKFAST_VARIATIONS[random.nextInt(BREAKFAST_VARIATIONS.length)];
            document.add(new Paragraph("➢ " + recommandation.getPetit_dejeuner() + " - " + breakfastVariation, mealFont));
            document.add(new Paragraph(" ", notesFont));
            
            // Lunch
            document.add(new Paragraph("DÉJEUNER:", mealTypeFont));
            String lunchVariation = LUNCH_VARIATIONS[random.nextInt(LUNCH_VARIATIONS.length)];
            document.add(new Paragraph("➢ " + recommandation.getDejeuner() + " - " + lunchVariation, mealFont));
            document.add(new Paragraph(" ", notesFont));
            
            // Dinner
            document.add(new Paragraph("DÎNER:", mealTypeFont));
            String dinnerVariation = DINNER_VARIATIONS[random.nextInt(DINNER_VARIATIONS.length)];
            document.add(new Paragraph("➢ " + recommandation.getDiner() + " - " + dinnerVariation, mealFont));
            document.add(new Paragraph(" ", notesFont));
            
            // Activity
            if (recommandation.getActivity() != null && !recommandation.getActivity().trim().isEmpty() 
                    && !recommandation.getActivity().equalsIgnoreCase("Aucune")) {
                document.add(new Paragraph("ACTIVITÉ PHYSIQUE:", mealTypeFont));
                String activityVariation = ACTIVITY_VARIATIONS[random.nextInt(ACTIVITY_VARIATIONS.length)];
                document.add(new Paragraph("➢ " + recommandation.getActivity() + " - " + activityVariation, mealFont));
                
                if (recommandation.getDuree() != null && recommandation.getDuree() > 0) {
                    document.add(new Paragraph("  Durée recommandée: " + recommandation.getDuree() + " minutes", mealFont));
                }
            }
            
            // Add supplements if available
            if (recommandation.getSupplements() != null && !recommandation.getSupplements().trim().isEmpty()) {
                document.add(new Paragraph("SUPPLÉMENTS:", mealTypeFont));
                document.add(new Paragraph("➢ " + recommandation.getSupplements(), mealFont));
            }
            
            // Add a separator
            if (i < 6) {
                PdfContentByte canvas = writer.getDirectContent();
                canvas.setLineWidth(0.5f);
                canvas.setColorStroke(BaseColor.LIGHT_GRAY);
                float yPosition = document.getPageSize().getHeight() - document.topMargin() - 50 - (i * 110);
                canvas.moveTo(document.leftMargin(), yPosition);
                canvas.lineTo(document.getPageSize().getWidth() - document.rightMargin(), yPosition);
                canvas.stroke();
                document.add(new Paragraph("\n", mealFont));
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        // Add nutrition notes
        document.add(new Paragraph("\n", mealFont));
        Font notesTitle = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY);
        document.add(new Paragraph("NOTES NUTRITIONNELLES:", notesTitle));
        
        if (recommandation.getCalories() != null) {
            document.add(new Paragraph("- Apport calorique quotidien recommandé: environ " 
                    + recommandation.getCalories() + " kcal", mealFont));
        }
        
        document.add(new Paragraph("- Hydratation: Boire au moins 1.5L d'eau par jour", mealFont));
        document.add(new Paragraph("- Limiter la consommation de sel et de sucre", mealFont));
        document.add(new Paragraph("- Privilégier les aliments non transformés", mealFont));
        
        document.close();
        
        return outputFile;
    }
    
    /**
     * Custom page event for adding header and footer
     */
    static class HeaderFooterPageEvent extends PdfPageEventHelper {
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.DARK_GRAY);
        Font footerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.DARK_GRAY);
        
        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();
                Phrase header = new Phrase("ChronoSerna - Votre programme personnalisé", headerFont);
                ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, 
                        header, document.leftMargin(), document.top() + 10, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();
                Phrase footer = new Phrase("Page " + writer.getPageNumber() + " - ChronoSerna Health App", footerFont);
                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, 
                        footer, (document.right() - document.left()) / 2 + document.leftMargin(), 
                        document.bottom() - 10, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
} 