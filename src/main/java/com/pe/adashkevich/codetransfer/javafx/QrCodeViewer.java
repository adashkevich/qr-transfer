package com.pe.adashkevich.codetransfer.javafx;

import com.google.zxing.WriterException;
import com.pe.adashkevich.codetransfer.QRCodeGenerator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class QrCodeViewer extends Application {

    private static String [] savedArgs;
    //private final QrCode qrCode = new QrCode();
    private ImageView imageView;

    @Override
    public void start(Stage stage) throws Exception{
        GridPane gridPane = FXMLLoader.load(getClass().getResource("/fxml/sample.fxml"));

        Scene scene = new Scene(gridPane, 500, 500, Color.BLACK);

        imageView = new ImageView(new Image(getClass().getResourceAsStream("/screenSaver.png")));

        QRCodeGenerator generator = new QRCodeGenerator(this);
        Task<Image> task = new Task<Image>() {
            @Override
            protected Image call() {
                try {
                    generator.transferFileByQRCodes(Paths.get(savedArgs[0]), this::updateValue);
                } catch (IOException|InterruptedException|WriterException e) {
                    e.printStackTrace();
                } finally {
                    Platform.exit();
                }
                return null;
            }
        };

        imageView.setPreserveRatio(true);
        imageView.fitHeightProperty().bind(stage.heightProperty());
        imageView.fitWidthProperty().bind(stage.widthProperty());
        imageView.imageProperty().bind(task.valueProperty());

        gridPane.getChildren().add(imageView);

        stage.setScene(scene);
        stage.show();

        new Thread(task).start();
    }

    public void showQrCode(Path path) {
        System.out.println("QrCodeViewer.showQrCode()");
        try(InputStream is = Files.newInputStream(path)) {
            imageView.setImage(new Image(is));
            sleep();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sleep() {
        Platform.runLater(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("notify");
                notify();
            }
        });
    }

    public static void main(String[] args) {
        savedArgs = args;
        launch(args);
    }
}
