package game.gui;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.media.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.Duration;

import java.io.File;
import java.util.*;

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
    // FIX: Hold strong references so GC doesn't kill MediaPlayers mid-playback
    private final List<MediaPlayer> activePlayers = new ArrayList<>();
    private MediaPlayer bgMusic;
    // Volume settings (shared with GameWindow via static access)
    public static double musicVolume   = 0.35;
    public static double effectsVolume = 0.70;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("DooR DasH: Scare vs Laugh Touchdown");

        rootNode = new StackPane();
        rootNode.setStyle("-fx-background-color: radial-gradient(center 50% 10%, radius 80%, #1a1060 0%, " + BG_APP + " 60%);");

        // --- LAYER 0: Animated Particle Background ---
        Pane particles = buildParticleLayer();
        rootNode.getChildren().add(particles);

        // --- LAYER 1: Main Content ---
        BorderPane mainContent = new BorderPane();

        // 1. TITLE
        VBox titleBox = new VBox(8);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(52, 0, 24, 0));

        Label title = new Label("DooR DasH");
        ImageView titleIcon = loadIcon("boo.png", 52);
        if (titleIcon != null) { titleIcon.setEffect(new DropShadow(8, Color.web(ORANGE, 0.7))); title.setGraphic(titleIcon); }
        title.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 58));
        title.setTextFill(Color.web(GOLD));
        DropShadow glow = new DropShadow(15, Color.web(ORANGE, 0.7));
        title.setEffect(glow);
        animateBreathingGlow(glow);

        Label subtitle = new Label("S C A R E   *   V S   *   L A U G H   T O U C H D O W N");
        subtitle.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        subtitle.setTextFill(Color.web(TEXT_MUTED));
        FadeTransition subtitlePulse = new FadeTransition(Duration.millis(2500), subtitle);
        subtitlePulse.setFromValue(0.45); subtitlePulse.setToValue(1.0);
        subtitlePulse.setAutoReverse(true); subtitlePulse.setCycleCount(Animation.INDEFINITE);
        subtitlePulse.play();

        titleBox.getChildren().addAll(title, subtitle);
        mainContent.setTop(titleBox);

        // 2. TEAM CARDS
        HBox selectionBox = new HBox(60);
        selectionBox.setAlignment(Pos.CENTER);
        selectionBox.setPadding(new Insets(0, 50, 16, 50));

        String[] scarerRoster = {"Sulley - Dynamo - Freezes opponent", "Randall - Schemer - Steals energy", "Waternoose - Schemer - Chain steal", "Roz - MultiTasker - +200 E bonus"};
        VBox scarerCard = createTeamCard("TEAM SCARER",
            "Harness the dark energy of screams.\nIntimidate your foes and seize Boo's door.",
            PURPLE, "team_scarer.png", scarerRoster);
        Button btnScarer = createPlayButton(PURPLE, ">> SELECT SCARER");
        btnScarer.setOnAction(e -> launchGameWithTransition(primaryStage, "SCARER"));
        scarerCard.getChildren().add(btnScarer);

        String[] laugherRoster = {"Mike - Dasher - 2x movement speed", "Celia - MultiTasker - +200 E bonus", "Fungus - Dasher - Momentum Rush", "Yeti - Dynamo - Doubles all gains"};
        VBox laugherCard = createTeamCard("TEAM LAUGHER",
            "Harness the bright energy of joy.\nOutwit your opponents and claim Boo's door.",
            GREEN, "team_laugher.png", laugherRoster);
        Button btnLaugher = createPlayButton(GREEN, ">> SELECT LAUGHER");
        btnLaugher.setOnAction(e -> launchGameWithTransition(primaryStage, "LAUGHER"));
        laugherCard.getChildren().add(btnLaugher);

        selectionBox.getChildren().addAll(scarerCard, laugherCard);
        mainContent.setCenter(selectionBox);

        VBox bottomBox = buildBottomBar(primaryStage);
        mainContent.setBottom(bottomBox);

        rootNode.getChildren().add(mainContent);

        // Start hidden — intro will reveal it
        mainContent.setOpacity(0);
        mainContent.setTranslateY(20);

        Scene scene = new Scene(rootNode, 1100, 720);

        // Keyboard shortcuts
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case DIGIT1: case NUMPAD1: launchGameWithTransition(primaryStage, "SCARER");  break;
                case DIGIT2: case NUMPAD2: launchGameWithTransition(primaryStage, "LAUGHER"); break;
                case F1:  showInstructions(); break;
                case F11:
                    boolean fs = !primaryStage.isFullScreen();
                    primaryStage.setFullScreen(fs);
                    primaryStage.setFullScreenExitHint("");
                    break;
                default: break;
            }
        });

        // When restored from minimise, return to full-screen if it was active
        primaryStage.iconifiedProperty().addListener((obs, wasMin, isNowMin) -> {
            if (!isNowMin && primaryStage.isFullScreen()) {
                primaryStage.setFullScreen(true);
                primaryStage.setFullScreenExitHint("");
            }
        });

        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(650);
        primaryStage.setScene(scene);
        primaryStage.show();
        // Play intro, then slide in main menu + start music
        showIntro(rootNode, () -> {
            FadeTransition ft = new FadeTransition(Duration.millis(800), mainContent);
            ft.setToValue(1);
            TranslateTransition tt = new TranslateTransition(Duration.millis(800), mainContent);
            tt.setToY(0);
            ft.play(); tt.play();
            startMenuMusic();
        });
    }

    // ======================================================================
    // BOTTOM ACTION BAR (extracted to reduce start() local variable count)
    // ======================================================================
    private VBox buildBottomBar(Stage primaryStage) {
        VBox bottomBox = new VBox(10);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(0, 0, 32, 0));

        // ── HOW TO PLAY button ──
        Button btnRules = new Button("HOW TO PLAY");
        String rulesDef =
            "-fx-background-color:linear-gradient(to right,#082f49,#064e3b);" +
            "-fx-text-fill:" + CYAN + ";" +
            "-fx-padding:13 36;-fx-border-radius:12;-fx-background-radius:12;" +
            "-fx-border-color:" + CYAN + ";-fx-border-width:1.5;-fx-cursor:hand;" +
            "-fx-font-size:14px;-fx-font-weight:bold;-fx-font-family:'Arial';" +
            "-fx-min-width:200px;";
        String rulesHov =
            "-fx-background-color:linear-gradient(to right,#0c3f60,#085e48);" +
            "-fx-text-fill:" + CYAN + ";" +
            "-fx-padding:13 36;-fx-border-radius:12;-fx-background-radius:12;" +
            "-fx-border-color:" + CYAN + ";-fx-border-width:2;-fx-cursor:hand;" +
            "-fx-font-size:14px;-fx-font-weight:bold;-fx-font-family:'Arial';" +
            "-fx-min-width:200px;" +
            "-fx-effect:dropshadow(three-pass-box," + CYAN + "99,18,0.5,0,0);";
        btnRules.setStyle(rulesDef);
        btnRules.setOnMouseEntered(e -> btnRules.setStyle(rulesHov));
        btnRules.setOnMouseExited(e -> btnRules.setStyle(rulesDef));
        btnRules.setOnAction(e -> showInstructions());

        // ── SETTINGS button ──
        Button btnSettings = new Button("SETTINGS");
        String sdDef =
            "-fx-background-color:linear-gradient(to right,#1a0a00,#2d1500);" +
            "-fx-text-fill:" + GOLD + ";" +
            "-fx-padding:13 36;-fx-border-radius:12;-fx-background-radius:12;" +
            "-fx-border-color:" + GOLD + ";-fx-border-width:1.5;-fx-cursor:hand;" +
            "-fx-font-size:14px;-fx-font-weight:bold;-fx-font-family:'Arial';" +
            "-fx-min-width:200px;";
        String sdHov =
            "-fx-background-color:linear-gradient(to right,#2d1500,#4a2200);" +
            "-fx-text-fill:" + GOLD + ";" +
            "-fx-padding:13 36;-fx-border-radius:12;-fx-background-radius:12;" +
            "-fx-border-color:" + GOLD + ";-fx-border-width:2;-fx-cursor:hand;" +
            "-fx-font-size:14px;-fx-font-weight:bold;-fx-font-family:'Arial';" +
            "-fx-min-width:200px;" +
            "-fx-effect:dropshadow(three-pass-box," + GOLD + "99,18,0.5,0,0);";
        btnSettings.setStyle(sdDef);
        btnSettings.setOnMouseEntered(e -> btnSettings.setStyle(sdHov));
        btnSettings.setOnMouseExited(e -> btnSettings.setStyle(sdDef));
        btnSettings.setOnAction(e -> showSettings());

        // ── FULLSCREEN toggle button ──
        final boolean[] isFullScreen = {false};
        final String fsIconOff = "FULL SCREEN";
        final String fsIconOn  = "EXIT FULL SCREEN";
        Button btnFullScreen = new Button(fsIconOff);
        String fsDef =
            "-fx-background-color:linear-gradient(to right,#0d1b2a,#162032);" +
            "-fx-text-fill:#a78bfa;" +
            "-fx-padding:13 36;-fx-border-radius:12;-fx-background-radius:12;" +
            "-fx-border-color:#7c3aed;-fx-border-width:1.5;-fx-cursor:hand;" +
            "-fx-font-size:14px;-fx-font-weight:bold;-fx-font-family:'Arial';" +
            "-fx-min-width:200px;";
        String fsHov =
            "-fx-background-color:linear-gradient(to right,#1a2d42,#22354c);" +
            "-fx-text-fill:#c4b5fd;" +
            "-fx-padding:13 36;-fx-border-radius:12;-fx-background-radius:12;" +
            "-fx-border-color:#a78bfa;-fx-border-width:2;-fx-cursor:hand;" +
            "-fx-font-size:14px;-fx-font-weight:bold;-fx-font-family:'Arial';" +
            "-fx-min-width:200px;" +
            "-fx-effect:dropshadow(three-pass-box,#7c3aed99,18,0.5,0,0);";
        btnFullScreen.setStyle(fsDef);
        btnFullScreen.setOnMouseEntered(e -> btnFullScreen.setStyle(fsHov));
        btnFullScreen.setOnMouseExited(e -> btnFullScreen.setStyle(fsDef));
        btnFullScreen.setOnAction(e -> {
            isFullScreen[0] = !isFullScreen[0];
            primaryStage.setFullScreen(isFullScreen[0]);
            primaryStage.setFullScreenExitHint("");
            btnFullScreen.setText(isFullScreen[0] ? fsIconOn : fsIconOff);
        });

        HBox actionRow = new HBox(18, btnRules, btnSettings, btnFullScreen);
        actionRow.setAlignment(Pos.CENTER);
        actionRow.setPadding(new Insets(10, 0, 10, 0));

        Label hint = new Label("[ 1 ] Scarer   -   [ 2 ] Laugher   -   [ F1 ] Rules   -   [ F11 ] Full Screen");
        hint.setFont(Font.font("Arial", 11));
        hint.setTextFill(Color.web(TEXT_MUTED, 0.40));

        bottomBox.getChildren().addAll(actionRow, hint);
        return bottomBox;
    }

    // ======================================================================
    // PARTICLE BACKGROUND
    // ======================================================================
    private Pane buildParticleLayer() {
        Pane pane = new Pane();
        pane.setMouseTransparent(true);
        pane.setPickOnBounds(false);

        Random rand = new Random();
        String[] colors = {PURPLE, GOLD, CYAN, "#a78bfa", "#38bdf8", "#f472b6"};

        for (int i = 0; i < 55; i++) {
            double size = rand.nextDouble() * 3.5 + 0.8;
            Circle c = new Circle(size);
            c.setFill(Color.web(colors[rand.nextInt(colors.length)], rand.nextDouble() * 0.35 + 0.05));
            c.setLayoutX(rand.nextDouble() * 1100);
            c.setLayoutY(820 + rand.nextDouble() * 150);

            double dur   = rand.nextDouble() * 9000 + 5000;
            double delay = rand.nextDouble() * 14000;

            Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(c.layoutYProperty(), c.getLayoutY()),
                    new KeyValue(c.opacityProperty(), 0.0)),
                new KeyFrame(Duration.millis(dur * 0.15),
                    new KeyValue(c.opacityProperty(), rand.nextDouble() * 0.45 + 0.1)),
                new KeyFrame(Duration.millis(dur * 0.85),
                    new KeyValue(c.opacityProperty(), rand.nextDouble() * 0.45 + 0.1)),
                new KeyFrame(Duration.millis(dur),
                    new KeyValue(c.layoutYProperty(), -30.0),
                    new KeyValue(c.opacityProperty(), 0.0))
            );
            tl.setDelay(Duration.millis(delay));
            tl.setCycleCount(Animation.INDEFINITE);
            tl.play();
            pane.getChildren().add(c);
        }
        return pane;
    }

    // ======================================================================
    // BREATHING GLOW ON TITLE
    // ======================================================================
    private void animateBreathingGlow(DropShadow glow) {
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(glow.radiusProperty(), 12.0),
                new KeyValue(glow.colorProperty(), Color.web(ORANGE, 0.5))),
            new KeyFrame(Duration.millis(1800),
                new KeyValue(glow.radiusProperty(), 24.0),
                new KeyValue(glow.colorProperty(), Color.web(GOLD, 0.95))),
            new KeyFrame(Duration.millis(3600),
                new KeyValue(glow.radiusProperty(), 12.0),
                new KeyValue(glow.colorProperty(), Color.web(ORANGE, 0.5)))
        );
        tl.setCycleCount(Animation.INDEFINITE);
        tl.play();
    }

    // ======================================================================
    // LAUNCH GAME WITH TRANSITION
    // ======================================================================
    private void launchGameWithTransition(Stage stage, String role) {
        stopMenuMusic();
        playSound("start.mp3");

        Region fadeOverlay = new Region();
        fadeOverlay.setStyle("-fx-background-color: " + BG_APP + ";");
        fadeOverlay.setOpacity(0);
        rootNode.getChildren().add(fadeOverlay);

        Label loadingLabel = new Label("ENTERING THE FLOOR...");
        loadingLabel.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 22));
        loadingLabel.setTextFill(Color.web(GOLD));
        loadingLabel.setEffect(new DropShadow(10, Color.web(ORANGE, 0.8)));
        loadingLabel.setOpacity(0);
        rootNode.getChildren().add(loadingLabel);

        FadeTransition overlayFade = new FadeTransition(Duration.millis(500), fadeOverlay);
        overlayFade.setToValue(1.0);
        overlayFade.setOnFinished(e -> {
            FadeTransition lblFadeIn = new FadeTransition(Duration.millis(300), loadingLabel);
            lblFadeIn.setToValue(1.0);
            lblFadeIn.play();

            Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO,        new KeyValue(loadingLabel.scaleXProperty(), 1.0),  new KeyValue(loadingLabel.scaleYProperty(), 1.0)),
                new KeyFrame(Duration.millis(400), new KeyValue(loadingLabel.scaleXProperty(), 1.08), new KeyValue(loadingLabel.scaleYProperty(), 1.08)),
                new KeyFrame(Duration.millis(800), new KeyValue(loadingLabel.scaleXProperty(), 1.0),  new KeyValue(loadingLabel.scaleYProperty(), 1.0))
            );
            pulse.setCycleCount(2);
            pulse.setOnFinished(ev -> {
                stage.setFullScreen(true);
                stage.setFullScreenExitHint("");
                stage.iconifiedProperty().addListener((obs, wasMin, isNowMin) -> {
                    if (isNowMin) showMinimizeWarning(stage);
                });
                Timeline switchDelay = new Timeline(
                    new KeyFrame(Duration.millis(250), evSwitch -> new GameWindow(stage, role))
                );
                switchDelay.play();
            });
            pulse.play();
        });
        overlayFade.play();
    }

    // ======================================================================
    // MINIMIZE WARNING POPUP
    // ======================================================================
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

        Label icon = new Label("[ FULLSCREEN ]");
        icon.setFont(Font.font("Arial Black", FontWeight.BOLD, 14));
        icon.setTextFill(Color.web(GOLD, 0.7));
        Label head = new Label("RETURN TO FULL SCREEN");
        head.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 20));
        head.setTextFill(Color.web(GOLD));
        head.setEffect(new DropShadow(8, Color.web(GOLD, 0.6)));

        Label body = new Label("For the best experience, this game is designed\nto be played in full screen mode.\n\nPlease restore the window to continue.");
        body.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        body.setTextFill(Color.web(TEXT_LIGHT));
        body.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        body.setWrapText(true); body.setMaxWidth(300);

        Button btnRestore = new Button("<< RESTORE FULL SCREEN");
        btnRestore.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 13));
        btnRestore.setStyle("-fx-background-color:" + GOLD + ";-fx-text-fill:#1a0a00;" +
            "-fx-padding:11 28;-fx-background-radius:10;-fx-border-radius:10;-fx-cursor:hand;" +
            "-fx-effect:dropshadow(three-pass-box," + GOLD + "88,12,0.4,0,0);");
        btnRestore.setPrefWidth(240);
        btnRestore.setOnAction(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(180), layout);
            ft.setToValue(0);
            ft.setOnFinished(ev -> { pop.close(); ownerStage.setIconified(false); ownerStage.setFullScreen(true); ownerStage.toFront(); });
            ft.play();
        });

        layout.getChildren().addAll(icon, head, body, btnRestore);
        StackPane overlay = new StackPane(layout);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.75);");
        layout.setOpacity(0); layout.setTranslateY(20);
        Scene sc = new Scene(overlay); sc.setFill(Color.TRANSPARENT);
        pop.setScene(sc); pop.show();

        javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
        pop.setX((screen.getWidth()  - pop.getWidth())  / 2);
        pop.setY((screen.getHeight() - pop.getHeight()) / 2);

        FadeTransition ft = new FadeTransition(Duration.millis(280), layout); ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(280), layout); tt.setToY(0);
        ft.play(); tt.play();
    }

    // ======================================================================
    // HOW TO PLAY POPUP
    // ======================================================================
    private void showInstructions() {
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setPadding(new Insets(30, 44, 30, 44));
        layout.setMaxWidth(560);
        layout.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        layout.setStyle("-fx-background-color:linear-gradient(to bottom right,#1a0a2e,#0a0a0f);" +
            "-fx-border-color:" + CYAN + ";-fx-border-width:2;" +
            "-fx-border-radius:14;-fx-background-radius:14;" +
            "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.9),25,0.3,0,8);");

        Label head = new Label("HOW TO PLAY");
        head.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 22));
        head.setTextFill(Color.web(CYAN));
        head.setEffect(new DropShadow(8, Color.web(CYAN, 0.5)));

        VBox rulesBox = new VBox(8);
        rulesBox.setAlignment(Pos.TOP_LEFT);

        Object[][] rules = {
            {"[ GOAL ]", true},
            {"Race to Boo's Door (Cell 99) first AND hold >= 1000 Energy to win.", false},
            {"[ ENERGY ]", true},
            {"Collect energy from Doors matching your role. Avoid rival Doors \u2014 they drain you.", false},
            {"[ SPECIAL CELLS ]", true},
            {"  [D] Doors       \u2014 Boost allies, penalise rivals (fires once per door)", false},
            {"  [^] Conveyors   \u2014 Launch you forward instantly", false},
            {"  [S] Socks       \u2014 Move back AND lose 100 energy", false},
            {"  [C] Card Cells  \u2014 Draw a fate card (swap, shield, steal, confuse...)", false},
            {"  [M] Monsters    \u2014 Ally: trigger powerup  |  Enemy: energy equalised", false},
            {"[ POWERUPS ]", true},
            {"Cost 500 E. Each monster type has a unique ability \u2014 use it wisely!", false},
            {"[ SHORTCUTS ]", true},
            {"[1] Scarer  |  [2] Laugher  |  [F1] Rules  |  In-game: [W] Teleport  [E] +500 E", false},
        };

        for (Object[] row : rules) {
            Label l = new Label((String) row[0]);
            if ((boolean) row[1]) {
                l.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 14));
                l.setTextFill(Color.web(GOLD));
                VBox.setMargin(l, new Insets(8, 0, 0, 0));
            } else {
                l.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
                l.setTextFill(Color.web(TEXT_LIGHT));
                l.setWrapText(true);
            }
            rulesBox.getChildren().add(l);
        }

        StackPane overlayPane = new StackPane(layout);
        overlayPane.setStyle("-fx-background-color:rgba(0,0,0,0.78);");

        Button ok = new Button("UNDERSTOOD");
        ok.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        ok.setStyle("-fx-background-color:" + CYAN + ";-fx-text-fill:#0a0a1a;" +
            "-fx-padding:10 30;-fx-background-radius:8;-fx-cursor:hand;");
        ok.setPrefWidth(200);
        VBox.setMargin(ok, new Insets(18, 0, 0, 0));
        ok.setOnAction(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(200), overlayPane);
            ft.setToValue(0); ft.setOnFinished(ev -> rootNode.getChildren().remove(overlayPane)); ft.play();
        });
        overlayPane.setOnMouseClicked(e -> {
            if (e.getTarget() == overlayPane) {
                FadeTransition ft = new FadeTransition(Duration.millis(200), overlayPane);
                ft.setToValue(0); ft.setOnFinished(ev -> rootNode.getChildren().remove(overlayPane)); ft.play();
            }
        });

        layout.getChildren().addAll(head, rulesBox, ok);
        overlayPane.setOpacity(0);
        rootNode.getChildren().add(overlayPane);

        layout.setTranslateY(24);
        FadeTransition ft = new FadeTransition(Duration.millis(260), overlayPane); ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(260), layout); tt.setToY(0);
        ft.play(); tt.play();
    }

    // ======================================================================
    // BACKGROUND MUSIC
    // ======================================================================
    private void startMenuMusic() {
        try {
            File file = new File("assets/menu_music.mp3");
            if (!file.exists()) return;
            Media media = new Media(file.toURI().toString());
            bgMusic = new MediaPlayer(media);
            bgMusic.setCycleCount(MediaPlayer.INDEFINITE);
            bgMusic.setVolume(0);
            bgMusic.play();
            Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.ZERO,        new KeyValue(bgMusic.volumeProperty(), 0.0)),
                new KeyFrame(Duration.millis(2000), new KeyValue(bgMusic.volumeProperty(), musicVolume))
            );
            fadeIn.play();
        } catch (Exception ignored) {}
    }

    private void stopMenuMusic() {
        if (bgMusic == null) return;
        // Fade out over 500ms then stop
        Timeline fadeOut = new Timeline(
            new KeyFrame(Duration.ZERO,       new KeyValue(bgMusic.volumeProperty(), bgMusic.getVolume())),
            new KeyFrame(Duration.millis(500), new KeyValue(bgMusic.volumeProperty(), 0.0))
        );
        fadeOut.setOnFinished(e -> { bgMusic.stop(); bgMusic.dispose(); bgMusic = null; });
        fadeOut.play();
    }

    // ======================================================================
    // SOUND
    // ======================================================================
    private void playSound(String filename) {
        String fname = filename.endsWith(".wav")
            ? filename.substring(0, filename.length() - 4) + ".mp3" : filename;
        try {
            File file = new File("assets/" + fname);
            if (!file.exists()) file = new File("assets/" + filename);
            if (file.exists()) {
                Media media = new Media(file.toURI().toString());
                MediaPlayer mp = new MediaPlayer(media);
                mp.setVolume(effectsVolume);
                activePlayers.add(mp);
                mp.setOnEndOfMedia(() -> { mp.dispose(); activePlayers.remove(mp); });
                mp.play();
            }
        } catch (Exception ignored) {}
    }

    // ======================================================================
    // GAME INTRO CINEMATIC
    // ======================================================================
    private void showIntro(StackPane root, Runnable onComplete) {
        StackPane introPane = new StackPane();
        introPane.setStyle("-fx-background-color: black;");
        root.getChildren().add(introPane);

        // Phase 1: Studio card
        Label studio = new Label("A   M O N S T E R S   I N C.   P R O D U C T I O N");
        studio.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        studio.setTextFill(Color.web("#aaaaaa"));
        studio.setOpacity(0);
        StackPane.setAlignment(studio, Pos.CENTER);

        // Phase 2: Big title
        Label bigTitle = new Label("DooR DasH");
        bigTitle.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 86));
        bigTitle.setTextFill(Color.web(GOLD));
        bigTitle.setOpacity(0); bigTitle.setScaleX(0.2); bigTitle.setScaleY(0.2);
        DropShadow titleGlow = new DropShadow(0, Color.web(ORANGE));
        bigTitle.setEffect(titleGlow);

        Label bigSub = new Label("S C A R E   *   V S   *   L A U G H   T O U C H D O W N");
        bigSub.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        bigSub.setTextFill(Color.web(TEXT_MUTED));
        bigSub.setOpacity(0);

        Label skipHint = new Label("[ CLICK TO SKIP ]");
        skipHint.setFont(Font.font("Arial", 11));
        skipHint.setTextFill(Color.web("#333333"));
        StackPane.setAlignment(skipHint, Pos.BOTTOM_CENTER);
        StackPane.setMargin(skipHint, new Insets(0, 0, 18, 0));

        VBox titleGroup = new VBox(10, bigTitle, bigSub);
        titleGroup.setAlignment(Pos.CENTER);
        StackPane.setAlignment(titleGroup, Pos.CENTER);

        // === CHROMATIC ABERRATION GHOST LABELS ===
        // Red channel — offset left
        Label ghostR = new Label("DooR DasH");
        ghostR.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 86));
        ghostR.setTextFill(Color.rgb(255, 30, 30, 0.55));
        ghostR.setOpacity(0);
        ghostR.setTranslateX(-8); ghostR.setTranslateY(2);
        StackPane.setAlignment(ghostR, Pos.CENTER);

        // Blue/cyan channel — offset right
        Label ghostB = new Label("DooR DasH");
        ghostB.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 86));
        ghostB.setTextFill(Color.rgb(30, 200, 255, 0.55));
        ghostB.setOpacity(0);
        ghostB.setTranslateX(8); ghostB.setTranslateY(-2);
        StackPane.setAlignment(ghostB, Pos.CENTER);

        // Green channel — slight vertical offset
        Label ghostG = new Label("DooR DasH");
        ghostG.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 86));
        ghostG.setTextFill(Color.rgb(30, 255, 120, 0.30));
        ghostG.setOpacity(0);
        ghostG.setTranslateX(3); ghostG.setTranslateY(6);
        StackPane.setAlignment(ghostG, Pos.CENTER);

        introPane.getChildren().addAll(ghostR, ghostB, ghostG, studio, titleGroup, skipHint);

        Runnable finish = () -> {
            FadeTransition out = new FadeTransition(Duration.millis(600), introPane);
            out.setToValue(0);
            out.setOnFinished(e -> { root.getChildren().remove(introPane); onComplete.run(); });
            out.play();
        };

        FadeTransition studioIn  = new FadeTransition(Duration.millis(1200), studio); studioIn.setToValue(1.0);
        FadeTransition studioOut = new FadeTransition(Duration.millis(800),  studio); studioOut.setToValue(0.0);

        FadeTransition titleFade = new FadeTransition(Duration.millis(900), bigTitle); titleFade.setToValue(1.0);
        ScaleTransition titleZoom = new ScaleTransition(Duration.millis(900), bigTitle);
        titleZoom.setToX(1.0); titleZoom.setToY(1.0);
        titleZoom.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        Timeline glowBuild = new Timeline(
            new KeyFrame(Duration.ZERO,        new KeyValue(titleGlow.radiusProperty(), 0.0)),
            new KeyFrame(Duration.millis(900),  new KeyValue(titleGlow.radiusProperty(), 28.0))
        );
        javafx.animation.ParallelTransition titleReveal =
            new javafx.animation.ParallelTransition(titleFade, titleZoom, glowBuild);

        FadeTransition subIn = new FadeTransition(Duration.millis(600), bigSub); subIn.setToValue(1.0);

        // Chromatic aberration glitch — rapid flicker frames before title slams in
        Timeline glitchAnim = new Timeline();
        double[] offsets = {-10, 12, -6, 8, -14, 5}; // random X shifts per frame
        for (int i = 0; i < 6; i++) {
            final double ox = offsets[i];
            final boolean show = (i % 2 == 0); // alternate on/off
            glitchAnim.getKeyFrames().add(new KeyFrame(Duration.millis(i * 55), ev -> {
                double vis = show ? 1.0 : 0.0;
                ghostR.setOpacity(vis); ghostR.setTranslateX(-8 + ox * 0.6);
                ghostB.setOpacity(vis); ghostB.setTranslateX( 8 - ox * 0.4);
                ghostG.setOpacity(vis * 0.6);
            }));
        }
        // Hide ghosts after glitch
        glitchAnim.getKeyFrames().add(new KeyFrame(Duration.millis(360), ev -> {
            ghostR.setOpacity(0); ghostB.setOpacity(0); ghostG.setOpacity(0);
        }));

        // Sound effect triggers
        javafx.animation.PauseTransition pProjector = new javafx.animation.PauseTransition(Duration.millis(1));
        pProjector.setOnFinished(e -> playSound("intro_projector.mp3"));
        javafx.animation.PauseTransition pGlitch = new javafx.animation.PauseTransition(Duration.millis(1));
        pGlitch.setOnFinished(e -> playSound("intro_glitch.mp3"));
        javafx.animation.PauseTransition pBoom = new javafx.animation.PauseTransition(Duration.millis(1));
        pBoom.setOnFinished(e -> playSound("intro_boom.mp3"));
        javafx.animation.PauseTransition pShimmer = new javafx.animation.PauseTransition(Duration.millis(1));
        pShimmer.setOnFinished(e -> playSound("intro_shimmer.mp3"));

        SequentialTransition seq = new SequentialTransition(
            pProjector,
            studioIn,
            new javafx.animation.PauseTransition(Duration.millis(900)),
            studioOut,
            new javafx.animation.PauseTransition(Duration.millis(200)),
            pGlitch,
            glitchAnim,                   // <<< GLITCH fires here
            pBoom,
            titleReveal,                  // <<< then title slams in
            pShimmer,
            subIn,
            new javafx.animation.PauseTransition(Duration.millis(1600))
        );
        seq.setOnFinished(e -> finish.run());
        seq.play();
        introPane.setOnMouseClicked(e -> { seq.stop(); finish.run(); });
    }

    // ======================================================================
    // SETTINGS POPUP
    // ======================================================================
    private void showSettings() {
        playSound("hover.mp3");

        VBox layout = new VBox(22);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(38, 54, 38, 54));
        layout.setMaxWidth(420);
        layout.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        layout.setStyle("-fx-background-color:linear-gradient(to bottom right,#1a0a2e,#0a0a0f);" +
            "-fx-border-color:" + GOLD + ";-fx-border-width:2;" +
            "-fx-border-radius:14;-fx-background-radius:14;" +
            "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.9),25,0.3,0,8);");

        Label head = new Label("SETTINGS");
        head.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 22));
        head.setTextFill(Color.web(GOLD));
        head.setEffect(new DropShadow(8, Color.web(GOLD, 0.5)));

        VBox musicSec   = buildSliderSection("MUSIC VOLUME",   musicVolume,   PURPLE, v -> {
            musicVolume = v;
            if (bgMusic != null) bgMusic.setVolume(musicVolume);
        });
        VBox effectsSec = buildSliderSection("EFFECTS VOLUME", effectsVolume, CYAN,   v -> {
            effectsVolume = v;
        });

        StackPane overlayPane = new StackPane(layout);
        overlayPane.setStyle("-fx-background-color:rgba(0,0,0,0.78);");

        Button ok = new Button("SAVE & CLOSE");
        ok.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        ok.setStyle("-fx-background-color:" + GOLD + ";-fx-text-fill:#1a0a00;" +
            "-fx-padding:10 30;-fx-background-radius:8;-fx-cursor:hand;");
        ok.setPrefWidth(220);
        VBox.setMargin(ok, new Insets(8, 0, 0, 0));
        ok.setOnAction(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(200), overlayPane);
            ft.setToValue(0); ft.setOnFinished(ev -> rootNode.getChildren().remove(overlayPane)); ft.play();
        });
        overlayPane.setOnMouseClicked(e -> {
            if (e.getTarget() == overlayPane) {
                FadeTransition ft = new FadeTransition(Duration.millis(200), overlayPane);
                ft.setToValue(0); ft.setOnFinished(ev -> rootNode.getChildren().remove(overlayPane)); ft.play();
            }
        });

        layout.getChildren().addAll(head, musicSec, effectsSec, ok);
        overlayPane.setOpacity(0);
        rootNode.getChildren().add(overlayPane);

        layout.setTranslateY(24);
        FadeTransition ft2 = new FadeTransition(Duration.millis(260), overlayPane); ft2.setToValue(1);
        TranslateTransition tt2 = new TranslateTransition(Duration.millis(260), layout); tt2.setToY(0);
        ft2.play(); tt2.play();
    }

    private VBox buildSliderSection(String labelText, double initial, String accent,
                                    java.util.function.DoubleConsumer onChange) {
        VBox sec = new VBox(8);
        sec.setAlignment(Pos.CENTER_LEFT);
        sec.setMaxWidth(300);

        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.web(accent));

        javafx.scene.control.Slider slider = new javafx.scene.control.Slider(0, 1, initial);
        slider.setPrefWidth(280);
        slider.setStyle("-fx-control-inner-background:" + accent + "33;" +
            "-fx-accent:" + accent + ";");

        Label valLabel = new Label(String.format("%.0f%%", initial * 100));
        valLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        valLabel.setTextFill(Color.web(TEXT_MUTED));

        slider.valueProperty().addListener((obs, old, val) -> {
            onChange.accept(val.doubleValue());
            valLabel.setText(String.format("%.0f%%", val.doubleValue() * 100));
        });

        HBox row = new HBox(12, slider, valLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        sec.getChildren().addAll(lbl, row);
        return sec;
    }

    // ======================================================================
    // IMAGE LOADER
    // ======================================================================
    private ImageView loadIcon(String filename, int size) {
        try {
            File file = new File("assets/" + filename);
            if (file.exists()) {
                Image img = new Image(file.toURI().toString());
                ImageView view = new ImageView(img);
                view.setFitWidth(size); view.setFitHeight(size);
                view.setPreserveRatio(true);
                view.setEffect(new DropShadow(10, 2, 2, Color.web("#000000", 0.8)));
                return view;
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ======================================================================
    // TEAM CARD (now includes monster roster)
    // ======================================================================
    private VBox createTeamCard(String teamName, String desc, String accentColor, String imgFile, String[] roster) {
        VBox card = new VBox(18);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(36, 28, 36, 28));
        card.setPrefWidth(350); card.setMaxWidth(350);
        card.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

        String defStyle = "-fx-background-color:" + BG_CARD + ";" +
            "-fx-border-color:" + PURPLE_BORDER + ";-fx-border-width:2;" +
            "-fx-border-radius:16;-fx-background-radius:16;" +
            "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.6),15,0,0,10);";
        String hovStyle = "-fx-background-color:" + BG_CARD + ";" +
            "-fx-border-color:" + accentColor + ";-fx-border-width:2;" +
            "-fx-border-radius:16;-fx-background-radius:16;" +
            "-fx-effect:dropshadow(three-pass-box," + accentColor + "66,28,0.45,0,0);";
        card.setStyle(defStyle);

        ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
        card.setOnMouseEntered(e -> {
            playSound("hover.mp3");
            card.setStyle(hovStyle);
            st.setToX(1.03); st.setToY(1.03); st.playFromStart();
        });
        card.setOnMouseExited(e -> {
            card.setStyle(defStyle);
            st.setToX(1.0); st.setToY(1.0); st.playFromStart();
        });

        // Monster image
        ImageView icon = loadIcon(imgFile, 140);
        StackPane iconPane = new StackPane();
        iconPane.setMinHeight(150);
        if (icon != null) iconPane.getChildren().add(icon);
        else { Label m = new Label("[?]"); m.setFont(Font.font("Arial Black", FontWeight.BOLD, 42)); m.setTextFill(Color.web(TEXT_MUTED)); iconPane.getChildren().add(m); }

        Label name = new Label(teamName);
        name.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 24));
        name.setTextFill(Color.web(TEXT_LIGHT));
        name.setEffect(new DropShadow(5, Color.web(accentColor, 0.6)));

        Label description = new Label(desc);
        description.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        description.setTextFill(Color.web(TEXT_MUTED));
        description.setWrapText(true);
        description.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        description.setMinHeight(52);

        card.getChildren().addAll(iconPane, name, description);
        return card;
    }

    // ======================================================================
    // PLAY BUTTON
    // ======================================================================
    private Button createPlayButton(String colorHex, String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        btn.setMaxWidth(Double.MAX_VALUE);
        String def = "-fx-background-color:" + colorHex + ";-fx-text-fill:white;" +
            "-fx-padding:13;-fx-border-radius:10;-fx-background-radius:10;-fx-cursor:hand;";
        String hov = "-fx-background-color:derive(" + colorHex + ",20%);-fx-text-fill:white;" +
            "-fx-padding:13;-fx-border-radius:10;-fx-background-radius:10;-fx-cursor:hand;" +
            "-fx-effect:dropshadow(three-pass-box," + colorHex + "99,18,0.45,0,0);";
        btn.setStyle(def);
        btn.setOnMouseEntered(e -> btn.setStyle(hov));
        btn.setOnMouseExited(e -> btn.setStyle(def));
        return btn;
    }

    public static void main(String[] args) { launch(args); }
}