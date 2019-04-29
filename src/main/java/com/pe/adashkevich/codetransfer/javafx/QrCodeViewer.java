package com.pe.adashkevich.codetransfer.javafx;

import com.pe.adashkevich.codetransfer.QRCommandGenerator;
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

import java.io.File;

public class QrCodeViewer extends Application {

    private static String [] savedArgs;
    private ImageView imageView;

    @Override
    public void start(Stage stage) throws Exception{
        GridPane gridPane = FXMLLoader.load(getClass().getResource("/fxml/sample.fxml"));

        Scene scene = new Scene(gridPane, 500, 500, Color.BLACK);

        imageView = new ImageView(new Image(getClass().getResourceAsStream("/screenSaver.png")));

        Task<Image> task = new Task<Image>() {
            @Override
            protected Image call() {
                try {
                    QRCommandGenerator generator = new QRCommandGenerator();
                    if(savedArgs[0].endsWith("Plan.csv")) {
                        generator.processTransferPlan(savedArgs[0], this::updateValue);
                    } else {
                        generator.filesTransfer(new File(savedArgs[0]), this::updateValue);
                    }
                } catch (Exception e) {
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

    public static void main(String[] args) {
        savedArgs = args;
        launch(args);
    }
}
