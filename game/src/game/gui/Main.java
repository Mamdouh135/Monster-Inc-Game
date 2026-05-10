package game.gui;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("DoorDasH: Scare vs Laugh Touchdown");

        Label titleLabel = new Label("DOOR DASH");
        titleLabel.setFont(Font.font("Impact", FontWeight.BOLD, 48));
        
        Label subTitle = new Label("Select Your Side");
        subTitle.setFont(Font.font("Arial", FontWeight.NORMAL, 20));

        // Team Scarer Panel
        VBox scarerBox = createTeamBox("TEAM SCARER", "Power of Screams", "#2c3e50", "#e74c3c");
        Button btnScarer = new Button("PLAY AS SCARER");
        btnScarer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        scarerBox.getChildren().add(btnScarer);

        // Team Laugher Panel
        VBox laugherBox = createTeamBox("TEAM LAUGHER", "Power of Laughter", "#f39c12", "#27ae60");
        Button btnLaugher = new Button("PLAY AS LAUGHER");
        btnLaugher.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        laugherBox.getChildren().add(btnLaugher);

        HBox selectionLayout = new HBox(40, scarerBox, laugherBox);
        selectionLayout.setAlignment(Pos.CENTER);

        VBox rootBox = new VBox(30, titleLabel, subTitle, selectionLayout);
        rootBox.setAlignment(Pos.CENTER);
        rootBox.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 40;");

        BorderPane root = new BorderPane(rootBox);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Launch Game Actions
        btnScarer.setOnAction(e -> new GameWindow(primaryStage, "SCARER"));
        btnLaugher.setOnAction(e -> new GameWindow(primaryStage, "LAUGHER"));
    }

    private VBox createTeamBox(String teamName, String motto, String bgColor, String accentColor) {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 30; -fx-border-radius: 10; -fx-background-radius: 10; -fx-min-width: 300;");
        
        Label name = new Label(teamName);
        name.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        name.setTextFill(Color.WHITE);
        
        Label desc = new Label(motto);
        desc.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        desc.setTextFill(Color.web(accentColor));

        box.getChildren().addAll(name, desc);
        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }
}