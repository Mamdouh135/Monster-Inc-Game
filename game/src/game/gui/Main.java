package game.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("DoorDasH: Scare vs Laugh Touchdown");

        BorderPane root = new BorderPane();
        // Deep blue gradient matching the game board background
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a2a42, #0d1522);");

        // --- 1. TITLE SECTION ---
        VBox titleBox = new VBox(5);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(50, 0, 40, 0));

        Label title = new Label("DOORDASH");
        title.setFont(Font.font("Impact", FontWeight.BOLD, 72));
        title.setTextFill(Color.web("#f1c40f"));
        
        // Add a nice dark drop shadow to make the title pop
        DropShadow dropShadow = new DropShadow(10, Color.BLACK);
        title.setEffect(dropShadow);

        Label subtitle = new Label("SCARE VS LAUGH TOUCHDOWN");
        subtitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        subtitle.setTextFill(Color.WHITE);

        titleBox.getChildren().addAll(title, subtitle);
        root.setTop(titleBox);

        // --- 2. TEAM SELECTION CARDS ---
        HBox selectionBox = new HBox(60);
        selectionBox.setAlignment(Pos.CENTER);
        selectionBox.setPadding(new Insets(0, 50, 80, 50));

        // Create the Purple Scarer Card
        VBox scarerCard = createTeamCard(
            "TEAM SCARER", 
            "Harness the dark energy of screams. Intimidate your opponents and race to Boo's door.", 
            "#8e44ad", "#9b59b6", "scarer"
        );
        Button btnScarer = createPlayButton("#8e44ad");
        btnScarer.setOnAction(e -> launchGame(primaryStage, "SCARER"));
        scarerCard.getChildren().add(btnScarer);

        // Create the Green Laugher Card
        VBox laugherCard = createTeamCard(
            "TEAM LAUGHER", 
            "Harness the bright energy of joy. Outsmart your opponents and race to Boo's door.", 
            "#2ecc71", "#27ae60", "laugher"
        );
        Button btnLaugher = createPlayButton("#27ae60");
        btnLaugher.setOnAction(e -> launchGame(primaryStage, "LAUGHER"));
        laugherCard.getChildren().add(btnLaugher);

        selectionBox.getChildren().addAll(scarerCard, laugherCard);
        root.setCenter(selectionBox);

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void launchGame(Stage stage, String role) {
        // Launches your highly styled GameWindow
        new GameWindow(stage, role);
    }

    // --- UI FACTORY METHODS ---

    private VBox createTeamCard(String teamName, String desc, String primaryColor, String secondaryColor, String iconType) {
        VBox card = new VBox(25);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(40, 30, 40, 30));
        card.setPrefWidth(350);
        card.setMaxWidth(350);
        
        // Base Styling
        String defaultStyle = "-fx-background-color: #24344d; " +
                              "-fx-border-color: " + primaryColor + "; " +
                              "-fx-border-width: 4; " +
                              "-fx-border-radius: 15; " +
                              "-fx-background-radius: 15; " +
                              "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 15, 0, 0, 10);";
                              
        // Glowing Hover Styling
        String hoverStyle = "-fx-background-color: #2c3e50; " +
                            "-fx-border-color: " + secondaryColor + "; " +
                            "-fx-border-width: 4; " +
                            "-fx-border-radius: 15; " +
                            "-fx-background-radius: 15; " +
                            "-fx-effect: dropshadow(three-pass-box, " + primaryColor + ", 25, 0.4, 0, 0);";

        card.setStyle(defaultStyle);

        // Add interactive hover glow animations
        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(defaultStyle));

        // Use native graphics to draw giant high-res monsters
        StackPane icon = createIcon(iconType, 130);

        Label name = new Label(teamName);
        name.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 28));
        name.setTextFill(Color.web(primaryColor));

        Label description = new Label(desc);
        description.setFont(Font.font("Arial", 15));
        description.setTextFill(Color.web("#bdc3c7"));
        description.setWrapText(true);
        description.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        description.setMinHeight(70);

        card.getChildren().addAll(icon, name, description);
        return card;
    }

    private Button createPlayButton(String color) {
        Button btn = new Button("SELECT TEAM");
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        btn.setMaxWidth(Double.MAX_VALUE);
        
        String defaultStyle = "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 15; -fx-background-radius: 8;";
        String hoverStyle = "-fx-background-color: derive(" + color + ", 20%); -fx-text-fill: white; -fx-padding: 15; -fx-background-radius: 8;";
        
        btn.setStyle(defaultStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(defaultStyle));
        
        return btn;
    }

    // Native Graphics Drawer to build the monsters purely out of JavaFX shapes
    private StackPane createIcon(String type, int size) {
        StackPane iconPane = new StackPane();
        iconPane.setMinSize(size, size);
        iconPane.setMaxSize(size, size);

        Color baseColor = type.equals("laugher") ? Color.web("#2ecc71") : Color.web("#9b59b6");
        
        // Body
        javafx.scene.shape.Circle body = new javafx.scene.shape.Circle(size / 2.0, baseColor);
        body.setStroke(Color.WHITE); 
        body.setStrokeWidth(size * 0.04);
        
        // Big Cyclops Eye
        javafx.scene.shape.Circle eye = new javafx.scene.shape.Circle(size / 3.5, Color.WHITE);
        javafx.scene.shape.Circle pupil = new javafx.scene.shape.Circle(size / 8.0, Color.web("#111111"));
        
        // Adjust eye position slightly upward
        StackPane.setMargin(eye, new Insets(0, 0, size / 5.0, 0));
        StackPane.setMargin(pupil, new Insets(0, 0, size / 5.0, 0));
        
        iconPane.getChildren().addAll(body, eye, pupil);
        return iconPane;
    }

    public static void main(String[] args) {
        launch(args);
    }
}