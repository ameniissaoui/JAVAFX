package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.models.Admin;
import org.example.models.Medecin;
import org.example.models.Patient;
import org.example.services.AdminService;
import org.example.services.MedecinService;
import org.example.services.PatientService;

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
import javafx.scene.image.Image;

import javafx.scene.image.ImageView;
public class AdminDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private TableView<UserTableData> usersTable;
    @FXML private TableColumn<UserTableData, Integer> idColumn;
    @FXML private TableColumn<UserTableData, String> nomColumn;
    @FXML private TableColumn<UserTableData, String> prenomColumn;
    @FXML private TableColumn<UserTableData, String> emailColumn;
    @FXML private TableColumn<UserTableData, String> telephoneColumn;
    @FXML private TableColumn<UserTableData, String> typeColumn;
    @FXML private TableColumn<UserTableData, String> dateNaissanceColumn;
    @FXML private TableColumn<UserTableData, Void> actionsColumn;
    @FXML private Button tablesButton; // Add this field
    @FXML private Button eventButton; // Add this field

    @FXML private ComboBox<String> filterComboBox;
    @FXML private TextField searchField;
    @FXML private Button profileButton;
    @FXML private Button historique;
    @FXML private Button suivi;

    private Admin currentAdmin;
    private AdminService adminService = new AdminService();
    private MedecinService medecinService = new MedecinService();
    private PatientService patientService = new PatientService();
    @FXML private Button buttoncommande;

    private ObservableList<UserTableData> allUsers = FXCollections.observableArrayList();
    private ObservableList<UserTableData> filteredUsers = FXCollections.observableArrayList();

    public void setAdmin(Admin admin) {
        this.currentAdmin = admin;

        welcomeLabel.setText("Bienvenue, " + admin.getPrenom() + " " + admin.getNom() + "!");
        loadUsers();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTableColumns();
        setupFilterComboBox();
        setupSearchField();
        tablesButton.setOnAction(event -> handleTablesRedirect());
        eventButton.setOnAction(event -> handleeventRedirect());
        historique.setOnAction(event -> handleHistoriqueRedirect());
        suivi.setOnAction(event -> handleSuiviRedirect());
        buttoncommande.setOnAction(event -> handleCommandeRedirect());

        // Link the profile button to its handler
        profileButton.setOnAction(event -> handleProfileRedirect());
        refreshTable();

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
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/liste_suivi_back.fxml"));
            Stage stage = (Stage) suivi.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page des historiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleHistoriqueRedirect() {
        try {
            Parent tableRoot = FXMLLoader.load(getClass().getResource("/fxml/liste_historique_back.fxml"));
            Stage stage = (Stage) tablesButton.getScene().getWindow();
            Scene tableScene = new Scene(tableRoot);
            stage.setScene(tableScene);
            stage.show();
        } catch (IOException e) {
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

    private void setupTableColumns() {
        // Basic column setup with strict width control
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        dateNaissanceColumn.setCellValueFactory(new PropertyValueFactory<>("dateNaissance"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        // Force exact widths
        idColumn.setResizable(false);
        idColumn.setReorderable(false);

        // Set column alignment styles
        idColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        nomColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        prenomColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        emailColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        telephoneColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        dateNaissanceColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        typeColumn.setStyle("-fx-alignment: CENTER;");
        actionsColumn.setStyle("-fx-alignment: CENTER;");

        // ID column styling
        idColumn.setCellFactory(column -> new TableCell<UserTableData, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(item));
                    setAlignment(Pos.CENTER_LEFT);
                    getStyleClass().add("id-column");
                }
            }
        });

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
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox pane = new HBox(8, editButton, deleteButton);

            {
                // Setup edit button with pencil icon
                FontIcon editIcon = new FontIcon(BootstrapIcons.PENCIL_SQUARE);
                editIcon.setIconSize(16);
                editIcon.setIconColor(javafx.scene.paint.Color.WHITE);
                editButton.setGraphic(editIcon);
                editButton.getStyleClass().addAll("action-btn", "edit-btn");
                editButton.setTooltip(new Tooltip("Modifier"));

                // Setup delete button with trash icon
                FontIcon deleteIcon = new FontIcon(BootstrapIcons.TRASH);
                deleteIcon.setIconSize(16);
                deleteIcon.setIconColor(javafx.scene.paint.Color.WHITE);
                deleteButton.setGraphic(deleteIcon);
                deleteButton.getStyleClass().addAll("action-btn", "delete-btn");
                deleteButton.setTooltip(new Tooltip("Supprimer"));

                // Make buttons compact squares
                editButton.setPrefSize(32, 32);
                deleteButton.setPrefSize(32, 32);
                editButton.setMinSize(32, 32);
                deleteButton.setMinSize(32, 32);
                editButton.setMaxSize(32, 32);
                deleteButton.setMaxSize(32, 32);

                // Set perfect center alignment
                pane.setAlignment(Pos.CENTER);
                setAlignment(Pos.CENTER);

                // Button actions remain the same
                editButton.setOnAction(event -> {
                    UserTableData userData = getTableView().getItems().get(getIndex());
                    showInfoDialog("Édition temporairement désactivée",
                            "La fonctionnalité d'édition sera disponible dans une future mise à jour.\n" +
                                    "Détails de l'utilisateur:\n" +
                                    "ID: " + userData.getId() + "\n" +
                                    "Nom: " + userData.getNom() + "\n" +
                                    "Prénom: " + userData.getPrenom() + "\n" +
                                    "Email: " + userData.getEmail() + "\n" +
                                    "Type: " + userData.getType());
                });

                deleteButton.setOnAction(event -> {
                    UserTableData userData = getTableView().getItems().get(getIndex());
                    handleDeleteUser(userData);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
                setAlignment(Pos.CENTER); // Ensure buttons are centered
            }
        });
    }

    @FXML
    private void handleProfileRedirect() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_profile.fxml"));
            Parent profileRoot = loader.load();
            AdminProfileController profileController = loader.getController();
            profileController.setAdmin(currentAdmin);

            Stage stage = (Stage) profileButton.getScene().getWindow();
            Scene profileScene = new Scene(profileRoot);
            stage.setScene(profileScene);
            stage.show();
        } catch (IOException e) {
            showErrorDialog("Erreur", "Impossible de charger la page de profil: " + e.getMessage());
            e.printStackTrace();
        }
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
                    "Admin"
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
                    "Médecin"
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
                    "Patient"
            ));
        }

        applyFilters();
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(date);
    }

    private void applyFilters() {
        String filterType = filterComboBox.getValue();
        String searchText = searchField.getText().toLowerCase();

        filteredUsers.clear();

        for (UserTableData user : allUsers) {
            // Apply type filter
            if (!filterType.equals("Tous") && !user.getType().equals(filterType)) {
                continue;
            }

            // Apply search filter
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

    @FXML
    private void handleAddUser() {
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

    @FXML
    private void handleLogout() {
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
        } catch (IOException e) {
            showErrorDialog("Erreur", "Échec de la déconnexion: " + e.getMessage());
        }
    }

    @FXML
    private void refreshTable() {
        loadUsers();
    }

    // Inner class for table data
    public static class UserTableData {
        private final int id;
        private final String nom;
        private final String prenom;
        private final String email;
        private final String telephone;
        private final String dateNaissance;
        private final String type;

        public UserTableData(int id, String nom, String prenom, String email, String telephone,
                             String dateNaissance, String type) {
            this.id = id;
            this.nom = nom;
            this.prenom = prenom;
            this.email = email;
            this.telephone = telephone != null ? telephone : "";
            this.dateNaissance = dateNaissance;
            this.type = type;
        }

        public int getId() { return id; }
        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
        public String getEmail() { return email; }
        public String getTelephone() { return telephone; }
        public String getDateNaissance() { return dateNaissance; }
        public String getType() { return type; }
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

}