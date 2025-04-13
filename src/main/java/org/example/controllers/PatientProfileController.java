package org.example.controllers;

import javafx.event.ActionEvent;
import org.example.models.Patient;
import org.example.models.User;
import org.example.services.PatientService;

import java.net.URL;
import java.util.ResourceBundle;

public class PatientProfileController extends BaseProfileController {
    private PatientService patientService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        patientService = new PatientService();
    }

    @Override
    public void setUser(User user) {
        if (user instanceof Patient) {
            super.setUser(user);
        } else {
            throw new IllegalArgumentException("User must be a Patient");
        }
    }

    public void setPatient(Patient patient) {
        this.currentUser = patient;
        loadUserData();
    }

    @Override
    protected void saveUser() {
        if (currentUser instanceof Patient) {
            patientService.update((Patient) currentUser);
        }
    }

    public void handleOverview(ActionEvent actionEvent) {
    }

    public void handleChangePassword(ActionEvent actionEvent) {
    }

    public void handleModifier(ActionEvent actionEvent) {
    }
}