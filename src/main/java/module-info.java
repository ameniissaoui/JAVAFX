module com.example.sante {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;

    opens com.example.sante to javafx.fxml;
    opens com.example.sante.controllers to javafx.fxml;
    opens com.example.sante.Entity to javafx.fxml,javafx.base;
    exports com.example.sante;

}