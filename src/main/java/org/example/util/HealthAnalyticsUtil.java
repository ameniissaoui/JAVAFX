package org.example.util;

import org.example.models.Demande;
import org.example.models.Recommandation;
import org.example.services.DemandeDAO;
import org.example.services.RecommandationDAO;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Utility class for generating health analytics from patient demands and recommendations
 */
public class HealthAnalyticsUtil {
    
    /**
     * Generate water consumption trend from patient demands
     * @param patientId The patient ID
     * @param limit Maximum number of demands to include (most recent)
     * @return Map of date to water consumption in liters
     */
    public static Map<LocalDateTime, Float> getWaterConsumptionTrend(int patientId, int limit) {
        DemandeDAO demandeDAO = new DemandeDAO();
        List<Demande> demandes = demandeDAO.getByPatientId(patientId);
        
        // Sort by date descending
        demandes.sort((d1, d2) -> d2.getDate().compareTo(d1.getDate()));
        
        // Take only up to the limit
        if (limit > 0 && demandes.size() > limit) {
            demandes = demandes.subList(0, limit);
        }
        
        // Sort by date ascending for display
        demandes.sort(Comparator.comparing(Demande::getDate));
        
        Map<LocalDateTime, Float> waterTrend = new LinkedHashMap<>();
        for (Demande demande : demandes) {
            waterTrend.put(demande.getDate(), demande.getEau());
        }
        
        return waterTrend;
    }
    
    /**
     * Generate meal count trend from patient demands
     * @param patientId The patient ID
     * @param limit Maximum number of demands to include (most recent)
     * @return Map of date to meal count
     */
    public static Map<LocalDateTime, Integer> getMealCountTrend(int patientId, int limit) {
        DemandeDAO demandeDAO = new DemandeDAO();
        List<Demande> demandes = demandeDAO.getByPatientId(patientId);
        
        // Sort by date descending
        demandes.sort((d1, d2) -> d2.getDate().compareTo(d1.getDate()));
        
        // Take only up to the limit
        if (limit > 0 && demandes.size() > limit) {
            demandes = demandes.subList(0, limit);
        }
        
        // Sort by date ascending for display
        demandes.sort(Comparator.comparing(Demande::getDate));
        
        Map<LocalDateTime, Integer> mealTrend = new LinkedHashMap<>();
        for (Demande demande : demandes) {
            mealTrend.put(demande.getDate(), demande.getNbr_repas());
        }
        
        return mealTrend;
    }
    
    /**
     * Generate activity duration trend from patient demands
     * @param patientId The patient ID
     * @param limit Maximum number of demands to include (most recent)
     * @return Map of date to activity duration
     */
    public static Map<LocalDateTime, Float> getActivityDurationTrend(int patientId, int limit) {
        DemandeDAO demandeDAO = new DemandeDAO();
        List<Demande> demandes = demandeDAO.getByPatientId(patientId);
        
        // Sort by date descending
        demandes.sort((d1, d2) -> d2.getDate().compareTo(d1.getDate()));
        
        // Take only up to the limit
        if (limit > 0 && demandes.size() > limit) {
            demandes = demandes.subList(0, limit);
        }
        
        // Sort by date ascending for display
        demandes.sort(Comparator.comparing(Demande::getDate));
        
        Map<LocalDateTime, Float> activityTrend = new LinkedHashMap<>();
        for (Demande demande : demandes) {
            activityTrend.put(demande.getDate(), demande.getDuree_activite());
        }
        
        return activityTrend;
    }
    
    /**
     * Generate calorie consumption trend from patient demands
     * @param patientId The patient ID
     * @param limit Maximum number of demands to include (most recent)
     * @return Map of date to calorie consumption
     */
    public static Map<LocalDateTime, Float> getCalorieTrend(int patientId, int limit) {
        DemandeDAO demandeDAO = new DemandeDAO();
        List<Demande> demandes = demandeDAO.getByPatientId(patientId);
        
        // Sort by date descending
        demandes.sort((d1, d2) -> d2.getDate().compareTo(d1.getDate()));
        
        // Take only up to the limit
        if (limit > 0 && demandes.size() > limit) {
            demandes = demandes.subList(0, limit);
        }
        
        // Sort by date ascending for display
        demandes.sort(Comparator.comparing(Demande::getDate));
        
        Map<LocalDateTime, Float> calorieTrend = new LinkedHashMap<>();
        for (Demande demande : demandes) {
            if (demande.getCalories() != null) {
                calorieTrend.put(demande.getDate(), demande.getCalories());
            }
        }
        
        return calorieTrend;
    }
    
    /**
     * Generate sleep pattern trend from patient demands
     * @param patientId The patient ID
     * @param limit Maximum number of demands to include (most recent)
     * @return Map of date to sleep pattern
     */
    public static Map<LocalDateTime, String> getSleepTrend(int patientId, int limit) {
        DemandeDAO demandeDAO = new DemandeDAO();
        List<Demande> demandes = demandeDAO.getByPatientId(patientId);
        
        // Sort by date descending
        demandes.sort((d1, d2) -> d2.getDate().compareTo(d1.getDate()));
        
        // Take only up to the limit
        if (limit > 0 && demandes.size() > limit) {
            demandes = demandes.subList(0, limit);
        }
        
        // Sort by date ascending for display
        demandes.sort(Comparator.comparing(Demande::getDate));
        
        Map<LocalDateTime, String> sleepTrend = new LinkedHashMap<>();
        for (Demande demande : demandes) {
            if (demande.getSommeil() != null) {
                sleepTrend.put(demande.getDate(), demande.getSommeil());
            }
        }
        
        return sleepTrend;
    }
    
    /**
     * Get a patient's adherence to recommendations
     * @param patientId The patient ID
     * @return A percentage between 0 and 100 representing adherence
     */
    public static double getRecommendationAdherence(int patientId) {
        DemandeDAO demandeDAO = new DemandeDAO();
        RecommandationDAO recommandationDAO = new RecommandationDAO();
        
        List<Demande> demandes = demandeDAO.getByPatientId(patientId);
        
        if (demandes.isEmpty()) return 0.0;
        
        // Count demands that have associated recommendations
        int demandesWithRecs = 0;
        for (Demande demande : demandes) {
            Recommandation rec = recommandationDAO.getByDemandeId(demande.getId());
            if (rec != null) {
                demandesWithRecs++;
            }
        }
        
        return (double) demandesWithRecs / demandes.size() * 100.0;
    }
    
    /**
     * Calculate health score based on the patient's lifestyle patterns
     * @param patientId The patient ID
     * @return A score from 0 to 100
     */
    public static int calculateHealthScore(int patientId) {
        DemandeDAO demandeDAO = new DemandeDAO();
        List<Demande> demandes = demandeDAO.getByPatientId(patientId);
        
        if (demandes.isEmpty()) return 50; // Default score
        
        // Get most recent demand
        demandes.sort((d1, d2) -> d2.getDate().compareTo(d1.getDate()));
        Demande latestDemande = demandes.get(0);
        
        // Scoring factors (each can contribute up to 20 points)
        int waterScore = calculateWaterScore(latestDemande.getEau());
        int mealScore = calculateMealScore(latestDemande.getNbr_repas(), latestDemande.isSnacks());
        int activityScore = calculateActivityScore(latestDemande.getDuree_activite());
        int sleepScore = calculateSleepScore(latestDemande.getSommeil());
        int consistencyScore = calculateConsistencyScore(demandes);
        
        // Total score
        return waterScore + mealScore + activityScore + sleepScore + consistencyScore;
    }
    
    private static int calculateWaterScore(float waterConsumption) {
        // Ideal water consumption is around 2L per day
        if (waterConsumption >= 2.0f) return 20;
        if (waterConsumption >= 1.5f) return 15;
        if (waterConsumption >= 1.0f) return 10;
        if (waterConsumption >= 0.5f) return 5;
        return 0;
    }
    
    private static int calculateMealScore(int mealCount, boolean hasSnacks) {
        // Ideal is 3 meals per day with healthy snacks
        if (mealCount == 3 && !hasSnacks) return 18;
        if (mealCount == 3 && hasSnacks) return 20;
        if (mealCount == 2) return 10;
        if (mealCount > 3) return 15; // Multiple smaller meals can also be good
        return 5;
    }
    
    private static int calculateActivityScore(float activityDuration) {
        // WHO recommends at least 150 minutes of moderate activity per week (about 30 minutes per day)
        if (activityDuration >= 60) return 20;
        if (activityDuration >= 30) return 15;
        if (activityDuration >= 15) return 10;
        if (activityDuration > 0) return 5;
        return 0;
    }
    
    private static int calculateSleepScore(String sleepPattern) {
        if (sleepPattern == null) return 10; // Default if not specified
        
        // Different sleep patterns and their scores
        sleepPattern = sleepPattern.toLowerCase();
        
        if (sleepPattern.contains("7") || sleepPattern.contains("8")) return 20;
        if (sleepPattern.contains("6") || sleepPattern.contains("9")) return 15;
        if (sleepPattern.contains("5") || sleepPattern.contains("10")) return 10;
        if (sleepPattern.contains("insomn") || sleepPattern.contains("trouble")) return 5;
        
        return 10; // Default score
    }
    
    private static int calculateConsistencyScore(List<Demande> demandes) {
        if (demandes.size() < 2) return 10; // Not enough data
        
        // Check regularity of entries
        demandes.sort(Comparator.comparing(Demande::getDate));
        
        LocalDateTime firstDate = demandes.get(0).getDate();
        LocalDateTime lastDate = demandes.get(demandes.size() - 1).getDate();
        
        long daysBetween = ChronoUnit.DAYS.between(firstDate, lastDate);
        
        if (daysBetween == 0) return 10; // Only one day of data
        
        double entriesPerDay = (double) demandes.size() / daysBetween;
        
        // More regular entries get higher scores
        if (entriesPerDay >= 0.8) return 20; // Very consistent
        if (entriesPerDay >= 0.5) return 15;
        if (entriesPerDay >= 0.3) return 10;
        
        return 5; // Not very consistent
    }
    
    /**
     * Get health insights based on a patient's data
     * @param patientId The patient ID
     * @return A list of insight strings
     */
    public static List<String> getHealthInsights(int patientId) {
        DemandeDAO demandeDAO = new DemandeDAO();
        List<Demande> demandes = demandeDAO.getByPatientId(patientId);
        
        List<String> insights = new ArrayList<>();
        
        if (demandes.isEmpty()) {
            insights.add("Pas assez de données pour générer des insights. Veuillez créer plus de demandes.");
            return insights;
        }
        
        // Sort demandes by date (newest first)
        demandes.sort((d1, d2) -> d2.getDate().compareTo(d1.getDate()));
        
        // Get last 3 demandes for trend analysis (if available)
        List<Demande> recentDemandes = demandes.size() >= 3 
            ? demandes.subList(0, 3) 
            : demandes;
        
        // Water consumption insight
        if (recentDemandes.size() >= 2) {
            float latestWater = recentDemandes.get(0).getEau();
            float prevWater = recentDemandes.get(1).getEau();
            
            if (latestWater > prevWater) {
                insights.add("Votre consommation d'eau a augmenté. Continuez comme ça !");
            } else if (latestWater < prevWater) {
                insights.add("Votre consommation d'eau a diminué. Essayez de boire au moins 2L par jour.");
            }
        }
        
        // Latest demand insights
        Demande latest = demandes.get(0);
        
        // Water
        if (latest.getEau() < 1.5f) {
            insights.add("Votre consommation d'eau est inférieure aux recommandations. Visez au moins 1.5L par jour.");
        }
        
        // Activity
        if (latest.getDuree_activite() < 30) {
            insights.add("Votre niveau d'activité est faible. Essayez d'atteindre au moins 30 minutes d'activité par jour.");
        } else if (latest.getDuree_activite() >= 60) {
            insights.add("Excellent niveau d'activité ! Continuez à rester actif.");
        }
        
        // Meals
        if (latest.getNbr_repas() < 3) {
            insights.add("Vous avez déclaré moins de 3 repas par jour. Des repas réguliers sont importants pour une alimentation équilibrée.");
        }
        
        // Sleep
        if (latest.getSommeil() != null) {
            String sleep = latest.getSommeil().toLowerCase();
            if (sleep.contains("insomn") || sleep.contains("trouble") || sleep.contains("diffic")) {
                insights.add("Vous semblez avoir des problèmes de sommeil. Envisagez de consulter un spécialiste ou d'améliorer votre hygiène de sommeil.");
            }
        }
        
        // Default insight if none generated
        if (insights.isEmpty()) {
            insights.add("Continuez à suivre votre santé régulièrement pour obtenir des insights personnalisés.");
        }
        
        return insights;
    }
}