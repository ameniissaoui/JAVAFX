package org.example.controllers;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Div;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.models.Admin;
import org.example.models.Medecin;
import org.example.models.Patient;
import org.example.models.User;
import org.example.services.AdminService;
import org.example.services.MedecinService;
import org.example.services.PatientService;
import org.example.util.SessionManager;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.control.TableCell;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.control.Tooltip;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.stage.FileChooser;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.element.Text;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
public class AdminDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private TableView<UserTableData> usersTable;
    @FXML private TableColumn<UserTableData, String> nomColumn;
    @FXML private TableColumn<UserTableData, String> prenomColumn;
    @FXML private TableColumn<UserTableData, String> emailColumn;
    @FXML private TableColumn<UserTableData, String> telephoneColumn;
    @FXML private TableColumn<UserTableData, String> typeColumn;
    @FXML private TableColumn<UserTableData, String> dateNaissanceColumn;
    @FXML private TableColumn<UserTableData, Void> actionsColumn;
    @FXML private TableColumn<UserTableData, Boolean> bannedColumn;
    @FXML private Button tablesButton;
    @FXML private Button eventButton;
    @FXML private Button reservationButton;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private TextField searchField;
    @FXML private Button profileButton;
    @FXML private Button historique;
    @FXML private Button suivi;
    @FXML private Button buttoncommande;


    private AdminService adminService = new AdminService();
    private MedecinService medecinService = new MedecinService();
    private PatientService patientService = new PatientService();

    private ObservableList<UserTableData> allUsers = FXCollections.observableArrayList();
    private ObservableList<UserTableData> filteredUsers = FXCollections.observableArrayList();


    @FXML private Label detailNomLabel;
    @FXML private Label detailPrenomLabel;
    @FXML private Label detailEmailLabel;
    @FXML private Label detailTelephoneLabel;
    @FXML private Label detailDateNaissanceLabel;
    @FXML private Label detailRoleLabel;
    @FXML private Label detailStatutLabel;
    @FXML private Label detailSpecialiteLabel;
    @FXML private Label detailDiplomeLabel;
    @FXML private VBox detailsVBox;
    @FXML private HBox specialiteHBox;
    @FXML private HBox diplomeHBox;

    //verfication diploma
    @FXML private Label detailVerificationLabel;
    @FXML private Button verifyDiplomaButton;
    @FXML private HBox verificationHBox;
    private static final String TWILIO_ACCOUNT_SID = "AC10f70f27747b41922e66438d7505dccc";
    private static final String TWILIO_AUTH_TOKEN = "0b82b2a71654b5a329594b5ba32a6e07";
    private static final String TWILIO_WHATSAPP_NUMBER = "whatsapp:+14155238886";
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Check if user is logged in and is an admin
        if (!SessionManager.getInstance().isLoggedIn() || !SessionManager.getInstance().isAdmin()) {
            showErrorDialog("Erreur", "Accès non autorisé");
            handleLogout();
            return;
        }

        // Setup welcome message
        Admin currentAdmin = SessionManager.getInstance().getCurrentAdmin();
        if (currentAdmin != null) {
            welcomeLabel.setText("Bienvenue, " + currentAdmin.getPrenom() + " " + currentAdmin.getNom() + "!");
        }

        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTableColumns();
        setupFilterComboBox();
        setupSearchField();
        tablesButton.setOnAction(event -> handleTablesRedirect());
        eventButton.setOnAction(event -> handleeventRedirect());
        reservationButton.setOnAction(event -> handlereservationRedirect());
        historique.setOnAction(event -> handleHistoriqueRedirect());
        suivi.setOnAction(event -> handleSuiviRedirect());
        buttoncommande.setOnAction(event -> handleCommandeRedirect());

        loadUsers();
        detailsVBox.setVisible(false);
        detailsVBox.setManaged(false);

        // Make the usersTable take all available vertical space
        VBox.setVgrow(usersTable, Priority.ALWAYS);

        //about the user details
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showUserDetails(newSelection);
                detailsVBox.setVisible(true); // Hide the details VBox
                detailsVBox.setManaged(true);
                VBox.setVgrow(usersTable, Priority.ALWAYS);
                VBox.setVgrow(detailsVBox, Priority.NEVER); // Don't let details expand
            } else {
                clearUserDetails();
                detailsVBox.setVisible(false); // Hide the details VBox
                detailsVBox.setManaged(false); // This will make it not take up space when invisible
            }
        });
        Twilio.init(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN);
    }
    private void showUserDetails(UserTableData userData) {
        // Fetch the full user object based on type
        switch (userData.getType()) {
            case "Admin" -> {
                Admin admin = adminService.getOne(userData.getId());
                populateDetails(admin);
            }
            case "Médecin" -> {
                Medecin medecin = medecinService.getOne(userData.getId());
                populateDetails(medecin);
            }
            case "Patient" -> {
                Patient patient = patientService.getOne(userData.getId());
                populateDetails(patient);
            }
        }
    }
    private void populateDetails(User user) {
        // Common details
        if (user == null) {
            // Hide details or show an error message
            detailsVBox.setVisible(false);
            return;
        }
        detailNomLabel.setText("Nom: " + user.getNom());
        detailPrenomLabel.setText("Prénom: " + user.getPrenom());
        detailEmailLabel.setText("Email: " + user.getEmail());
        detailTelephoneLabel.setText("Téléphone: " + user.getTelephone());
        detailDateNaissanceLabel.setText("Date de naissance: " + formatDate(user.getDateNaissance()));
        detailRoleLabel.setText("Rôle: " + getUserRole(user));
        detailStatutLabel.setText("Statut: " + (user.isBanned() ? "Banni" : "Actif"));

        // Role-specific details
        if (user instanceof Medecin medecin) {
            detailSpecialiteLabel.setText("Spécialité: " + medecin.getSpecialite());
            // Update the diploma display to show filename and a view button
            String diplomaPath = medecin.getDiploma();
            if (diplomaPath != null && !diplomaPath.isEmpty()) {
                // Extract just the filename from the path
                String fileName = new File(diplomaPath).getName();
                detailDiplomeLabel.setText("Diplôme: " + fileName);

                // Create a "View" button
                Button viewDiplomaButton = new Button("Voir");
                viewDiplomaButton.getStyleClass().add("view-btn");
                viewDiplomaButton.setOnAction(e -> openDiplomaFile(diplomaPath));

                // Add the button to the HBox next to the label
                if (diplomeHBox.getChildren().size() > 1) {
                    // Remove any existing button if present
                    diplomeHBox.getChildren().remove(1, diplomeHBox.getChildren().size());
                }
                diplomeHBox.getChildren().add(viewDiplomaButton);
            } else {
                detailDiplomeLabel.setText("Diplôme: Non disponible");
            }
            boolean isVerified = medecin.isIs_verified(); // Assuming this method exists
            detailVerificationLabel.setText("Statut de vérification: " + (isVerified ? "Vérifié" : "Non vérifié"));

            // Configure the verification button
            verifyDiplomaButton.setText(isVerified ? "Annuler la vérification" : "Vérifier le diplôme");
            verifyDiplomaButton.setStyle(isVerified ?
                    "-fx-background-color: #cbd5e1; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;" :
                    "-fx-background-color: #14b8a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
            verifyDiplomaButton.setOnAction(e -> handleVerifyDoctor(medecin));

            specialiteHBox.setVisible(true);
            diplomeHBox.setVisible(true);
            verificationHBox.setVisible(true);
            detailSpecialiteLabel.setVisible(true);
            detailDiplomeLabel.setVisible(true);
            detailVerificationLabel.setVisible(true);
            verifyDiplomaButton.setVisible(true);
        } else {
            // For non-doctor users, hide the doctor-specific fields
            specialiteHBox.setVisible(false);
            diplomeHBox.setVisible(false);
            verificationHBox.setVisible(false);
        }
    }
    private void handleVerifyDoctor(Medecin medecin) {
        boolean currentVerificationStatus = medecin.isIs_verified();
        String action = currentVerificationStatus ? "annuler la vérification de" : "vérifier";

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation");
        confirmDialog.setHeaderText("Êtes-vous sûr de vouloir " + action + " ce médecin ?");
        confirmDialog.setContentText("Vous confirmez avoir vérifié ses diplômes et qualifications professionnelles.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Toggle verification status
                if (currentVerificationStatus) {
                    medecinService.unverifyDoctor(medecin.getId());
                } else {
                    medecinService.verifyDoctor(medecin.getId());
                    sendWhatsAppNotification(medecin);
                }
                loadUsers();
                for (UserTableData userData : filteredUsers) {
                    if (userData.getId() == medecin.getId() && userData.getType().equals("Médecin")) {
                        usersTable.getSelectionModel().select(userData);
                        break;
                    }
                }

                showInfoDialog("Succès", "Le statut de vérification du médecin a été mis à jour avec succès");
            } catch (Exception e) {
                showErrorDialog("Erreur", "Échec de l'opération: " + e.getMessage());
            }
        }
    }
    private void openDiplomaFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            showErrorDialog("Erreur", "Chemin du fichier invalide");
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            showErrorDialog("Erreur", "Le fichier n'existe pas: " + filePath);
            return;
        }

        try {
            // Open the file with the default system application
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(file);
            } else {
                showErrorDialog("Erreur", "Votre système ne prend pas en charge l'ouverture de fichiers");
            }
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible d'ouvrir le fichier: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private String getUserRole(User user) {
        if (user instanceof Admin) {
            return "Admin";
        } else if (user instanceof Medecin) {
            return "Médecin";
        } else if (user instanceof Patient) {
            return "Patient";
        }
        return "Inconnu";
    }
    private void clearUserDetails() {
        detailNomLabel.setText("Nom: ");
        detailPrenomLabel.setText("Prénom: ");
        detailEmailLabel.setText("Email: ");
        detailTelephoneLabel.setText("Téléphone: ");
        detailDateNaissanceLabel.setText("Date de naissance: ");
        detailRoleLabel.setText("Rôle: ");
        detailStatutLabel.setText("Statut: ");
        detailVerificationLabel.setText("Statut de vérification: ");
        specialiteHBox.setVisible(false);
        diplomeHBox.setVisible(false);
        verificationHBox.setVisible(false);
    }

    private void handleTablesRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/back/showProduit.fxml"));
            Stage stage = (Stage) tablesButton.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des produits: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void setupTableColumns() {
        // Basic column setup with strict width control
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        dateNaissanceColumn.setCellValueFactory(new PropertyValueFactory<>("dateNaissance"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));


        nomColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        prenomColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        emailColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        telephoneColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        dateNaissanceColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        typeColumn.setStyle("-fx-alignment: CENTER;");
        actionsColumn.setStyle("-fx-alignment: CENTER;");


        // Role/Type column styling with centered content
        typeColumn.setCellFactory(column -> new TableCell<UserTableData, String>() {
            private final Label roleLabel = new Label();
            private final StackPane rolePane = new StackPane(roleLabel);

            {
                rolePane.getStyleClass().add("role-cell");
                setAlignment(Pos.CENTER); // Ensure cell content is centered
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                roleLabel.setText(item);
                rolePane.getStyleClass().removeAll("role-admin", "role-medecin", "role-patient");

                switch (item) {
                    case "Admin":
                        rolePane.getStyleClass().add("role-admin");
                        break;
                    case "Médecin":
                        rolePane.getStyleClass().add("role-medecin");
                        break;
                    case "Patient":
                        rolePane.getStyleClass().add("role-patient");
                        break;
                }

                setGraphic(rolePane);
                setText(null);
            }
        });

        // Actions column with centered buttons that align with header
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button banButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox pane = new HBox(8, banButton, deleteButton);

            {
                // Setup delete button with trash icon
                FontIcon deleteIcon = new FontIcon(BootstrapIcons.TRASH);
                deleteIcon.setIconSize(16);
                deleteIcon.setIconColor(javafx.scene.paint.Color.WHITE);
                deleteButton.setGraphic(deleteIcon);
                deleteButton.getStyleClass().addAll("action-btn", "delete-btn");
                deleteButton.setTooltip(new Tooltip("Supprimer"));

                FontIcon banIcon = new FontIcon(BootstrapIcons.LOCK);
                banIcon.setIconSize(16);
                banIcon.setIconColor(javafx.scene.paint.Color.WHITE);
                banButton.setGraphic(banIcon);
                banButton.getStyleClass().addAll("action-btn", "ban-btn");

                // Make buttons compact squares
                deleteButton.setPrefSize(32, 32);
                deleteButton.setMinSize(32, 32);
                deleteButton.setMaxSize(32, 32);
                banButton.setPrefSize(32, 32);
                banButton.setMinSize(32, 32);
                banButton.setMaxSize(32, 32);
                // Set perfect center alignment
                pane.setAlignment(Pos.CENTER);
                setAlignment(Pos.CENTER);

                // Button actions
                deleteButton.setOnAction(event -> {
                    UserTableData userData = getTableView().getItems().get(getIndex());
                    handleDeleteUser(userData);
                });
                banButton.setOnAction(event -> {
                    UserTableData userData = getTableView().getItems().get(getIndex());
                    handleBanUnbanUser(userData);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    UserTableData userData = getTableView().getItems().get(getIndex());

                    // Update ban button appearance based on user status
                    FontIcon icon;
                    if (userData.isBanned()) {
                        icon = new FontIcon(BootstrapIcons.UNLOCK);
                        banButton.setTooltip(new Tooltip("Débloquer"));
                        banButton.getStyleClass().removeAll("ban-btn");
                        banButton.getStyleClass().add("unban-btn");
                    } else {
                        icon = new FontIcon(BootstrapIcons.LOCK);
                        banButton.setTooltip(new Tooltip("Bloquer"));
                        banButton.getStyleClass().removeAll("unban-btn");
                        banButton.getStyleClass().add("ban-btn");
                    }
                    icon.setIconSize(16);
                    icon.setIconColor(javafx.scene.paint.Color.WHITE);
                    banButton.setGraphic(icon);

                    setGraphic(pane);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        // Inside setupTableColumns method
        bannedColumn.setCellValueFactory(new PropertyValueFactory<>("banned"));
        bannedColumn.setCellFactory(column -> new TableCell<UserTableData, Boolean>() {
            @Override
            protected void updateItem(Boolean banned, boolean empty) {
                super.updateItem(banned, empty);

                if (empty || banned == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(null);
                    Label statusLabel = new Label(banned ? "Banni" : "Actif");
                    statusLabel.getStyleClass().add(banned ? "status-banned" : "status-active");
                    setGraphic(statusLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }
    private void handleBanUnbanUser(UserTableData userData) {
        boolean currentBanStatus = userData.isBanned();
        String action = currentBanStatus ? "débloquer" : "bloquer";

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation");
        confirmDialog.setHeaderText("Êtes-vous sûr de vouloir " + action + " cet utilisateur ?");
        confirmDialog.setContentText("Cette action peut être annulée ultérieurement.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                switch (userData.getType()) {
                    case "Admin" -> {
                        if (currentBanStatus) {
                            adminService.unbanUser(userData.getId());
                        } else {
                            adminService.banUser(userData.getId());
                        }
                    }
                    case "Médecin" -> {
                        if (currentBanStatus) {
                            medecinService.unbanUser(userData.getId());
                        } else {
                            medecinService.banUser(userData.getId());
                        }
                    }
                    case "Patient" -> {
                        if (currentBanStatus) {
                            patientService.unbanUser(userData.getId());
                        } else {
                            patientService.banUser(userData.getId());
                        }
                    }
                }
                loadUsers();
                showInfoDialog("Succès", "Utilisateur " + (currentBanStatus ? "débloqué" : "bloqué") + " avec succès");
            } catch (Exception e) {
                showErrorDialog("Erreur", "Échec de l'opération: " + e.getMessage());
            }
        }
    }
    @FXML private void handleProfileRedirect(ActionEvent event) {
        SceneManager.loadScene("/fxml/admin_profile.fxml", event);
    }
    private void applyFilters() {
        String filterType = filterComboBox.getValue();
        String searchText = searchField.getText().toLowerCase();
        filteredUsers.clear();
        for (UserTableData user : allUsers)
        {
            if (!filterType.equals("Tous") && !user.getType().equals(filterType)) {
                continue;
            }
            if (!searchText.isEmpty() &&
                    !user.getNom().toLowerCase().contains(searchText) &&
                    !user.getPrenom().toLowerCase().contains(searchText) &&
                    !user.getEmail().toLowerCase().contains(searchText)) {
                continue;
            }
            filteredUsers.add(user);
        }
        usersTable.setItems(filteredUsers);
    }
    private void setupFilterComboBox() {
        filterComboBox.setItems(FXCollections.observableArrayList(
                "Tous", "Admin", "Médecin", "Patient"));
        filterComboBox.setValue("Tous");
        filterComboBox.setOnAction(e -> applyFilters());
    }
    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }
    private void loadUsers() {
        allUsers.clear();

        // Load admins
        for (Admin admin : adminService.getAll()) {
            allUsers.add(new UserTableData(
                    admin.getId(),
                    admin.getNom(),
                    admin.getPrenom(),
                    admin.getEmail(),
                    admin.getTelephone(),
                    formatDate(admin.getDateNaissance()),
                    "Admin",
                    admin.isBanned()

            ));
        }

        // Load medecins
        for (Medecin medecin : medecinService.getAll()) {
            allUsers.add(new UserTableData(
                    medecin.getId(),
                    medecin.getNom(),
                    medecin.getPrenom(),
                    medecin.getEmail(),
                    medecin.getTelephone(),
                    formatDate(medecin.getDateNaissance()),
                    "Médecin",
                    medecin.isBanned()
            ));
        }

        // Load patients
        for (Patient patient : patientService.getAll()) {
            allUsers.add(new UserTableData(
                    patient.getId(),
                    patient.getNom(),
                    patient.getPrenom(),
                    patient.getEmail(),
                    patient.getTelephone(),
                    formatDate(patient.getDateNaissance()),
                    "Patient",
                    patient.isBanned()
            ));
        }

        applyFilters();
    }
    private String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(date);
    }
    private void handleDeleteUser(UserTableData userData) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Êtes-vous sûr de vouloir supprimer cet utilisateur ?");
        confirmDialog.setContentText("Cette action ne peut pas être annulée.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                switch (userData.getType()) {
                    case "Admin" -> {
                        Admin admin = adminService.getOne(userData.getId());
                        adminService.delete(admin);
                    }
                    case "Médecin" -> {
                        Medecin medecin = medecinService.getOne(userData.getId());
                        medecinService.delete(medecin);
                    }
                    case "Patient" -> {
                        Patient patient = patientService.getOne(userData.getId());
                        patientService.delete(patient);
                    }
                }
                loadUsers();
                showInfoDialog("Succès", "Utilisateur supprimé avec succès");
            } catch (Exception e) {
                showErrorDialog("Erreur", "Échec de la suppression: " + e.getMessage());
            }
        }
    }
    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML private void handleAddUser() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/RoleSelection.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Ajouter un utilisateur");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh table after adding a user
            loadUsers();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible d'ouvrir la fenêtre d'ajout: " + e.getMessage());
        }
    }
    @FXML private void handleLogout() {
        try {
            // Clear the session
            SessionManager.getInstance().clearSession();

            // Navigate to login page
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
        } catch (IOException e) {
            showErrorDialog("Erreur", "Échec de la déconnexion: " + e.getMessage());
        }
    }
    @FXML private void refreshTable() {
        loadUsers();
    }
    @FXML
    private void handleStatisticsRedirect(ActionEvent event) {
        SceneManager.loadScene("/fxml/statistics-view.fxml", event);
    }
    @FXML
    private void handleStatisticsCommandeRedirect(ActionEvent event) {
        SceneManager.loadScene("/fxml/back/statistics.fxml", event);
    }

    public static class UserTableData {
        private final int id;
        private final String nom;
        private final String prenom;
        private final String email;
        private final String telephone;
        private final String dateNaissance;
        private final String type;
        private final boolean banned;
        public UserTableData(int id, String nom, String prenom, String email, String telephone,
                             String dateNaissance, String type, boolean banned) {
            this.id = id;
            this.nom = nom;
            this.prenom = prenom;
            this.email = email;
            this.telephone = telephone != null ? telephone : "";
            this.dateNaissance = dateNaissance;
            this.type = type;
            this.banned = banned;
        }
        public int getId() { return id; }
        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
        public String getEmail() { return email; }
        public String getTelephone() { return telephone; }
        public String getDateNaissance() { return dateNaissance; }
        public String getType() { return type; }
        public boolean isBanned() { return banned; }

    }


    private void sendWhatsAppNotification(Medecin medecin) {
        try {
            String toPhoneNumber = medecin.getTelephone();
            if (toPhoneNumber == null || toPhoneNumber.isEmpty()) {
                System.err.println("No phone number available for doctor: " + medecin.getNom());
                showErrorDialog("Erreur", "Numéro de téléphone manquant pour le médecin.");
                return;
            }
            if (!toPhoneNumber.startsWith("+")) {
                toPhoneNumber = "+216" + toPhoneNumber;
            }

            if (toPhoneNumber.length() != 12 || !toPhoneNumber.matches("\\+216\\d{8}")) {
                System.err.println("Invalid phone number format for doctor: " + medecin.getNom());
                showErrorDialog("Erreur", "Format de numéro de téléphone invalide: " + toPhoneNumber + ". Attendu: +216 suivi de 8 chiffres.");
                return;
            }

            String messageBody = "Bonjour Dr. " + medecin.getNom() + ",\n" +
                    "Votre compte a été vérifié avec succès. Vous pouvez maintenant accéder à toutes les fonctionnalités de la plateforme.\n" +
                    "Merci,\nL'équipe d'administration";

            Message message = Message.creator(
                    new PhoneNumber("whatsapp:" + toPhoneNumber),
                    new PhoneNumber(TWILIO_WHATSAPP_NUMBER),
                    messageBody
            ).create();

            System.out.println("WhatsApp message sent successfully: " + message.getSid());
        } catch (Exception e) {
            System.err.println("Failed to send WhatsApp notification: " + e.getMessage());
            showErrorDialog("Erreur", "Échec de l'envoi de la notification WhatsApp: " + e.getMessage());
        }
    }
    @FXML
    private void exportToPDF() {
        //dialog elli yet7al bech na5tar le placement ta3 pf
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Users_List.pdf");
        File file = fileChooser.showSaveDialog(usersTable.getScene().getWindow());

        if (file == null) {
            return;
        }
        try {
            //initialization ta3 PDF with A4
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);

            document.setMargins(30, 30, 30, 30);

            Color primaryBlue = new DeviceRgb(59, 130, 246);
            Color accentPink = new DeviceRgb(244, 63, 94);
            Color teal = new DeviceRgb(20, 184, 166);
            Color amber = new DeviceRgb(245, 158, 11);
            Color lightGray = new DeviceRgb(245, 247, 250);
            Color darkText = new DeviceRgb(51, 65, 85);
            //header creation for the logo image and title
            Div headerDiv = new Div();
            headerDiv.setKeepTogether(true);
            headerDiv.setWidth(UnitValue.createPercentValue(100));
            Div logoDiv = new Div();
            logoDiv.setWidth(120);
            String logoPath = getClass().getResource("/images/logo.png").toExternalForm();
            Image logo = new Image(ImageDataFactory.create(logoPath));
            logo.setWidth(120);
            logo.setHeight(60);
            logoDiv.add(logo);
            headerDiv.add(logoDiv);
            Div titleDiv = new Div();
            titleDiv.setWidth(UnitValue.createPercentValue(100));
            titleDiv.setTextAlignment(TextAlignment.CENTER);
            Paragraph title = new Paragraph()
                    .add(new Text("Liste des Utilisateurs").setFontColor(primaryBlue).setBold())
                    .add(new Text(" - ChronoSerena").setFontColor(accentPink).setBold());
            title.setFontSize(20);
            title.setMarginTop(10);
            titleDiv.add(title);
            headerDiv.add(titleDiv);

            document.add(headerDiv);

            PdfCanvas canvas = new PdfCanvas(pdf.getFirstPage());
            PdfExtGState gs = new PdfExtGState().setFillOpacity(0.1f);
            canvas.saveState().setExtGState(gs);
            canvas.setFillColor(primaryBlue);
            canvas.rectangle(0, PageSize.A4.getHeight() - 100, PageSize.A4.getWidth(), 100);
            canvas.fill();
            canvas.restoreState();
            String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
            document.add(new Paragraph("Généré le : " + currentDate)
                    .setFontSize(12)
                    .setFontColor(darkText)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20)
                    .setMarginBottom(20));
            float[] columnWidths = {1, 1, 2, 1, 1, 1, 1};
            Table pdfTable = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();
            //header ta3 table
            String[] headers = {"Nom", "Prénom", "Email", "Téléphone", "Date de naissance", "Rôle", "Statut"};
            for (String header : headers) {
                pdfTable.addHeaderCell(new Cell()
                        .add(new Paragraph(header).setBold())
                        .setBackgroundColor(primaryBlue)
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBorder(Border.NO_BORDER)
                        .setPadding(8));
            }

            int rowIndex = 0;
            for (UserTableData user : usersTable.getItems()) {
                Color rowColor = (rowIndex % 2 == 0) ? lightGray : ColorConstants.WHITE;
                pdfTable.addCell(new Cell()
                        .add(new Paragraph(user.getNom()).setFontColor(darkText))
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setPadding(8));
                pdfTable.addCell(new Cell()
                        .add(new Paragraph(user.getPrenom()).setFontColor(darkText))
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setPadding(8));
                pdfTable.addCell(new Cell()
                        .add(new Paragraph(user.getEmail()).setFontColor(darkText))
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setPadding(8));
                pdfTable.addCell(new Cell()
                        .add(new Paragraph(user.getTelephone()).setFontColor(darkText))
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setPadding(8));
                pdfTable.addCell(new Cell()
                        .add(new Paragraph(user.getDateNaissance()).setFontColor(darkText))
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setPadding(8));
                pdfTable.addCell(new Cell()
                        .add(new Paragraph(user.getType()).setFontColor(darkText))
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setPadding(8));
                pdfTable.addCell(new Cell()
                        .add(new Paragraph(user.isBanned() ? "Banni" : "Actif")
                                .setFontColor(user.isBanned() ? accentPink : teal))
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setPadding(8));
                rowIndex++;
            }
            pdfTable.setBorder(Border.NO_BORDER);
            pdfTable.setBorderCollapse(com.itextpdf.layout.properties.BorderCollapsePropertyValue.SEPARATE);
            pdfTable.setHorizontalBorderSpacing(2);
            pdfTable.setVerticalBorderSpacing(2);
            canvas = new PdfCanvas(pdf.getFirstPage());
            canvas.setFillColor(lightGray);
            canvas.rectangle(28, 50, PageSize.A4.getWidth() - 56, PageSize.A4.getHeight() - 200);
            canvas.fill();
            document.add(pdfTable);
            Paragraph footer = new Paragraph()
                    .add(new Text("© 2025 ").setFontColor(darkText))
                    .add(new Text("ChronoSerena").setFontColor(primaryBlue).setBold())
                    .add(new Text(" - Application de gestion médicale").setFontColor(darkText));
            footer.setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20)
                    .setFixedPosition(30, 20, PageSize.A4.getWidth() - 60);
            document.add(footer);
            document.close();
            showInfoDialog("Succès", "Le fichier PDF a été exporté avec succès à : " + file.getAbsolutePath());
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }

        } catch (IOException e) {
            showErrorDialog("Erreur", "Échec de l'exportation en PDF : " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showErrorDialog("Erreur", "Erreur lors du chargement du logo : " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void navigateToReportDashboard(ActionEvent event) {
        SceneManager.loadScene("/fxml/AdminReportDashboard.fxml", event);
    }
    @FXML
    private void navigateToRegister(ActionEvent event) {
        SceneManager.loadScene("/fxml/AdminRegistration.fxml", event);
    }
    private void handleCommandeRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/back/showCommande.fxml"));
            Stage stage = (Stage) buttoncommande.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des produits: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void handleSuiviRedirect() {
        try {
            // Use SceneManager to load the scene and ensure it displays maximized
            SceneManager.loadScene("/fxml/liste_suivi_back.fxml", new ActionEvent(suivi, null));
        } catch (Exception e) {
            showErrorDialog("Erreur", "Impossible de charger la page des suivis: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void handleHistoriqueRedirect() {
        try {
            // Use SceneManager to load the scene and ensure it displays maximized
            SceneManager.loadScene("/fxml/liste_historique_back.fxml", new ActionEvent(historique, null));
        } catch (Exception e) {
            showErrorDialog("Erreur", "Impossible de charger la page des historiques: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void handleeventRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/listevent.fxml"));
            Stage stage = (Stage) tablesButton.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des produits: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void handlestatRedirect()  {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/statistique.fxml"));
            Stage stage = (Stage) tablesButton.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des produits: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void handlereservationRedirect()  {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/listreservation.fxml"));
            Stage stage = (Stage) tablesButton.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des produits: " + e.getMessage());
            e.printStackTrace();
        }

    }
}