module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires io.github.cdimascio.dotenv.java;
    requires itextpdf;
    requires javafx.swing;
    requires com.google.zxing;
    requires com.google.zxing.javase;


    opens com.example.demo to javafx.fxml;
    exports com.example.demo;
}