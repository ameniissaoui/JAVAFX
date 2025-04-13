package Controllers;

import Model.historique_traitement;
import Model.suivie_medical;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AfficherSuiviController implements Initializable {

    @FXML
    private Label dateLabel;

    @FXML
    private TextArea commentaireTextArea;

    @FXML
    private Button fermerButton;

    private suivie_medical suiviMedical;
    private historique_traitement historiqueTraitement;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the controller
        System.out.println("AfficherSuiviController initialized");

        // Set up the fermer (close) button action
        fermerButton.setOnAction(event -> {
            System.out.println("Fermer button clicked");
            Stage stage = (Stage) fermerButton.getScene().getWindow();
            stage.close();
        });
    }

    public void setSuiviMedical(suivie_medical suiviMedical) {
        this.suiviMedical = suiviMedical;

        // Update UI with suivi_medical data
        if (suiviMedical != null) {
            System.out.println("Setting suiviMedical data: ID=" + suiviMedical.getId());
            dateLabel.setText(suiviMedical.getDate());
            commentaireTextArea.setText(suiviMedical.getCommentaire());
            commentaireTextArea.setEditable(false); // Make it read-only
        } else {
            System.err.println("Error: suiviMedical is null");
        }
    }

    public void setHistoriqueTraitement(historique_traitement historiqueTraitement) {
        this.historiqueTraitement = historiqueTraitement;
        // You can use historiqueTraitement data if needed in this view
    }
}