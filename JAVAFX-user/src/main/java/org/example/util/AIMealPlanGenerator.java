package org.example.util;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.example.models.Recommandation;
import org.json.JSONArray;
import org.json.JSONObject;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class AIMealPlanGenerator {

    // API key and endpoint for Gemini API
    private static final String API_KEY = "AIzaSyDFHoRC5VyXGDZltsrNzYwBwe0sAJ-Is-I";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    /**
     * Generate an AI-powered weekly meal plan based on a recommendation
     * @param recommandation The recommendation to base the meal plan on
     * @param outputPath The file path where the PDF should be saved
     * @return The File object of the generated PDF
     */
    public static File generateAIWeeklyMealPlan(Recommandation recommandation, String outputPath) throws IOException, DocumentException {
        // Get AI-generated meal plan
        JSONArray weeklyPlan = generateMealPlanFromAI(recommandation);
        
        // Create the PDF with the AI-generated content
        return createPDFFromAIResponse(recommandation, weeklyPlan, outputPath);
    }
    
    /**
     * Call the Gemini API to generate a 7-day meal plan
     * @param recommandation The recommendation to base the meal plan on
     * @return JSONArray containing the 7-day meal plan
     */
    private static JSONArray generateMealPlanFromAI(Recommandation recommandation) throws IOException {
        System.out.println("AIMealPlanGenerator: Starting API call to Gemini...");
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Add API key as query parameter
            String url = API_URL + "?key=" + API_KEY;
            HttpPost request = new HttpPost(url);
            
            // Set headers
            request.setHeader("Content-Type", "application/json");
            
            // Prepare the prompt
            String prompt = buildAIPrompt(recommandation);
            System.out.println("AIMealPlanGenerator: Prompt prepared: " + prompt.substring(0, Math.min(prompt.length(), 200)) + "...");
            
            // Create request body for Gemini API format
            JSONObject requestBody = new JSONObject();
            
            // Create contents array with single item (the prompt)
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            
            // Create parts array with text content
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", prompt);
            parts.put(part);
            
            // Add parts to content
            content.put("parts", parts);
            contents.put(content);
            
            // Add contents to request body
            requestBody.put("contents", contents);
            
            // Set request body
            String jsonRequestBody = requestBody.toString();
            System.out.println("AIMealPlanGenerator: Sending request to API: " + jsonRequestBody);
            request.setEntity(new StringEntity(jsonRequestBody, "UTF-8"));
            
            // Execute request
            System.out.println("AIMealPlanGenerator: Executing request to: " + url);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("AIMealPlanGenerator: Received response with status code: " + statusCode);
                
                HttpEntity entity = response.getEntity();
                
                if (entity != null) {
                    String result = EntityUtils.toString(entity);
                    System.out.println("AIMealPlanGenerator: Response body: " + result);
                    
                    JSONObject jsonResponse = new JSONObject(result);
                    
                    // Check if we have a successful response
                    if (jsonResponse.has("candidates") && !jsonResponse.isNull("candidates")) {
                        JSONArray candidates = jsonResponse.getJSONArray("candidates");
                        if (candidates.length() > 0) {
                            JSONObject candidate = candidates.getJSONObject(0);
                            JSONObject content_response = candidate.getJSONObject("content");
                            JSONArray parts_response = content_response.getJSONArray("parts");
                            if (parts_response.length() > 0) {
                                JSONObject part_response = parts_response.getJSONObject(0);
                                String aiContent = part_response.getString("text");
                                System.out.println("AIMealPlanGenerator: Raw AI content: " + aiContent.substring(0, Math.min(aiContent.length(), 200)) + "...");
                                
                                // Look for JSON content in the AI response
                                int startIndex = aiContent.indexOf("{");
                                int endIndex = aiContent.lastIndexOf("}") + 1;
                                
                                if (startIndex >= 0 && endIndex > startIndex) {
                                    // Extract JSON from response
                                    String jsonContent = aiContent.substring(startIndex, endIndex);
                                    System.out.println("AIMealPlanGenerator: Extracted JSON content: " + jsonContent.substring(0, Math.min(jsonContent.length(), 200)) + "...");
                                    try {
                                        // Parse the AI content as JSON
                                        JSONObject mealPlanJson = new JSONObject(jsonContent);
                                        if (mealPlanJson.has("weeklyPlan")) {
                                            JSONArray weeklyPlan = mealPlanJson.getJSONArray("weeklyPlan");
                                            System.out.println("AIMealPlanGenerator: Successfully parsed JSON with " + weeklyPlan.length() + " days");
                                            return weeklyPlan;
                                        } else {
                                            System.err.println("AIMealPlanGenerator: JSON does not contain weeklyPlan key");
                                            System.err.println("AIMealPlanGenerator: JSON keys: " + String.join(", ", mealPlanJson.keySet()));
                                            throw new IOException("JSON response missing weeklyPlan key");
                                        }
                                    } catch (Exception e) {
                                        System.err.println("AIMealPlanGenerator: Error parsing AI JSON response: " + e.getMessage());
                                        System.err.println("AIMealPlanGenerator: JSON content: " + jsonContent);
                                        throw new IOException("Failed to parse Gemini response as JSON: " + e.getMessage(), e);
                                    }
                                } else {
                                    System.err.println("AIMealPlanGenerator: Could not find JSON content in AI response");
                                    throw new IOException("No JSON content found in Gemini response");
                                }
                            } else {
                                System.err.println("AIMealPlanGenerator: No parts in content response");
                                throw new IOException("No parts in Gemini API response content");
                            }
                        } else {
                            System.err.println("AIMealPlanGenerator: No candidates in response");
                            throw new IOException("No candidates in Gemini API response");
                        }
                    } else if (jsonResponse.has("error")) {
                        // Handle API error
                        JSONObject error = jsonResponse.getJSONObject("error");
                        String errorMessage = error.optString("message", "Unknown API error");
                        System.err.println("AIMealPlanGenerator: API error: " + errorMessage);
                        throw new IOException("Gemini API error: " + errorMessage);
                    } else {
                        System.err.println("AIMealPlanGenerator: Unexpected response format");
                        throw new IOException("Unexpected Gemini API response format");
                    }
                } else {
                    System.err.println("AIMealPlanGenerator: Empty response entity");
                    throw new IOException("Empty response from Gemini API");
                }
            }
        } catch (Exception e) {
            System.err.println("AIMealPlanGenerator: Exception during API call: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error calling Gemini API: " + e.getMessage(), e);
        }
    }
    
    /**
     * Build the prompt for the AI model
     */
    private static String buildAIPrompt(Recommandation recommandation) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("En tant que nutritionniste professionnel, crée un plan alimentaire pour 7 jours basé sur cette recommandation médicale:\n\n");
        prompt.append("- Petit-déjeuner: ").append(recommandation.getPetit_dejeuner()).append("\n");
        prompt.append("- Déjeuner: ").append(recommandation.getDejeuner()).append("\n");
        prompt.append("- Dîner: ").append(recommandation.getDiner()).append("\n");
        
        if (recommandation.getActivity() != null && !recommandation.getActivity().isEmpty()) {
            prompt.append("- Activité physique: ").append(recommandation.getActivity()).append("\n");
            
            if (recommandation.getDuree() != null) {
                prompt.append("- Durée d'activité: ").append(recommandation.getDuree()).append(" minutes\n");
            }
        }
        
        if (recommandation.getCalories() != null) {
            prompt.append("- Apport calorique cible: ").append(recommandation.getCalories()).append(" kcal\n");
        }
        
        if (recommandation.getSupplements() != null && !recommandation.getSupplements().isEmpty()) {
            prompt.append("- Suppléments: ").append(recommandation.getSupplements()).append("\n");
        }
        
        prompt.append("\nCrée des variations pour chaque jour tout en respectant les grandes lignes des recommandations. ");
        prompt.append("Inclus des collations équilibrées si nécessaire. ");
        prompt.append("IMPORTANT: Réponds UNIQUEMENT avec un JSON au format suivant sans AUCUN texte avant ou après le JSON:\n");
        prompt.append("{\n");
        prompt.append("  \"weeklyPlan\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"day\": \"Lundi\",\n");
        prompt.append("      \"breakfast\": \"Description détaillée du petit-déjeuner\",\n");
        prompt.append("      \"lunch\": \"Description détaillée du déjeuner\",\n");
        prompt.append("      \"dinner\": \"Description détaillée du dîner\",\n");
        prompt.append("      \"snacks\": \"Collations suggérées (optionnel)\",\n");
        prompt.append("      \"activity\": \"Activité physique du jour (si applicable)\",\n");
        prompt.append("      \"hydration\": \"Conseil d'hydratation\"\n");
        prompt.append("    },\n");
        prompt.append("    ... (pour chaque jour de la semaine)\n");
        prompt.append("  ]\n");
        prompt.append("}");
        
        return prompt.toString();
    }
    
    /**
     * Create a PDF document from the AI response
     */
    private static File createPDFFromAIResponse(Recommandation recommandation, JSONArray weeklyPlan, String outputPath) 
            throws DocumentException, IOException {
        // Set page size and margins
        Document document = new Document(PageSize.A4, 50, 50, 70, 50); // left, right, top, bottom margins
        File outputFile = new File(outputPath);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));
        
        // Add header and footer
        writer.setPageEvent(new ChronoSerenaHeaderFooter());
        
        document.open();
        
        // Define ChronoSerena brand colors
        BaseColor brandTeal = new BaseColor(78, 205, 196); // #4ECDC4
        BaseColor darkGray = new BaseColor(85, 85, 85);    // #555555
        BaseColor lightGray = new BaseColor(245, 245, 245); // #f5f5f5
        
        // Define fonts
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, brandTeal);
        Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, brandTeal);
        Font headingFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, darkGray);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, darkGray);
        Font italicFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, darkGray);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, darkGray);
        
        // Add title with ChronoSerena styling
        Paragraph title = new Paragraph("Plan Alimentaire Personnalisé", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(5);
        document.add(title);
        
        // Add subtitle
        Paragraph subtitlePara = new Paragraph("Généré par intelligence artificielle", subtitleFont);
        subtitlePara.setAlignment(Element.ALIGN_CENTER);
        subtitlePara.setSpacingAfter(15);
        document.add(subtitlePara);
        
        // Add a separator line
        PdfContentByte cb = writer.getDirectContent();
        cb.setColorStroke(brandTeal);
        cb.setLineWidth(2f);
        cb.moveTo(document.left(), document.top() - 60);
        cb.lineTo(document.right(), document.top() - 60);
        cb.stroke();
        
        // Add date range
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Paragraph dateRange = new Paragraph(
                "Période: " + today.format(formatter) + " - " + endDate.format(formatter),
                italicFont);
        dateRange.setAlignment(Element.ALIGN_CENTER);
        dateRange.setSpacingAfter(20);
        document.add(dateRange);
        
        // Create a 2-column layout for each day
        float[] columnWidths = {1f, 3f};
        
        // Add daily meal plans from AI response
        LocalDate currentDate = today;
        
        // Days per page - adjust based on content
        int daysPerPage = 2;
        
        for (int i = 0; i < weeklyPlan.length(); i++) {
            JSONObject dayPlan = weeklyPlan.getJSONObject(i);
            
            // Start new page for each set of days
            if (i > 0 && i % daysPerPage == 0) {
                document.newPage();
            }
            
            // Draw day header with teal background
            PdfPTable dayHeader = new PdfPTable(1);
            dayHeader.setWidthPercentage(100);
            
            String dayName = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRANCE);
            String headerText = dayName.toUpperCase() + " - " + currentDate.format(formatter);
            
            PdfPCell headerCell = new PdfPCell(new Phrase(headerText, new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.WHITE)));
            headerCell.setBackgroundColor(brandTeal);
            headerCell.setPadding(8);
            headerCell.setBorderWidth(0);
            dayHeader.addCell(headerCell);
            
            // Add spacing based on position in page
            if (i % daysPerPage == 0) {
                dayHeader.setSpacingBefore(5);
            } else {
                dayHeader.setSpacingBefore(20);
            }
            dayHeader.setSpacingAfter(5);
            document.add(dayHeader);
            
            // Create table for daily content
            PdfPTable dayTable = new PdfPTable(columnWidths);
            dayTable.setWidthPercentage(100);
            
            // Breakfast
            PdfPCell labelCell = new PdfPCell(new Phrase("PETIT DÉJEUNER", boldFont));
            labelCell.setPadding(5);
            labelCell.setBorderWidth(0);
            labelCell.setBackgroundColor(lightGray);
            dayTable.addCell(labelCell);
            
            // Limit text length for better display
            String breakfastText = limitTextLength(dayPlan.getString("breakfast"), 300);
            PdfPCell contentCell = new PdfPCell(new Phrase(breakfastText, normalFont));
            contentCell.setPadding(5);
            contentCell.setBorderWidth(0);
            dayTable.addCell(contentCell);
            
            // Lunch
            labelCell = new PdfPCell(new Phrase("DÉJEUNER", boldFont));
            labelCell.setPadding(5);
            labelCell.setBorderWidth(0);
            labelCell.setBackgroundColor(lightGray);
            dayTable.addCell(labelCell);
            
            String lunchText = limitTextLength(dayPlan.getString("lunch"), 300);
            contentCell = new PdfPCell(new Phrase(lunchText, normalFont));
            contentCell.setPadding(5);
            contentCell.setBorderWidth(0);
            dayTable.addCell(contentCell);
            
            // Dinner
            labelCell = new PdfPCell(new Phrase("DÎNER", boldFont));
            labelCell.setPadding(5);
            labelCell.setBorderWidth(0);
            labelCell.setBackgroundColor(lightGray);
            dayTable.addCell(labelCell);
            
            String dinnerText = limitTextLength(dayPlan.getString("dinner"), 300);
            contentCell = new PdfPCell(new Phrase(dinnerText, normalFont));
            contentCell.setPadding(5);
            contentCell.setBorderWidth(0);
            dayTable.addCell(contentCell);
            
            // Snacks if available
            if (dayPlan.has("snacks") && !dayPlan.isNull("snacks")) {
                labelCell = new PdfPCell(new Phrase("COLLATIONS", boldFont));
                labelCell.setPadding(5);
                labelCell.setBorderWidth(0);
                labelCell.setBackgroundColor(lightGray);
                dayTable.addCell(labelCell);
                
                String snackText = limitTextLength(dayPlan.getString("snacks"), 200);
                contentCell = new PdfPCell(new Phrase(snackText, normalFont));
                contentCell.setPadding(5);
                contentCell.setBorderWidth(0);
                dayTable.addCell(contentCell);
            }
            
            // Activity if available
            if (dayPlan.has("activity") && !dayPlan.isNull("activity")) {
                labelCell = new PdfPCell(new Phrase("ACTIVITÉ PHYSIQUE", boldFont));
                labelCell.setPadding(5);
                labelCell.setBorderWidth(0);
                labelCell.setBackgroundColor(lightGray);
                dayTable.addCell(labelCell);
                
                StringBuilder activityText = new StringBuilder(dayPlan.getString("activity"));
                if (dayPlan.has("duration") && !dayPlan.isNull("duration")) {
                    activityText.append("\nDurée recommandée: ").append(dayPlan.getString("duration"));
                }
                
                contentCell = new PdfPCell(new Phrase(limitTextLength(activityText.toString(), 200), normalFont));
                contentCell.setPadding(5);
                contentCell.setBorderWidth(0);
                dayTable.addCell(contentCell);
            }
            
            // Hydration tips if available
            if (dayPlan.has("hydration") && !dayPlan.isNull("hydration")) {
                labelCell = new PdfPCell(new Phrase("HYDRATATION", boldFont));
                labelCell.setPadding(5);
                labelCell.setBorderWidth(0);
                labelCell.setBackgroundColor(lightGray);
                dayTable.addCell(labelCell);
                
                String hydrationText = limitTextLength(dayPlan.getString("hydration"), 200);
                contentCell = new PdfPCell(new Phrase(hydrationText, normalFont));
                contentCell.setPadding(5);
                contentCell.setBorderWidth(0);
                dayTable.addCell(contentCell);
            }
            
            document.add(dayTable);
            
            // Add a separator line between days on the same page
            if ((i % daysPerPage != daysPerPage - 1) && (i < weeklyPlan.length() - 1)) {
                cb.setColorStroke(lightGray);
                cb.setLineWidth(1f);
                
                // Calculate a position for the separator line based on the day index
                float separatorYPosition = document.top() - 250 - ((i % daysPerPage) * 180);
                
                cb.moveTo(document.left(), separatorYPosition);
                cb.lineTo(document.right(), separatorYPosition);
                cb.stroke();
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        // Add a new page for nutrition notes
        document.newPage();
        
        // Add nutrition notes header with teal background
        PdfPTable notesHeaderTable = new PdfPTable(1);
        notesHeaderTable.setWidthPercentage(100);
        
        PdfPCell notesHeaderCell = new PdfPCell(new Phrase("CONSEILS GÉNÉRAUX", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.WHITE)));
        notesHeaderCell.setBackgroundColor(brandTeal);
        notesHeaderCell.setPadding(8);
        notesHeaderCell.setBorderWidth(0);
        notesHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        notesHeaderTable.addCell(notesHeaderCell);
        notesHeaderTable.setSpacingBefore(20);
        notesHeaderTable.setSpacingAfter(20);
        document.add(notesHeaderTable);
        
        // Create a decorative box for nutrition tips
        PdfPTable tipsTable = new PdfPTable(1);
        tipsTable.setWidthPercentage(95);
        tipsTable.setSpacingAfter(20);
        
        PdfPCell tipsCell = new PdfPCell();
        tipsCell.setPadding(15);
        tipsCell.setBorderColor(brandTeal);
        tipsCell.setBorderWidth(1.5f);
        
        Paragraph tipsPara = new Paragraph();
        tipsPara.setIndentationLeft(5);
        tipsPara.setIndentationRight(5);
        
        if (recommandation.getCalories() != null) {
            Chunk caloriesTip = new Chunk("• Apport calorique quotidien recommandé: environ " 
                    + recommandation.getCalories() + " kcal\n\n", normalFont);
            tipsPara.add(caloriesTip);
        }
        
        tipsPara.add(new Chunk("• Hydratation: Boire au moins 1.5L d'eau par jour\n\n", normalFont));
        tipsPara.add(new Chunk("• Limiter la consommation de sel et de sucre\n\n", normalFont));
        tipsPara.add(new Chunk("• Privilégier les aliments non transformés\n\n", normalFont));
        tipsPara.add(new Chunk("• Manger lentement et être attentif à ses sensations de faim et de satiété\n\n", normalFont));
        tipsPara.add(new Chunk("• Varier les sources de protéines (viandes, poissons, oeufs, légumineuses)\n\n", normalFont));
        tipsPara.add(new Chunk("• Favoriser les graisses insaturées (huile d'olive, avocat, noix)", normalFont));
        
        tipsCell.addElement(tipsPara);
        tipsTable.addCell(tipsCell);
        document.add(tipsTable);
        
        // Add a tracking section
        PdfPTable trackingTable = new PdfPTable(1);
        trackingTable.setWidthPercentage(95);
        trackingTable.setSpacingBefore(20);
        
        PdfPCell trackingHeaderCell = new PdfPCell(new Phrase("SUIVI DE VOTRE PROGRESSION", boldFont));
        trackingHeaderCell.setPadding(5);
        trackingHeaderCell.setBorderWidth(0);
        trackingHeaderCell.setBackgroundColor(lightGray);
        trackingHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        trackingTable.addCell(trackingHeaderCell);
        
        PdfPCell trackingContentCell = new PdfPCell();
        trackingContentCell.setPadding(15);
        trackingContentCell.setBorderColor(lightGray);
        trackingContentCell.setBorderWidth(1f);
        
        Paragraph trackingPara = new Paragraph();
        trackingPara.add(new Chunk("Utilisez ce tableau pour suivre votre progression quotidienne:\n\n", normalFont));
        
        // Add checklist
        PdfPTable checklistTable = new PdfPTable(8);
        checklistTable.setWidthPercentage(100);
        
        // Add header row
        PdfPCell emptyHeaderCell = new PdfPCell(new Phrase("Objectifs", boldFont));
        emptyHeaderCell.setPadding(5);
        emptyHeaderCell.setBackgroundColor(new BaseColor(230, 230, 230));
        checklistTable.addCell(emptyHeaderCell);
        
        for (int i = 1; i <= 7; i++) {
            PdfPCell dayCell = new PdfPCell(new Phrase("Jour " + i, boldFont));
            dayCell.setPadding(5);
            dayCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            dayCell.setBackgroundColor(new BaseColor(230, 230, 230));
            checklistTable.addCell(dayCell);
        }
        
        // Add rows for different objectives
        String[] objectives = {
            "Boire 1.5L d'eau", 
            "Suivre le plan alimentaire", 
            "Activité physique", 
            "7-8h de sommeil"
        };
        
        for (String objective : objectives) {
            PdfPCell objectiveCell = new PdfPCell(new Phrase(objective, normalFont));
            objectiveCell.setPadding(5);
            checklistTable.addCell(objectiveCell);
            
            for (int i = 0; i < 7; i++) {
                PdfPCell checkCell = new PdfPCell(new Phrase("☐", new Font(Font.FontFamily.ZAPFDINGBATS, 12)));
                checkCell.setPadding(5);
                checkCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                checklistTable.addCell(checkCell);
            }
        }
        
        trackingPara.add(checklistTable);
        trackingContentCell.addElement(trackingPara);
        trackingTable.addCell(trackingContentCell);
        document.add(trackingTable);
        
        // Add motivational quote in a styled box
        PdfPTable quoteTable = new PdfPTable(1);
        quoteTable.setWidthPercentage(95);
        quoteTable.setSpacingBefore(25);
        quoteTable.setSpacingAfter(20);
        
        PdfPCell quoteCell = new PdfPCell();
        quoteCell.setPadding(15);
        quoteCell.setBorderColor(brandTeal);
        quoteCell.setBorderWidth(0);
        quoteCell.setBackgroundColor(new BaseColor(240, 250, 250));
        
        Paragraph quotePara = new Paragraph();
        quotePara.setAlignment(Element.ALIGN_CENTER);
        
        Chunk quoteText = new Chunk("« La santé n'est pas simplement l'absence de maladie, mais un état de complet bien-être physique, mental et social. »", italicFont);
        quotePara.add(quoteText);
        
        quoteCell.addElement(quotePara);
        quoteTable.addCell(quoteCell);
        document.add(quoteTable);
        
        // Add disclaimer
        Paragraph disclaimer = new Paragraph(
                "Ce plan alimentaire est fourni à titre informatif seulement. " +
                "Pour des conseils nutritionnels personnalisés, veuillez consulter un professionnel de la santé ou un diététicien.",
                smallFont);
        disclaimer.setAlignment(Element.ALIGN_CENTER);
        disclaimer.setSpacingBefore(15);
        document.add(disclaimer);
        
        document.close();
        
        return outputFile;
    }
    
    /**
     * Limits the text length to avoid layout issues
     */
    private static String limitTextLength(String text, int maxLength) {
        if (text == null) return "";
        
        if (text.length() <= maxLength) {
            return text;
        } else {
            return text.substring(0, maxLength - 3) + "...";
        }
    }
    
    /**
     * Custom header and footer class for ChronoSerena PDFs
     */
    static class ChronoSerenaHeaderFooter extends PdfPageEventHelper {
        private PdfTemplate total;
        private BaseFont baseFont;
        private Font headerFont;
        private Font footerFont;
        private BaseColor brandTeal = new BaseColor(78, 205, 196); // #4ECDC4
        
        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            total = writer.getDirectContent().createTemplate(30, 16);
            try {
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
                headerFont = new Font(baseFont, 10, Font.NORMAL, brandTeal);
                footerFont = new Font(baseFont, 8, Font.NORMAL, BaseColor.DARK_GRAY);
            } catch (Exception e) {
                throw new ExceptionConverter(e);
            }
        }
        
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            
            // Header with logo
            cb.saveState();
            String headerText = "ChronoSerena - Plan Alimentaire Personnalisé";
            float textBase = document.top() + 10;
            
            // Add teal header bar
            cb.setColorFill(brandTeal);
            cb.rectangle(document.left(), document.top() + 15, document.right() - document.left(), 25);
            cb.fill();
            
            cb.beginText();
            cb.setFontAndSize(baseFont, 10);
            cb.setColorFill(BaseColor.WHITE);
            cb.setTextMatrix(document.left() + 20, textBase);
            cb.showText(headerText);
            cb.endText();
            
            // Add page number
            cb.beginText();
            cb.setFontAndSize(baseFont, 8);
            cb.setColorFill(BaseColor.DARK_GRAY);
            cb.setTextMatrix(document.left() + 20, document.bottom() - 20);
            cb.showText("Page " + writer.getPageNumber());
            cb.endText();
            
            // Add separation line
            cb.setLineWidth(0.5f);
            cb.setColorStroke(BaseColor.LIGHT_GRAY);
            cb.moveTo(document.left(), document.bottom() - 10);
            cb.lineTo(document.right(), document.bottom() - 10);
            cb.stroke();
            
            // Add website
            cb.beginText();
            cb.setFontAndSize(baseFont, 8);
            cb.setColorFill(brandTeal);
            cb.setTextMatrix(document.right() - 120, document.bottom() - 20);
            cb.showText("www.chronoserena.com");
            cb.endText();
            
            cb.restoreState();
        }
    }
} 