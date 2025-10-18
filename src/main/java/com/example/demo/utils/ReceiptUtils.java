package com.example.demo.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReceiptUtils {

    public static void showReceiptModal(Stage parentStage, String movieTitle, String genre, String date,
                                        int normalQty, int normalPrice, int specialQty, int specialPrice) {

        int total = (normalQty * normalPrice) + (specialQty * specialPrice);
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String receipt = "MOVIE TICKET RECEIPT\n\n" +
                "Movie Title: " + movieTitle + "\n" +
                "Genre: " + genre + "\n" +
                "Showing Date: " + date + "\n\n" +
                "Tickets:\n" +
                "Normal Class - Qty: " + normalQty + ", Price: INR " + normalPrice + ", Total: INR " + (normalQty * normalPrice) + "\n" +
                "Special Class - Qty: " + specialQty + ", Price: INR " + specialPrice + ", Total: INR " + (specialQty * specialPrice) + "\n\n" +
                "-------------------------------------------\n" +
                "TOTAL AMOUNT: INR " + total + "\n" +
                "DATE OF PURCHASE: " + dateTime;

        TextArea receiptArea = new TextArea(receipt);
        receiptArea.setEditable(false);
        receiptArea.setWrapText(true);

        Button exportPDFBtn = new Button("Export to PDF");
        exportPDFBtn.setOnAction(e -> exportToPDF(receipt));

        Button printBtn = new Button("Print Receipt");
        printBtn.setOnAction(e -> printReceipt(receiptArea));

        VBox vbox = new VBox(10, receiptArea, exportPDFBtn, printBtn);
        vbox.setStyle("-fx-padding: 15; -fx-background-color: white;");
        Scene scene = new Scene(vbox, 500, 400);

        Stage modal = new Stage();
        modal.setScene(scene);
        modal.initModality(Modality.WINDOW_MODAL);
        modal.initOwner(parentStage);
        modal.setTitle("Receipt");
        modal.show();
    }

    private static void exportToPDF(String receiptText) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Receipt PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("receipt.pdf");
        File file = fileChooser.showSaveDialog(null);
        if (file == null) return;

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
            document.add(new Paragraph(receiptText));
            document.close();
            showAlert("Success", "PDF exported successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to export PDF.");
        }
    }

    private static void printReceipt(TextArea textArea) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(textArea.getScene().getWindow())) {
            boolean printed = job.printPage(textArea);
            if (printed) {
                job.endJob();
                showAlert("Printed", "Receipt printed successfully.");
            }
        }
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
