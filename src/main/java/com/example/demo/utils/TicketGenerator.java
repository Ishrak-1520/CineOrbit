package com.example.demo.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class TicketGenerator {

    public static String generateBarcode(String text, String fileName) throws WriterException, IOException {
        String path = "barcodes/" + fileName + "_barcode.png";
        Path filePath = FileSystems.getDefault().getPath(path);
        Files.createDirectories(filePath.getParent());

        BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.CODE_128, 200, 80);

        // Create a transparent BufferedImage
        BufferedImage image = new BufferedImage(matrix.getWidth(), matrix.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < matrix.getWidth(); x++) {
            for (int y = 0; y < matrix.getHeight(); y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0x00000000); // black or transparent
            }
        }

        // Rotate 90 degrees clockwise
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage rotatedImage = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotatedImage.createGraphics();
        g2d.translate(height / 2.0, width / 2.0);
        g2d.rotate(Math.toRadians(90));
        g2d.translate(-width / 2.0, -height / 2.0);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        ImageIO.write(rotatedImage, "PNG", filePath.toFile());
        return filePath.toUri().toString();
    }




    public static String generateQRCode(String text, String fileName) throws WriterException, IOException {
        String path = "qrcodes/" + fileName + "_qrcode.png";
        Path filePath = FileSystems.getDefault().getPath(path);
        Files.createDirectories(filePath.getParent());

        BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 100, 100);

        // Create a transparent BufferedImage
        BufferedImage image = new BufferedImage(matrix.getWidth(), matrix.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < matrix.getWidth(); x++) {
            for (int y = 0; y < matrix.getHeight(); y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0x00000000); // black or transparent
            }
        }

        ImageIO.write(image, "PNG", filePath.toFile());
        return filePath.toUri().toString();
    }


    public static Image loadImage(String uriPath) {
        return new Image(uriPath);
    }
}
