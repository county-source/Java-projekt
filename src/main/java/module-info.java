module org.example.test1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.test1 to javafx.fxml;
    exports business;
}