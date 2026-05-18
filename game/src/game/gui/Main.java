package game.gui;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

public class Main extends Application {

    private static final String BG_APP        = "#0a0a1a";
    private static final String BG_CARD       = "linear-gradient(to bottom right, #12082a, #1a1040)";
    private static final String PURPLE        = "#7c3aed";
    private static final String PURPLE_BORDER = "#7c3aed44";
    private static final String GOLD          = "#fbbf24";
    private static final String ORANGE        = "#f97316";
    private static final String GREEN         = "#10b981";
    private static final String CYAN          = "#67e8f9";
    private static final String TEXT_LIGHT    = "#f0ece0";
    private static final String TEXT_MUTED    = "#9ca3af";

    private StackPane rootNode;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("DoorDasH: Scare vs Laugh Touchdown");

        rootNode = new StackPane();
        rootNode.setStyle("-fx-background-color: radial-gradient(center 50% 10%, radius 80%, #1a1060 0%, " + BG_APP + " 60%);");

        BorderPane mainContent = new BorderPane();

        // --- 1. TITLE SECTION ---
        VBox titleBox = new VBox(5);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(60, 0, 40, 0));

        Label title = new Label("🚪 DooR DasH");
        title.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 54));
        title.setTextFill(Color.web(GOLD));
        title.setEffect(new DropShadow(15, Color.web(ORANGE, 0.7)));

        Label subtitle = new Label("SCARE VS LAUGH TOUCHDOWN");
        subtitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        subtitle.setTextFill(Color.web(TEXT_MUTED));
        subtitle.setStyle("-fx-letter-spacing: 4px;");

        titleBox.getChildren().addAll(title, subtitle);
        mainContent.setTop(titleBox);

        // --- 2. TEAM SELECTION CARDS ---
        HBox selectionBox = new HBox(60);
        selectionBox.setAlignment(Pos.CENTER);
        selectionBox.setPadding(new Insets(0, 50, 40, 50));

        VBox scarerCard = createTeamCard(
            "TEAM SCARER", 
            "Harness the dark energy of screams. Intimidate your foes and seize Boo's door.", 
            PURPLE, "team_scarer.png"
        );
        Button btnScarer = createPlayButton(PURPLE, "SELECT SCARER");
        btnScarer.setOnAction(e -> launchGameWithTransition(primaryStage, "SCARER"));
        scarerCard.getChildren().add(btnScarer);

        VBox laugherCard = createTeamCard(
            "TEAM LAUGHER", 
            "Harness the bright energy of joy. Outwit your opponents and claim Boo's door.", 
            GREEN, "team_laugher.png"
        );
        Button btnLaugher = createPlayButton(GREEN, "SELECT LAUGHER");
        btnLaugher.setOnAction(e -> launchGameWithTransition(primaryStage, "LAUGHER"));
        laugherCard.getChildren().add(btnLaugher);

        selectionBox.getChildren().addAll(scarerCard, laugherCard);
        mainContent.setCenter(selectionBox);

        // --- 3. BOTTOM INSTRUCTIONS BUTTON ---
        VBox bottomBox = new VBox();
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(0, 0, 50, 0));

        Button btnRules = new Button("📜 HOW TO PLAY");
        String rulesDef = "-fx-background-color:linear-gradient(to right,#082f49,#064e3b); -fx-text-fill:white; -fx-padding:12 40; -fx-border-radius:10; -fx-background-radius:10; -fx-border-color:" + CYAN + "; -fx-border-width:1.5; -fx-cursor:hand; -fx-font-size: 15px; -fx-font-weight: bold; -fx-font-family: 'Arial';";
        String rulesHov = rulesDef + "-fx-effect:dropshadow(three-pass-box," + CYAN + "88,14,0.5,0,0);";
        btnRules.setStyle(rulesDef);
        btnRules.setOnMouseEntered(e -> { btnRules.setStyle(rulesHov); });
        btnRules.setOnMouseExited(e -> btnRules.setStyle(rulesDef));
        btnRules.setOnAction(e -> showInstructions());

        Button btnSettings = new Button("⚙ SETTINGS");
        btnSettings.setStyle(rulesDef);
        btnSettings.setOnMouseEntered(e -> { btnSettings.setStyle(rulesHov); });
        btnSettings.setOnMouseExited(e -> btnSettings.setStyle(rulesDef));
        btnSettings.setOnAction(e -> showSettings());

        HBox bottomButtons = new HBox(20, btnRules, btnSettings);
        bottomButtons.setAlignment(Pos.CENTER);
        
        bottomBox.getChildren().add(bottomButtons);
        mainContent.setBottom(bottomBox);

        rootNode.getChildren().add(mainContent);

        // Entry Animation
        mainContent.setOpacity(0);
        mainContent.setTranslateY(20);
        FadeTransition ft = new FadeTransition(Duration.millis(800), mainContent);
        ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(800), mainContent);
        tt.setToY(0);
        ft.play(); tt.play();

        Scene scene = new Scene(rootNode, 1100, 720);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // ══════════════════════════════════════════════════════════════════════
    // LAUNCH GAME WITH FULLSCREEN TRANSITION
    // ══════════════════════════════════════════════════════════════════════
    private void launchGameWithTransition(Stage stage, String role) {
        playSound("start.mp3");

        // Step 1: Dark overlay fade-in
        Region fadeOverlay = new Region();
        fadeOverlay.setStyle("-fx-background-color: " + BG_APP + ";");
        fadeOverlay.setOpacity(0);
        rootNode.getChildren().add(fadeOverlay);

        // Animated loading label
        Label loadingLabel = new Label("ENTERING THE FLOOR...");
        loadingLabel.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 22));
        loadingLabel.setTextFill(Color.web(GOLD));
        loadingLabel.setEffect(new DropShadow(10, Color.web(ORANGE, 0.8)));
        loadingLabel.setOpacity(0);
        rootNode.getChildren().add(loadingLabel);

        // Step 2: Fade overlay in
        FadeTransition overlayFade = new FadeTransition(Duration.millis(500), fadeOverlay);
        overlayFade.setToValue(1.0);

        overlayFade.setOnFinished(e -> {
            // Step 3: Show loading text with fade
            FadeTransition lblFadeIn = new FadeTransition(Duration.millis(300), loadingLabel);
            lblFadeIn.setToValue(1.0);
            lblFadeIn.play();

            // Pulse the loading label
            Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO,       new KeyValue(loadingLabel.scaleXProperty(), 1.0),
                                                   new KeyValue(loadingLabel.scaleYProperty(), 1.0)),
                new KeyFrame(Duration.millis(400), new KeyValue(loadingLabel.scaleXProperty(), 1.08),
                                                   new KeyValue(loadingLabel.scaleYProperty(), 1.08)),
                new KeyFrame(Duration.millis(800), new KeyValue(loadingLabel.scaleXProperty(), 1.0),
                                                   new KeyValue(loadingLabel.scaleYProperty(), 1.0))
            );
            pulse.setCycleCount(2);

            // Step 4: After pulse, go fullscreen and launch
            pulse.setOnFinished(ev -> {
                // Animate to fullscreen
                stage.setFullScreen(true);
                stage.setFullScreenExitHint(""); // Remove default exit hint text

                // Add minimize/restore listener BEFORE switching scene
                stage.iconifiedProperty().addListener((obs, wasMinimized, isNowMinimized) -> {
                    if (isNowMinimized) {
                        showMinimizeWarning(stage);
                    }
                });

                // Short pause then switch scene
                Timeline switchDelay = new Timeline(
                    new KeyFrame(Duration.millis(250), evSwitch -> {
                        new GameWindow(stage, role);
                    })
                );
                switchDelay.play();
            });
            pulse.play();
        });

        overlayFade.play();
    }

    // ══════════════════════════════════════════════════════════════════════
    // MINIMIZE WARNING POPUP
    // ══════════════════════════════════════════════════════════════════════
    private void showMinimizeWarning(Stage ownerStage) {
        Stage pop = new Stage();
        pop.initModality(javafx.stage.Modality.NONE);
        pop.initStyle(StageStyle.TRANSPARENT);
        pop.setAlwaysOnTop(true);

        VBox layout = new VBox(16);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(34, 46, 34, 46));
        layout.setStyle(
            "-fx-background-color:linear-gradient(to bottom right,#1a0a2e,#0a0a0f);" +
            "-fx-border-color:" + GOLD + ";-fx-border-width:2;" +
            "-fx-border-radius:16;-fx-background-radius:16;" +
            "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.95),30,0.4,0,10);"
        );

        Label icon = new Label("🖥");
        icon.setFont(Font.font(44));

        Label head = new Label("RETURN TO FULL SCREEN");
        head.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 20));
        head.setTextFill(Color.web(GOLD));
        head.setEffect(new DropShadow(8, Color.web(GOLD, 0.6)));

        Label body = new Label(
            "For the best experience, this game is designed\n" +
            "to be played in full screen mode.\n\n" +
            "Please restore the window to continue."
        );
        body.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        body.setTextFill(Color.web(TEXT_LIGHT));
        body.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        body.setWrapText(true);
        body.setMaxWidth(300);

        Button btnRestore = new Button("↩  RESTORE FULL SCREEN");
        btnRestore.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 13));
        btnRestore.setStyle(
            "-fx-background-color:" + GOLD + ";-fx-text-fill:#1a0a00;" +
            "-fx-padding:11 28;-fx-background-radius:10;-fx-border-radius:10;-fx-cursor:hand;" +
            "-fx-effect:dropshadow(three-pass-box," + GOLD + "88,12,0.4,0,0);"
        );
        btnRestore.setPrefWidth(240);
        btnRestore.setOnAction(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(180), layout);
            ft.setToValue(0);
            ft.setOnFinished(ev -> {
                pop.close();
                ownerStage.setIconified(false);
                ownerStage.setFullScreen(true);
                ownerStage.toFront();
            });
            ft.play();
        });

        layout.getChildren().addAll(icon, head, body, btnRestore);

        StackPane overlay = new StackPane(layout);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.75); -fx-padding: 20;");

        layout.setOpacity(0);
        layout.setTranslateY(20);

        Scene sc = new Scene(overlay);
        sc.setFill(Color.TRANSPARENT);

        pop.setScene(sc);
        pop.show();

        // Centre the popup on the screen
        javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
        pop.setX((screen.getWidth()  - pop.getWidth())  / 2);
        pop.setY((screen.getHeight() - pop.getHeight()) / 2);

        // Animate in
        FadeTransition ft = new FadeTransition(Duration.millis(280), layout);
        ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(280), layout);
        tt.setToY(0);
        ft.play(); tt.play();
    }

    private void showInstructions() {
        Stage pop = new Stage();
        pop.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        pop.initStyle(StageStyle.TRANSPARENT);

        VBox layout = new VBox(15);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setPadding(new Insets(30, 40, 30, 40));
        layout.setStyle("-fx-background-color:linear-gradient(to bottom right,#1a0a2e,#0a0a0f);"
                      + "-fx-border-color:" + CYAN + ";-fx-border-width:2;"
                      + "-fx-border-radius:14;-fx-background-radius:14;"
                      + "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.9),25,0.3,0,8);");

        Label head = new Label("📜 HOW TO PLAY");
        head.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 22));
        head.setTextFill(Color.web(CYAN));
        head.setEffect(new DropShadow(8, Color.web(CYAN, 0.5)));

        VBox rulesBox = new VBox(10);
        rulesBox.setAlignment(Pos.TOP_LEFT);
        
        String[] rules = {
            "🎯 THE GOAL:",
            "Race against your opponent to reach Boo's Door (Cell 99) first.",
            "",
            "⚡ VITALITY (ENERGY):",
            "Energy is your lifeblood. Use it to pay Door Tributes or invoke Powerups.",
            "",
            "🗺️ THE BOARD:",
            "• Doors: You must have enough energy to pass, or you'll be stopped.",
            "• Conveyor Belts (⬆): Instantly launch you forward.",
            "• Contamination Socks (🧦): Drain your energy and slow you down.",
            "• Cards (🃏): Draw a random fate that can heal, harm, or teleport you.",
            "",
            "✨ POWERUPS:",
            "Once you amass 500 Energy, you can invoke your monster's unique Powerup to gain a massive advantage. Use it wisely!"
        };

        for (String line : rules) {
            Label l = new Label(line);
            if (line.endsWith(":")) {
                l.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 14));
                l.setTextFill(Color.web(GOLD));
                VBox.setMargin(l, new Insets(10, 0, 0, 0));
            } else {
                l.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
                l.setTextFill(Color.web(TEXT_LIGHT));
                l.setWrapText(true);
            }
            rulesBox.getChildren().add(l);
        }

        Button ok = new Button("UNDERSTOOD");
        ok.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        ok.setStyle("-fx-background-color:" + CYAN + "; -fx-text-fill:#0a0a1a; -fx-padding:10 30; -fx-background-radius:8; -fx-cursor:hand;");
        ok.setPrefWidth(180);
        VBox.setMargin(ok, new Insets(20, 0, 0, 0));
        ok.setOnAction(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(180), layout);
            ft.setToValue(0); ft.setOnFinished(ev -> pop.close()); ft.play();
        });

        layout.getChildren().addAll(head, rulesBox, ok);
        
        StackPane overlay = new StackPane(layout);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-padding: 20;");
        Scene sc = new Scene(overlay); sc.setFill(Color.TRANSPARENT);
        
        layout.setOpacity(0); layout.setTranslateY(24);
        FadeTransition ft = new FadeTransition(Duration.millis(260), layout); ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(260), layout); tt.setToY(0);
        ft.play(); tt.play();
        
        pop.setScene(sc); pop.showAndWait();
    }

    private void showSettings() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setPadding(new Insets(30, 40, 30, 40));
        layout.setMaxWidth(400); layout.setMaxHeight(300);
        layout.setStyle("-fx-background-color:linear-gradient(to bottom right,#1a0a2e,#0a0a0f);"
                      + "-fx-border-color:" + CYAN + ";-fx-border-width:2;"
                      + "-fx-border-radius:14;-fx-background-radius:14;"
                      + "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.9),25,0.3,0,8);");

        Label head = new Label("⚙ SOUND SETTINGS");
        head.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 22));
        head.setTextFill(Color.web(CYAN));
        head.setEffect(new DropShadow(8, Color.web(CYAN, 0.5)));

        Label volLabel = new Label("Volume: " + (int)(SoundManager.volume * 100) + "%");
        volLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        volLabel.setTextFill(Color.web(TEXT_LIGHT));

        Slider volSlider = new Slider(0, 100, SoundManager.volume * 100);
        volSlider.setShowTickMarks(true);
        volSlider.setShowTickLabels(true);
        volSlider.setMajorTickUnit(25);
        volSlider.setBlockIncrement(5);
        volSlider.setPrefWidth(250);
        
        volSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            SoundManager.volume = newVal.doubleValue() / 100.0;
            volLabel.setText("Volume: " + newVal.intValue() + "%");
        });

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-padding: 20;");

        Button ok = new Button("DONE");
        ok.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        ok.setStyle("-fx-background-color:" + CYAN + "; -fx-text-fill:#0a0a1a; -fx-padding:10 30; -fx-background-radius:8; -fx-cursor:hand;");
        ok.setPrefWidth(180);
        ok.setOnAction(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(180), layout);
            ft.setToValue(0); ft.setOnFinished(ev -> rootNode.getChildren().remove(overlay)); ft.play();
        });

        layout.getChildren().addAll(head, volLabel, volSlider, ok);
        overlay.getChildren().add(layout);
        
        layout.setOpacity(0); layout.setTranslateY(24);
        rootNode.getChildren().add(overlay);
        
        FadeTransition ft = new FadeTransition(Duration.millis(260), layout); ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(260), layout); tt.setToY(0);
        ft.play(); tt.play();
    }

    private void playSound(String filename) {
        // AudioClip does not support MP3; use MediaPlayer.
        // Auto-convert .wav references to .mp3 (all assets are MP3).
        String fname = filename.endsWith(".wav")
            ? filename.substring(0, filename.length() - 4) + ".mp3"
            : filename;
        try {
            File file = new File("assets/" + fname);
            if (!file.exists()) file = new File("assets/" + filename);
            if (file.exists()) {
                Media media = new Media(file.toURI().toString());
                MediaPlayer mp = new MediaPlayer(media);
                mp.setVolume(SoundManager.volume);
                mp.setOnEndOfMedia(mp::dispose);
                mp.play();
            }
        } catch (Exception e) {}
    }

    private ImageView loadIcon(String filename, int size) {
        try {
            File file = new File("assets/" + filename);
            if (file.exists()) {
                Image img = new Image(file.toURI().toString());
                ImageView view = new ImageView(img);
                view.setFitWidth(size);
                view.setFitHeight(size);
                view.setPreserveRatio(true);
                view.setEffect(new DropShadow(10, 2, 2, Color.web("#000000", 0.8)));
                return view;
            }
        } catch (Exception e) {}
        
        return null; 
    }

    private VBox createTeamCard(String teamName, String desc, String accentColor, String imgFile) {
        VBox card = new VBox(25);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(40, 30, 40, 30));
        card.setPrefWidth(350);
        card.setMaxWidth(350);
        
        String defaultStyle = "-fx-background-color: " + BG_CARD + "; " +
                              "-fx-border-color: " + PURPLE_BORDER + "; " +
                              "-fx-border-width: 2; " +
                              "-fx-border-radius: 16; " +
                              "-fx-background-radius: 16; " +
                              "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 15, 0, 0, 10);";
                              
        String hoverStyle = "-fx-background-color: " + BG_CARD + "; " +
                            "-fx-border-color: " + accentColor + "; " +
                            "-fx-border-width: 2; " +
                            "-fx-border-radius: 16; " +
                            "-fx-background-radius: 16; " +
                            "-fx-effect: dropshadow(three-pass-box, " + accentColor + "66, 25, 0.4, 0, 0);";

        card.setStyle(defaultStyle);

        ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
        card.setOnMouseEntered(e -> {
            card.setStyle(hoverStyle);
            st.setToX(1.03); st.setToY(1.03);
            st.playFromStart();
        });
        card.setOnMouseExited(e -> {
            card.setStyle(defaultStyle);
            st.setToX(1.0); st.setToY(1.0);
            st.playFromStart();
        });

        ImageView icon = loadIcon(imgFile, 150);
        StackPane iconPane = new StackPane();
        iconPane.setMinHeight(160);
        if (icon != null) {
            iconPane.getChildren().add(icon);
        } else {
            Label missing = new Label("👾");
            missing.setFont(Font.font(70));
            iconPane.getChildren().add(missing);
        }

        Label name = new Label(teamName);
        name.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 26));
        name.setTextFill(Color.web(TEXT_LIGHT));
        name.setEffect(new DropShadow(5, Color.web(accentColor, 0.6)));

        Label description = new Label(desc);
        description.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        description.setTextFill(Color.web(TEXT_MUTED));
        description.setWrapText(true);
        description.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        description.setMinHeight(60);

        card.getChildren().addAll(iconPane, name, description);
        return card;
    }

    private Button createPlayButton(String colorHex, String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btn.setMaxWidth(Double.MAX_VALUE);
        
        String defaultStyle = "-fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-padding: 12; -fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: derive(" + colorHex + ", 20%); -fx-text-fill: white; -fx-padding: 12; -fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, " + colorHex + "99, 15, 0.4, 0, 0);";
        
        btn.setStyle(defaultStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(defaultStyle));
        
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}