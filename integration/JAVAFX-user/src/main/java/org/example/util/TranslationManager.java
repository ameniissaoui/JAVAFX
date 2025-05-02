package org.example.util;

import java.util.*;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TranslationManager {
    private static TranslationManager instance;
    private ResourceBundle resourceBundle;
    private final ObjectProperty<Locale> currentLocale = new SimpleObjectProperty<>();
    private final Map<String, Locale> supportedLocales = new HashMap<>();

    private TranslationManager() {
        // Add supported languages
        supportedLocales.put("Français", Locale.FRENCH);
        supportedLocales.put("English", Locale.ENGLISH);
        supportedLocales.put("العربية", new Locale("ar"));

        // Set default locale
        setLocale("Français");
    }

    public static TranslationManager getInstance() {
        if (instance == null) {
            instance = new TranslationManager();
        }
        return instance;
    }

    public void setLocale(String languageName) {
        Locale locale = supportedLocales.get(languageName);
        if (locale != null) {
            currentLocale.set(locale);
            try {
                resourceBundle = ResourceBundle.getBundle("messages.messages", locale);
                System.out.println("Loaded resource bundle for locale: " + locale);
            } catch (MissingResourceException e) {
                System.err.println("Failed to load resource bundle for locale: " + locale);
                throw e;
            }
        }
    }

    public String translate(String key) {
        try {
            String translated = resourceBundle.getString(key);
            if (translated == null || translated.trim().isEmpty()) {
                System.err.println("Translation for key '" + key + "' is empty or null for locale: " + currentLocale.get());
                return key;
            }
            return translated;
        } catch (Exception e) {
            System.err.println("Missing translation for key: " + key + " in locale: " + currentLocale.get());
            return key;
        }
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public ObjectProperty<Locale> currentLocaleProperty() {
        return currentLocale;
    }

    // Additional method to get current locale name
    public String getCurrentLocaleName() {
        for (Map.Entry<String, Locale> entry : supportedLocales.entrySet()) {
            if (entry.getValue().equals(currentLocale.get())) {
                return entry.getKey();
            }
        }
        return "Français"; // Default if not found
    }

    // Method to support RTL languages like Arabic
    public boolean isCurrentLocaleRTL() {
        return currentLocale.get().getLanguage().equals("ar");
    }
}