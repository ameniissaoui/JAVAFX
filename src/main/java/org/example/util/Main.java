package org.example.util;
import org.example.models.Admin;
import org.example.models.Medecin;
import org.example.models.Patient;
import org.example.services.AdminService;
import org.example.services.MedecinService;
import org.example.services.PatientService;

import java.util.Date;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        //MaConnexion c = MaConnexion.getInstance();
        //UserService userService = new UserService();
        //User user = new User("ameni" , "issaoui" , "ameni@gmail.com" , "aaaa" , "medecin" ,  Date.valueOf("2003-08-02") , "28236733" , "genecologue" , "cdccd" );
        //userService.add(user);
        // Test AdminService
        testAdminService();

        // Test MedecinService
        testMedecinService();

        // Test PatientService
        testPatientService();
        }
        private static void testAdminService() {
            System.out.println("===== Testing AdminService =====");
            AdminService adminService = new AdminService();

            // Create an admin
            Admin admin = new Admin("AdminNom", "AdminPrenom", "admin@example.com", "password123", new Date(), "1234567890");
            adminService.add(admin);
            System.out.println("Admin added with ID: " + admin.getId());

            // Get all admins
            List<Admin> admins = adminService.getAll();
            System.out.println("All admins:");
            for (Admin a : admins) {
                System.out.println(a);
            }

            // Get one admin
            if (!admins.isEmpty()) {
                int adminId = admins.get(0).getId();
                Admin retrievedAdmin = adminService.getOne(adminId);
                System.out.println("Retrieved admin: " + retrievedAdmin);

                // Update admin
                retrievedAdmin.setEmail("updated.admin@example.com");
                System.out.println("Admin updated");

                // Verify update
                retrievedAdmin = adminService.getOne(adminId);
                System.out.println("Updated admin: " + retrievedAdmin);

                // Delete admin
                //adminService.delete(retrievedAdmin);
                //System.out.println("Admin deleted");
            }
        }

        private static void testMedecinService() {
            System.out.println("\n===== Testing MedecinService =====");
            MedecinService medecinService = new MedecinService();

            // Create a medecin
            Medecin medecin = new Medecin("Nom", "MedecinPrenom", "medecin@example.com",
                    "password123", new Date(), "1234567890",
                    "Cardiologie", "Doctorat en Médecine");
            medecinService.add(medecin);
            System.out.println("Medecin added with ID: " + medecin.getId());

            // Get all medecins
            List<Medecin> medecins = medecinService.getAll();
            System.out.println("All medecins:");
            for (Medecin m : medecins) {
                System.out.println(m);
            }

            // Get one medecin
            if (!medecins.isEmpty()) {
                int medecinId = medecins.get(0).getId();
                Medecin retrievedMedecin = medecinService.getOne(medecinId);
                System.out.println("Retrieved medecin: " + retrievedMedecin);

                // Update medecin
                retrievedMedecin.setEmail("updated.medecin@example.com");
                retrievedMedecin.setSpecialite("Neurologie");
                medecinService.update(retrievedMedecin);
                System.out.println("Medecin updated");

                // Verify update
                retrievedMedecin = medecinService.getOne(medecinId);
                System.out.println("Updated medecin: " + retrievedMedecin);

                // Delete medecin
                //medecinService.delete(retrievedMedecin);
                //System.out.println("Medecin deleted");
            }
        }

        private static void testPatientService() {
            System.out.println("\n===== Testing PatientService =====");
            PatientService patientService = new PatientService();

            // Create a patient
            Patient patient = new Patient("PatientNom", "PatientPrenom", "patient@example.com",
                    "password123", new Date(), "1234567890");
            patientService.add(patient);
            System.out.println("Patient added with ID: " + patient.getId());

            // Get all patients
            List<Patient> patients = patientService.getAll();
            System.out.println("All patients:");
            for (Patient p : patients) {
                System.out.println(p);
            }

            // Get one patient
            if (!patients.isEmpty()) {
                int patientId = patients.get(0).getId();
                Patient retrievedPatient = patientService.getOne(patientId);
                System.out.println("Retrieved patient: " + retrievedPatient);

                // Update patient
                retrievedPatient.setEmail("updated.patient@example.com");
                patientService.update(retrievedPatient);
                System.out.println("Patient updated");

                // Verify update
                retrievedPatient = patientService.getOne(patientId);
                System.out.println("Updated patient: " + retrievedPatient);

                // Delete patient
                //patientService.delete(retrievedPatient);
                //System.out.println("Patient deleted");
            }
        }
}