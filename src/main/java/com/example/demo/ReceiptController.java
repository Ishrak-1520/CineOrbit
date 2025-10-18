package com.example.demo;

import com.example.demo.utils.TicketGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

public class ReceiptController {

    @FXML private ImageView posterImageView;
    @FXML private Label titleLabel;
    @FXML private Label dateLabel;
    @FXML private Label timeLabel;
    @FXML private Label priceLabel;
    @FXML private Label hallLabel;
    @FXML private Label rowLabel;
    @FXML private Label seatLabel;
    @FXML private Label classLabel;
    @FXML private Label locationLabel;
    @FXML private ImageView qrImageView;

    public void setData(String title, String date, String time, String price,
                        String hall, String row, String seats, String classText, String location,
                        String posterPath, String qrPath) {
        titleLabel.setText(title);
        dateLabel.setText(date);
        timeLabel.setText(time);
        priceLabel.setText(price);
        hallLabel.setText(hall);
        rowLabel.setText(row);
        seatLabel.setText(seats);
        classLabel.setText(classText);
        locationLabel.setText(location);

        if (posterPath != null) {
            posterImageView.setImage(new Image("file:" + posterPath));
        }
        if (qrPath != null) {
            qrImageView.setImage(TicketGenerator.loadImage(qrPath));
        }
    }
}
