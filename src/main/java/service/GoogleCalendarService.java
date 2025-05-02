package service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import model.RendezVous;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.api.services.calendar.model.EventReminder;
import java.util.Arrays;

public class GoogleCalendarService {
    private static final String APPLICATION_NAME = "Application de Gestion Médicale";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private Calendar service;

    public GoogleCalendarService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Charger les informations d'identification du client
        InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Construire le flux d'autorisation
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    // Ajouter un rendez-vous à Google Calendar
    public String addEventToCalendar(RendezVous rendezVous) throws IOException {
        Event event = new Event()
                .setSummary("Rendez-vous médical")
                .setLocation("Cabinet médical")
                .setDescription(rendezVous.getDescription());

        LocalDateTime startDateTime = rendezVous.getDateheure();
        Date startDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
        DateTime start = new DateTime(startDate);
        EventDateTime eventStart = new EventDateTime()
                .setDateTime(start)
                .setTimeZone("Europe/Paris");
        event.setStart(eventStart);

        // Fin du rendez-vous (+ 30 minutes par défaut)
        LocalDateTime endDateTime = rendezVous.getDateheure().plusMinutes(30);
        Date endDate = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());
        DateTime end = new DateTime(endDate);
        EventDateTime eventEnd = new EventDateTime()
                .setDateTime(end)
                .setTimeZone("Europe/Paris");
        event.setEnd(eventEnd);

        // Définir des rappels
        EventReminder[] reminderOverrides = new EventReminder[] {
                new EventReminder().setMethod("email").setMinutes(24 * 60), // 1 jour avant
                new EventReminder().setMethod("popup").setMinutes(30) // 30 minutes avant
        };

        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(Arrays.asList(reminderOverrides));

        event.setReminders(reminders);

        // Ajouter l'événement au calendrier primaire
        event = service.events().insert("primary", event).execute();
        return event.getHtmlLink(); // URL vers l'événement dans Google Calendar
    }

    // Obtenir les événements du calendrier
    public List<Event> getEvents() throws IOException {
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        return events.getItems();
    }

    // Supprimer un événement du calendrier
    public void deleteEvent(String eventId) throws IOException {
        service.events().delete("primary", eventId).execute();
    }

    // Mettre à jour un événement du calendrier
    public Event updateEvent(String eventId, RendezVous rendezVous) throws IOException {
        // Récupérer l'événement existant
        Event event = service.events().get("primary", eventId).execute();

        // Mettre à jour les détails
        event.setDescription(rendezVous.getDescription());

        LocalDateTime startDateTime = rendezVous.getDateheure();
        Date startDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
        DateTime start = new DateTime(startDate);
        EventDateTime eventStart = new EventDateTime()
                .setDateTime(start)
                .setTimeZone("Europe/Paris");
        event.setStart(eventStart);

        LocalDateTime endDateTime = rendezVous.getDateheure().plusMinutes(30);
        Date endDate = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());
        DateTime end = new DateTime(endDate);
        EventDateTime eventEnd = new EventDateTime()
                .setDateTime(end)
                .setTimeZone("Europe/Paris");
        event.setEnd(eventEnd);

        return service.events().update("primary", eventId, event).execute();
    }
}