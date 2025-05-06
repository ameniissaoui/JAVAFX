package org.example.services;

import org.example.models.RendezVous;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.time.format.DateTimeFormatter;

public class GoogleCalendarService {
    private static final String APPLICATION_NAME = "ChronoSerena";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private final Calendar service;

    public GoogleCalendarService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public String addEventToCalendar(RendezVous rendezVous) throws Exception {
        Event event = new Event()
            .setSummary("Rendez-vous: " + rendezVous.getDescription())
            .setDescription(rendezVous.getDescription());

        DateTime startDateTime = new DateTime(rendezVous.getDateheure().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        EventDateTime start = new EventDateTime()
            .setDateTime(startDateTime)
            .setTimeZone("Europe/Paris");
        event.setStart(start);

        DateTime endDateTime = new DateTime(rendezVous.getDateheure().plusMinutes(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        EventDateTime end = new EventDateTime()
            .setDateTime(endDateTime)
            .setTimeZone("Europe/Paris");
        event.setEnd(end);

        Event createdEvent = service.events().insert("primary", event).execute();
        return createdEvent.getHtmlLink();
    }

    public void updateEventInCalendar(String eventId, RendezVous rendezVous) throws Exception {
        Event event = new Event()
            .setSummary("Rendez-vous: " + rendezVous.getDescription())
            .setDescription(rendezVous.getDescription());

        DateTime startDateTime = new DateTime(rendezVous.getDateheure().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        EventDateTime start = new EventDateTime()
            .setDateTime(startDateTime)
            .setTimeZone("Europe/Paris");
        event.setStart(start);

        DateTime endDateTime = new DateTime(rendezVous.getDateheure().plusMinutes(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        EventDateTime end = new EventDateTime()
            .setDateTime(endDateTime)
            .setTimeZone("Europe/Paris");
        event.setEnd(end);

        service.events().update("primary", eventId, event).execute();
    }

//    public void deleteEventFromCalendar(RendezVous rendezVous) throws IOException {
//        if (rendezVous.getGoogleEventId() == null) {
//            return;
//        }
//        service.events().delete("primary", rendezVous.getGoogleEventId()).execute();
//    }
    public void deleteEvent(String eventId) throws IOException {
        if (eventId == null || eventId.isEmpty()) return;

        service.events().delete("primary", eventId).execute();
        System.out.println("✅ Événement supprimé de Google Calendar : " + eventId);
    }
}