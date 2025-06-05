module com.example.bomberman {
    requires javafx.controls;
    requires javafx.fxml;
    
    exports BomberMan; // <-- Rends ton package accessible à JavaFX
    
    opens BomberMan to javafx.fxml; // <-- Permet à FXML d'accéder aux contrôleurs
}