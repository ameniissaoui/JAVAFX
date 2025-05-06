package org.example.controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfDocument;
import javafx.stage.Screen;
import org.example.models.Medecin;
import org.example.models.Patient;
import org.example.models.Reservation;
import org.example.services.ReservationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import org.example.util.SessionManager;

import static org.example.util.NotificationUtil.showAlert;

public class reservationFrontController {
    @FXML
    private TextField searchField;

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private Button EventButton;

    @FXML
    private Button ReservationButton;

    @FXML
    private Button historique;

    @FXML
    private Button demandeButton;

    @FXML
    private Button rendezVousButton;

    private final ReservationService reservationService = new ReservationService();
    private ObservableList<Reservation> reservationsList;

    @FXML
    void eventfront(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/eventFront.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void reservationfront(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reservationFront.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        // Chargement des données
        loadReservations();

        // Configuration de la recherche dynamique
        setupDynamicSearch();

        // Configuration des boutons de navigation
        if (EventButton != null) {
            EventButton.setOnAction(this::eventfront);
        }

        if (ReservationButton != null) {
            ReservationButton.setOnAction(this::reservationfront);
        }
    }

    private void openReservationForm(Reservation reservation, boolean isEditMode) {
        try {
            // Charger le FXML de la fenêtre d'ajout/modification
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/updateReservation.fxml"));
            Parent root = loader.load();

            // Passer la réservation et l'état (ajout ou modification)
            reservationController controller = loader.getController();
            controller.initialize(reservation, isEditMode);

            // Récupérer la fenêtre (stage) actuelle et mettre à jour la scène
            Scene scene = new Scene(root, 1200, 700);
            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadReservations() {
        // Retrieve the current user from SessionManager
        SessionManager session = SessionManager.getInstance();
        Object currentUser = session.getCurrentUser();
        int currentuser = -1; // Initialisation à une valeur par défaut

        if (currentUser instanceof Patient) {
            currentuser = ((Patient) currentUser).getId();
        } else if (currentUser instanceof Medecin) {
            currentuser = ((Medecin) currentUser).getId();
        } else {
            return;
        }

        List<Reservation> reservations = reservationService.getReservationByUser(currentuser);
        reservationsList = FXCollections.observableArrayList(reservations);
        displayReservationsAsCards(reservationsList);
    }

    private void setupDynamicSearch() {
        // Ajouter un listener au texte du champ de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterReservations(newValue);
        });
    }

    private void filterReservations(String keyword) {
        keyword = keyword.toLowerCase().trim();
        if (keyword.isEmpty()) {
            displayReservationsAsCards(reservationsList);
        } else {
            ObservableList<Reservation> filteredList = FXCollections.observableArrayList();
            for (Reservation res : reservationsList) {
                if (res.getNomreserv().toLowerCase().contains(keyword) ||
                        res.getMail().toLowerCase().contains(keyword)) {
                    filteredList.add(res);
                }
            }
            displayReservationsAsCards(filteredList);
        }
    }

    private void displayReservationsAsCards(ObservableList<Reservation> reservations) {
        cardsContainer.getChildren().clear();
        for (Reservation reservation : reservations) {
            cardsContainer.getChildren().add(createReservationCard(reservation));
        }
    }

    private VBox createReservationCard(Reservation reservation) {
        // Création de la carte principale
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 10); " +
                "-fx-padding: 15;");
        card.setPrefWidth(300);
        card.setSpacing(10);

        // Titre de la réservation (nom) avec une taille plus grande
        Label nameLabel = new Label(reservation.getNomreserv());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-text-fill: #43ACD1;");

        // Section des informations détaillées
        VBox infoSection = new VBox(8);
        infoSection.setStyle("-fx-padding: 5 0 5 0;");

        // Email avec icône - texte en bleu
        HBox emailContainer = new HBox(10);
        ImageView emailIcon = new ImageView(new Image("file:src/main/resources/images/email.png"));
        emailIcon.setFitHeight(16);
        emailIcon.setFitWidth(16);
        Label emailLabel = new Label(reservation.getMail());
        emailLabel.setWrapText(true);
        emailLabel.setStyle("-fx-text-fill: #43ACD1;");  // Même couleur bleue que le nom
        emailContainer.getChildren().addAll(emailIcon, emailLabel);

        // Nombre de personnes avec icône - texte en bleu
        HBox personnesContainer = new HBox(10);
        ImageView personnesIcon = new ImageView(new Image("file:src/main/resources/images/users.png"));
        personnesIcon.setFitHeight(16);
        personnesIcon.setFitWidth(16);
        Label personnesLabel = new Label(String.valueOf(reservation.getNbrpersonne()) + " personne(s)");
        personnesLabel.setStyle("-fx-text-fill: #43ACD1;");  // Même couleur bleue que le nom
        personnesContainer.getChildren().addAll(personnesIcon, personnesLabel);

        // Ajout des informations à la section
        infoSection.getChildren().addAll(emailContainer, personnesContainer);

        // Séparateur
        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 5, 0));

        // Section des boutons d'action
        HBox actionButtonsContainer = new HBox(10);
        actionButtonsContainer.setAlignment(Pos.CENTER);

        // Bouton Modifier
        Button btnModifier = new Button("Modifier");
        btnModifier.setStyle("-fx-background-color: #43ACD1; -fx-text-fill: white; -fx-background-radius: 5;");
        ImageView editIcon = new ImageView(new Image("file:src/main/resources/images/modifier.png"));
        editIcon.setFitWidth(16);
        editIcon.setFitHeight(16);
        btnModifier.setGraphic(editIcon);
        btnModifier.setContentDisplay(ContentDisplay.LEFT);
        btnModifier.setOnAction(event -> openReservationForm(reservation, true));

        // Bouton Supprimer
        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-background-radius: 5;");
        ImageView deleteIcon = new ImageView(new Image("file:src/main/resources/images/deleteM.png"));
        deleteIcon.setFitWidth(16);
        deleteIcon.setFitHeight(16);
        btnSupprimer.setGraphic(deleteIcon);
        btnSupprimer.setContentDisplay(ContentDisplay.LEFT);
        btnSupprimer.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Êtes-vous sûr de vouloir supprimer cette réservation ?");
            alert.setContentText("Cette action est irréversible.");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    reservationService.supprimer(reservation);
                    cardsContainer.getChildren().remove(card);
                    reservationsList.remove(reservation);
                    System.out.println("Supprimé : " + reservation);
                }
            });
        });

        // Bouton PDF
        Button btnPdf = new Button("PDF");
        btnPdf.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 5;");
        ImageView pdfIcon = new ImageView(new Image("file:src/main/resources/images/pdf.png"));
        pdfIcon.setFitWidth(16);
        pdfIcon.setFitHeight(16);
        btnPdf.setGraphic(pdfIcon);
        btnPdf.setContentDisplay(ContentDisplay.LEFT);
        btnPdf.setOnAction(event -> generatePdf(reservation));

        // Nouveau Bouton QR Code
        Button btnQrCode = new Button("QR Code");
        btnQrCode.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-background-radius: 5;");
        ImageView qrIcon = new ImageView(new Image("file:src/main/resources/images/qrcode.png"));
        qrIcon.setFitWidth(16);
        qrIcon.setFitHeight(16);
        btnQrCode.setGraphic(qrIcon);
        btnQrCode.setContentDisplay(ContentDisplay.LEFT);
        btnQrCode.setOnAction(event -> showQrCodePopup(reservation));

        actionButtonsContainer.getChildren().addAll(btnModifier, btnSupprimer, btnPdf, btnQrCode);

        // Assembler tous les éléments dans la carte
        card.getChildren().addAll(
                nameLabel,
                infoSection,
                separator,
                actionButtonsContainer
        );

        return card;
    }

    private void showQrCodePopup(Reservation reservation) {
        try {
            // Créer le contenu du QR code
            String qrContent = "Réservation: " + reservation.getNomreserv()
                    + " | Email: " + reservation.getMail()
                    + " | Nombre: " + reservation.getNbrpersonne();

            // Générer le QR code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 300, 300);

            // Convertir le QR code en JavaFX Image
            javafx.scene.image.WritableImage qrImage = new javafx.scene.image.WritableImage(300, 300);
            for (int x = 0; x < 300; x++) {
                for (int y = 0; y < 300; y++) {
                    qrImage.getPixelWriter().setColor(x, y,
                            bitMatrix.get(x, y) ? javafx.scene.paint.Color.BLACK : javafx.scene.paint.Color.WHITE);
                }
            }

            // Créer une vue pour l'image
            ImageView qrImageView = new ImageView(qrImage);

            // Créer un titre pour la fenêtre popup
            Label titleLabel = new Label("QR Code pour " + reservation.getNomreserv());
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10px;");

            // Ajouter les informations de la réservation
            Label infoLabel = new Label("Email: " + reservation.getMail() + "\nNombre de personnes: " + reservation.getNbrpersonne());
            infoLabel.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");

            // Créer le contenu du popup
            VBox popupContent = new VBox(15);
            popupContent.setAlignment(Pos.CENTER);
            popupContent.setStyle("-fx-background-color: white; -fx-padding: 20px;");
            popupContent.getChildren().addAll(titleLabel, qrImageView, infoLabel);

            // Créer et configurer la fenêtre popup
            Stage popupStage = new Stage();
            popupStage.setTitle("QR Code - " + reservation.getNomreserv());
            popupStage.setScene(new Scene(popupContent, 350, 450));
            popupStage.initModality(Modality.APPLICATION_MODAL); // Rend la fenêtre modale
            popupStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la génération du QR code.", ButtonType.OK);
            alert.show();
        }
    }

    private String getInitials(String name) {
        StringBuilder initials = new StringBuilder();
        boolean isFirstChar = true;
        for (char c : name.toCharArray()) {
            if (isFirstChar && Character.isLetter(c)) {
                initials.append(Character.toUpperCase(c));
                isFirstChar = false;
            } else if (c == ' ' && name.length() > initials.length()) {
                isFirstChar = true;
            }

            if (initials.length() >= 2) break;
        }

        // Si un seul caractère a été trouvé, utiliser le premier caractère deux fois
        if (initials.length() == 1) {
            initials.append(initials.charAt(0));
        }

        return initials.toString();
    }

    private void generatePdf(Reservation reservation) {
        try {
            // 1. Création du document PDF avec marges personnalisées
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            String filename = "Reservation_" + reservation.getNomreserv() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            // 2. Styles de base - Fix Font references by using fully qualified names
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 20, com.itextpdf.text.Font.BOLD, BaseColor.BLUE);
            com.itextpdf.text.Font sectionFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.NORMAL, BaseColor.DARK_GRAY);
            com.itextpdf.text.Font boldFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);

            // 3. Titre
            Paragraph title = new Paragraph("Confirmation de Réservation", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 4. Bloc de contenu stylisé
            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);
            PdfPCell cell = new PdfPCell();
            cell.setPadding(20);
            cell.setBackgroundColor(new BaseColor(244, 247, 252)); // couleur #f4f7fc
            cell.setBorder(Rectangle.NO_BORDER);

            Paragraph content = new Paragraph();
            content.add(new Phrase("Nom : ", boldFont));
            content.add(new Phrase(reservation.getNomreserv() + "\n", sectionFont));

            content.add(new Phrase("Email : ", boldFont));
            content.add(new Phrase(reservation.getMail() + "\n", sectionFont));

            content.add(new Phrase("Nombre de personnes : ", boldFont));
            content.add(new Phrase(String.valueOf(reservation.getNbrpersonne()) + "\n", sectionFont));

            cell.addElement(content);
            table.addCell(cell);
            document.add(table);

            // 5. Espace avant QR
            document.add(new Paragraph("\n"));

            // 6. QR Code
            String qrContent = "Réservation: " + reservation.getNomreserv()
                    + " | Email: " + reservation.getMail()
                    + " | Nombre: " + reservation.getNbrpersonne();

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 150, 150);
            ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOut);
            com.itextpdf.text.Image qrImage = com.itextpdf.text.Image.getInstance(pngOut.toByteArray());
            qrImage.setAlignment(Element.ALIGN_CENTER);
            document.add(qrImage);

            Paragraph qrCaption = new Paragraph("Scannez ce code pour vérifier votre réservation", sectionFont);
            qrCaption.setAlignment(Element.ALIGN_CENTER);
            document.add(qrCaption);

            // 7. Message de remerciement
            Paragraph thankYou = new Paragraph("\nMerci pour votre confiance !", boldFont);
            thankYou.setAlignment(Element.ALIGN_CENTER);
            thankYou.setSpacingBefore(30);

            Paragraph message = new Paragraph("Nous sommes ravis de vous avoir parmi nos clients. À bientôt pour votre prochaine réservation !", sectionFont);
            message.setAlignment(Element.ALIGN_CENTER);
            message.setSpacingAfter(20);

            document.add(thankYou);
            document.add(message);

            // 8. Clôture
            document.close();

            // 9. Alerte de succès
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "PDF généré : " + new File(filename).getAbsolutePath(), ButtonType.OK);
            alert.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la génération du PDF.", ButtonType.OK);
            alert.show();
        }
    }
    // Navigation methods from SuccessController
    private void maximizeStage(Stage stage) {
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();
        stage.setMaximized(true);
        stage.setWidth(screenWidth);
        stage.setHeight(screenHeight);
    }
    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void navigate(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            showError("Erreur de navigation", e.getMessage());
        }
    }
    @FXML
    void navigateToProfile(ActionEvent event) {
        try {
            SessionManager session = SessionManager.getInstance();
            String userType = session.getUserType();
            String fxmlPath;

            switch (userType) {
                case "admin":
                    fxmlPath = "/fxml/AdminDashboard.fxml";
                    break;
                case "medecin":
                    fxmlPath = "/fxml/medecin_profile.fxml";
                    break;
                case "patient":
                    fxmlPath = "/fxml/patient_profile.fxml";
                    break;
                default:
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Type utilisateur inconnu",
                            "Impossible de rediriger vers la page profil.");
                    return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mon Profil");
            stage.show();

        } catch (Exception e) {
            System.err.println("Erreur lors de la navigation vers le profil : " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible d'ouvrir le profil", e.getMessage());
        }
    }
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    public void redirectToCalendar(ActionEvent event) {
        try {
            // Check login status before navigation
            if (!checkLoginForNavigation()) return;
            SceneManager.loadScene("/fxml/patient_calendar.fxml", event);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de navigation");
            alert.setContentText("Impossible de charger le calendrier: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private boolean checkLoginForNavigation() {
        if (!sessionManager.isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Accès refusé", "Non connecté",
                    "Vous devez être connecté pour accéder à cette fonctionnalité.");
            return false;
        }
        return true;
    }



    // NAVIGATION METHODS
    @FXML void navigateToHome(ActionEvent event) { navigate("/fxml/front/home.fxml", event); }
    @FXML void navigateToEvent(ActionEvent event) { navigate("/fxml/eventFront.fxml", event); }

    @FXML void navigateToHistoriques(ActionEvent event) { navigate("/fxml/front/historiques.fxml", event); }
    @FXML void redirectToDemande(ActionEvent event) { navigate("/fxml/DemandeDashboard.fxml", event); }
    @FXML void redirectToRendezVous(ActionEvent event) { navigate("/fxml/rendez-vous-view.fxml", event); }
    @FXML void redirectProduit(ActionEvent event) { navigate("/fxml/front/showProduit.fxml", event); }
    @FXML void viewDoctors(ActionEvent event) { navigate("/fxml/DoctorList.fxml", event); }


}