module com.example.dsafinals {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.ikonli.javafx;


    opens com.example.dsafinals to javafx.fxml;
    exports com.example.dsafinals;
    exports com.example.dsafinals.controllers;
    opens com.example.dsafinals.controllers to javafx.fxml;
}