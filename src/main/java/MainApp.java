import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Classe principale de l'application de gestion de rendez-vous
 * Point d'entrée qui initialise et lance l'interface utilisateur JavaFX
 */
public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Système de Gestion de Cabinet Médical");

        initRootLayout();
        showPlanningView(); // Affiche par défaut la vue des plannings

        // Configuration de la fenêtre principale
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.show();

        // Message de démarrage dans la console
        System.out.println("Application démarrée avec succès");
    }

    /**
     * Initialise le layout racine avec la barre de menu
     */
    private void initRootLayout() {
        try {
            rootLayout = new BorderPane();

            // Création de la barre de menu
            MenuBar menuBar = createMenuBar();

            // Création du contenu principal
            VBox topContainer = new VBox();
            topContainer.getChildren().add(menuBar);

            // Ajout de la barre de menu au layout racine
            rootLayout.setTop(topContainer);

            // Création de la scène
            Scene scene = new Scene(rootLayout);

            // Ajout du style CSS global si nécessaire
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

            primaryStage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'initialiser l'interface: " + e.getMessage());
        }
    }

    /**
     * Crée la barre de menu avec les options de navigation
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // Menu Gestion
        Menu menuGestion = new Menu("Gestion");

        MenuItem planningsItem = new MenuItem("Plannings du médecin");
        planningsItem.setOnAction(event -> showPlanningView());

        MenuItem rendezVousItem = new MenuItem("Rendez-vous");
        rendezVousItem.setOnAction(event -> showRendezVousView());

        menuGestion.getItems().addAll(planningsItem, rendezVousItem);

        // Menu Aide
        Menu menuAide = new Menu("Aide");

        MenuItem aboutItem = new MenuItem("À propos");
        aboutItem.setOnAction(event -> showAboutDialog());

        menuAide.getItems().add(aboutItem);

        // Ajout des menus à la barre
        menuBar.getMenus().addAll(menuGestion, menuAide);

        return menuBar;
    }

    /**
     * Affiche la vue de gestion des plannings
     */
    private void showPlanningView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/planning-view.fxml"));
            Parent planningView = loader.load();
            rootLayout.setCenter(planningView);
            primaryStage.setTitle("Gestion des Plannings");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de charger la vue des plannings: " + e.getMessage());
        }
    }

    /**
     * Affiche la vue de gestion des rendez-vous
     */
    private void showRendezVousView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/rendez-vous-view.fxml"));
            Parent rendezVousView = loader.load();
            rootLayout.setCenter(rendezVousView);
            primaryStage.setTitle("Gestion des Rendez-vous");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de charger la vue des rendez-vous: " + e.getMessage());
        }
    }

    /**
     * Affiche une boîte de dialogue À propos
     */
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("À propos");
        alert.setHeaderText("Système de Gestion de Cabinet Médical");
        alert.setContentText("Version 1.0\n\n" +
                "Cette application permet de gérer les plannings des médecins et les rendez-vous des patients.\n\n" +
                "© 2023 - Développé par [Votre Nom]");

        // Style pour la boîte de dialogue
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        alert.showAndWait();
    }

    /**
     * Affiche une boîte de dialogue d'erreur
     *
     * @param title Titre de la boîte de dialogue
     * @param message Message d'erreur à afficher
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style pour la boîte de dialogue d'erreur
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        alert.showAndWait();
    }

    /**
     * Méthode appelée à la fermeture de l'application
     */
    @Override
    public void stop() {
        // Libération des ressources à la fermeture
        System.out.println("Fermeture de l'application");
        try {
            // Fermer la connexion à la base de données si nécessaire
            service.DatabaseConnection.closeConnection();
        } catch (Exception e) {
            System.err.println("Erreur lors de la fermeture des ressources: " + e.getMessage());
        }
    }

    /**
     * Point d'entrée principal de l'application
     *
     * @param args Arguments de ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        launch(args);
    }
}