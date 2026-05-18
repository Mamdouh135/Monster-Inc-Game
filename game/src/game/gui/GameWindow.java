package game.gui;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;

import java.io.File;

import game.engine.Board;
import game.engine.Constants;
import game.engine.Game;
import game.engine.Role;
import game.engine.cards.Card;
import game.engine.monsters.Monster;
import game.engine.monsters.Dasher;
import game.engine.monsters.MultiTasker;
import game.engine.cells.*;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;

public class GameWindow {

    // === PALETTE ===
    private static final String BG_APP        = "#0a0a1a";
    private static final String BG_CARD       = "linear-gradient(to bottom right, #12082a, #1a1040)";
    private static final String BG_BOARD      = "#0d0d22";
    private static final String BG_CONTROLS   = "#12082a";
    private static final String BG_INNER      = "#ffffff08";
    private StackPane rootOverlay;

    private static final String PURPLE        = "#7c3aed";
    private static final String PURPLE_DIM    = "#7c3aed22";
    private static final String GOLD          = "#fbbf24";
    private static final String ORANGE        = "#f97316";
    private static final String GREEN         = "#10b981";
    private static final String GREEN_DIM     = "#059669";
    private static final String RED           = "#ef4444";
    private static final String RED_DIM       = "#991b1b";
    private static final String CYAN          = "#67e8f9";
    private static final String VIOLET_TEXT   = "#c4b5fd";
    private static final String BLUE_TEXT     = "#93c5fd";
    private static final String GREEN_TEXT    = "#86efac";
    private static final String AMBER_TEXT    = "#fde68a";
    private static final String MUTED         = "#9ca3af";
    private static final String TEXT_LIGHT    = "#f0ece0";

    // === CELL COLOURS ===
    private static final String[] CELL_NORMAL   = {"#1e1b3a", "#2d2860"};
    private static final String[] CELL_DOOR_S   = {"rgba(30,58,138,0.25)", "#1e3a8a"};
    private static final String[] CELL_DOOR_L   = {"rgba(20,83,45,0.25)",  "#22c55e"};
    private static final String[] CELL_CARD     = {"rgba(220,38,38,0.25)", "#f87171"};
    private static final String[] CELL_CONVEYOR = {"rgba(5,150,105,0.25)", "#34d399"};
    private static final String[] CELL_SOCK     = {"rgba(217,119,6,0.25)", "#fbbf24"};
    private static final String[] CELL_MONSTER  = {"rgba(124,58,237,0.25)","#a78bfa"};
    private static final String[] CELL_START    = {"rgba(107,114,128,0.2)","#9ca3af"};
    private static final String[] CELL_END      = {"rgba(251,191,36,0.2)", "#fbbf24"};

    // === STATE ===
    private final Stage stage;
    private Game game;
    private BorderPane mainLayout;

    private final StackPane[] cellPanes = new StackPane[100];
    private GridPane boardGrid;

    private Label lblTurnBadge;
    private VBox playerCard, oppCard;
    private Label lblPlayerName, lblPlayerType, lblPlayerEnergy;
    private Label tagPlayerRole, tagPlayerShield, tagPlayerConfusion, tagPlayerFreeze;
    private Label tagPlayerMomentum, tagPlayerFocus; 
    private ProgressBar barPlayerEnergy;
    private Button btnPlayerPowerup;
    
    private Label lblOppName, lblOppType, lblOppEnergy;
    private Label tagOppRole, tagOppShield, tagOppConfusion, tagOppFreeze;
    private Label tagOppMomentum, tagOppFocus; 
    private ProgressBar barOppEnergy;
    private Button btnOppPowerup;

    private Button btnRoll, btnCheatGate, btnCheatEnergy;
    private ImageView diceView;
    private VBox logBox, cardVisualBox;
    private Label lblCardIcon, lblLastCardName, lblLastCardEffect, lblPileCount;

    private int lastPlayerEnergy = -1, lastOppEnergy = -1;
    
    // ANIMATION VARIABLES
    private javafx.scene.layout.Pane animationLayer;
    private boolean isAnimating = false;

    // ======================================================================
    // CONSTRUCTOR
    // ======================================================================
    public GameWindow(Stage stage, String side) {
        this.stage = stage;
        try {
            this.game = new Game(Role.valueOf(side));
        } catch (Exception e) {
            showError("System Error", "Engine failure: " + e.getMessage());
            return;
        }
        buildUI();
        cacheEnergy();
    }

    private void cacheEnergy() {
        if (game.getPlayer()   != null) lastPlayerEnergy = game.getPlayer().getEnergy();
        if (game.getOpponent() != null) lastOppEnergy    = game.getOpponent().getEnergy();
    }

    private int getDeckSize() {
        try {
            return Board.getCards() != null ? Board.getCards().size() : 24;
        } catch (Exception e) {
            return 24; 
        }
    }

    private void playSound(String f) {
        String fname = f.endsWith(".wav") ? f.substring(0, f.length() - 4) + ".mp3" : f;
        try {
            File file = new File("assets/" + fname);
            if (!file.exists()) {
                file = new File("assets/" + f);
            }
            if (file.exists()) {
                Media media = new Media(file.toURI().toString());
                MediaPlayer mp = new MediaPlayer(media);
                mp.setVolume(SoundManager.volume);
                mp.setOnEndOfMedia(mp::dispose);
                mp.play();
            }
        } catch (Exception ignored) {}
    }

    private static final java.util.Map<String, String> MONSTER_IMG_MAP = new java.util.HashMap<>();
    static {
        MONSTER_IMG_MAP.put("James P. Sullivan",   "james_p_sullivan.png");
        MONSTER_IMG_MAP.put("Mike Wazowski",       "mike_wazowski.png");
        MONSTER_IMG_MAP.put("Randall Boggs",       "randall_boggs.png");
        MONSTER_IMG_MAP.put("Celia Mae",           "celia_mae.png");
        MONSTER_IMG_MAP.put("Roz",                 "roz.png");
        MONSTER_IMG_MAP.put("Fungus",              "fungus.png");
        MONSTER_IMG_MAP.put("Henry J. Waternoose", "henry_j_waternoose.png");
        MONSTER_IMG_MAP.put("Yeti",                "yeti.png");
    }

    private Image loadImage(String f) {
        try {
            File file = new File("assets/" + f);
            if (file.exists()) return new Image(file.toURI().toString());
        } catch (Exception ignored) {}
        try {
            java.net.URL url = getClass().getResource("/assets/" + f);
            if (url != null) return new Image(url.toExternalForm());
        } catch (Exception ignored) {}
        try {
            File file = new File(System.getProperty("user.dir") + "/assets/" + f);
            if (file.exists()) return new Image(file.toURI().toString());
        } catch (Exception ignored) {}
        return null;
    }

    private ImageView loadIcon(String f, int size) {
        Image img = loadImage(f);
        if (img == null) return null;
        ImageView iv = new ImageView(img);
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        return iv;
    }

    private StackPane buildDynamicAvatar(Monster m, String ringColor, int size) {
        StackPane sp = new StackPane();
        sp.setPrefSize(size + 10, size + 10);
        sp.setMaxSize(size + 10, size + 10);
        sp.setMinSize(size + 10, size + 10);

        javafx.scene.shape.Circle bgCircle = new javafx.scene.shape.Circle((size + 10) / 2.0);
        bgCircle.setFill(Color.BLACK);

        String imgFile = MONSTER_IMG_MAP.get(m.getName());
        Image img = (imgFile != null) ? loadImage(imgFile) : null;

        if (img != null) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(size);
            iv.setFitHeight(size);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            StackPane.setAlignment(iv, Pos.CENTER);
            iv.setClip(new Circle(size / 2.0, size / 2.0, size / 2.0));

            javafx.scene.shape.Circle ring = new javafx.scene.shape.Circle((size + 10) / 2.0);
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(Color.web(ringColor));
            ring.setStrokeWidth(2.5);
            ring.setEffect(new DropShadow(6, Color.web(ringColor, 0.7)));

            sp.getChildren().addAll(bgCircle, iv, ring);
        } else {
            javafx.scene.shape.Circle ring = new javafx.scene.shape.Circle((size + 10) / 2.0);
            ring.setFill(Color.web("#111111"));
            ring.setStroke(Color.web(ringColor));
            ring.setStrokeWidth(2.5);
            Label letter = new Label(m.getName().substring(0, 1).toUpperCase());
            letter.setFont(Font.font("Arial Black", FontWeight.BOLD, size / 2.0));
            letter.setTextFill(Color.web(ringColor));
            StackPane.setAlignment(letter, Pos.CENTER);
            sp.getChildren().addAll(ring, letter);
        }
        return sp;
    }

    private void buildUI() {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color:" + BG_APP + ";");

        mainLayout.setTop(buildTopBar());
        mainLayout.setLeft(buildLeftPanel());
        mainLayout.setCenter(buildCenterPanel());
        mainLayout.setRight(buildRightPanel());

        refreshAll();

        ScrollPane root = new ScrollPane(mainLayout);
        root.setFitToWidth(true); root.setFitToHeight(true);
        root.setStyle("-fx-background:" + BG_APP + "; -fx-border-color:" + BG_APP + ";");

        // Set up the animation layer
        animationLayer = new javafx.scene.layout.Pane();
        animationLayer.setPickOnBounds(false);

        rootOverlay = new StackPane(root, animationLayer);
        
        Scene scene = new Scene(rootOverlay, 1380, 870);
        
        // --- ADDED KEY LISTENER FOR W AND E ---
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.W) {
                handleCheatGate();
            } else if (e.getCode() == KeyCode.E) {
                handleCheatEnergy();
            }
        });
        // --------------------------------------

        stage.setMinWidth(1100); stage.setMinHeight(720);
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
    }

    private HBox buildTopBar() {
        HBox bar = new HBox(14);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(9, 22, 9, 22));
        bar.setStyle("-fx-background-color:linear-gradient(to right,#1e0a3c,#0d1f3c,#1e0a3c);"
                   + "-fx-border-color:#7c3aed44;-fx-border-width:0 0 2 0;");

        Label title = new Label(" DooR DasH");
        ImageView titleIcon = loadIcon("boo.png", 34);
        if (titleIcon != null) {
            title.setGraphic(titleIcon);
        }
        title.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 32));
        title.setTextFill(Color.web(GOLD));
        title.setEffect(new DropShadow(10, Color.web(ORANGE, 0.7)));

        Label sub = new Label("SCARE VS LAUGH TOUCHDOWN");
        sub.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        sub.setTextFill(Color.web(MUTED));

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        lblTurnBadge = new Label("TURN INFO");
        styleTurnBadge(lblTurnBadge, "PREPARING...", true);

        bar.getChildren().addAll(title, sub, spacer, lblTurnBadge);
        return bar;
    }

    private VBox buildLeftPanel() {
        VBox panel = new VBox(12);
        panel.setPrefWidth(320);
        panel.setPadding(new Insets(18));

        panel.getChildren().add(panelLabel("PLAYER 1"));

        playerCard = monsterCard();

        HBox avatarRow = new HBox(12);
        avatarRow.setAlignment(Pos.CENTER_LEFT);
        
        StackPane avatar = buildDynamicAvatar(game.getPlayer(), PURPLE, 64);
        
        lblPlayerName = labelOf("", 22, TEXT_LIGHT, true);
        VBox nameCol = new VBox(2, lblPlayerName);
        avatarRow.getChildren().addAll(avatar, nameCol);

        lblPlayerType = labelOf("", 14, MUTED, false);

        tagPlayerRole      = makeTag("", BLUE_TEXT,  "#1e3a8a33", "#1e3a8a66");
        tagPlayerShield    = makeTag("[Shield]",  CYAN,        "#0e749022", "#0e749066");
        tagPlayerConfusion = makeTag("[Confused]",AMBER_TEXT,  "#d9770622", "#d9770666");
        tagPlayerFreeze    = makeTag("[Frozen]",  BLUE_TEXT,   "#0e749022", "#0e749066");
        
        tagPlayerMomentum  = makeTag("[Momentum]", ORANGE, "#d9770622", "#d9770666");
        tagPlayerFocus     = makeTag("[Focus]", PURPLE, "#7c3aed22", "#7c3aed66");
        tagPlayerMomentum.setVisible(false); tagPlayerMomentum.setManaged(false);
        tagPlayerFocus.setVisible(false); tagPlayerFocus.setManaged(false);
        
        FlowTags pTags = new FlowTags(tagPlayerRole, tagPlayerShield, tagPlayerConfusion, tagPlayerFreeze, tagPlayerMomentum, tagPlayerFocus);

        lblPlayerEnergy = labelOf("", 16, GOLD, true);
        barPlayerEnergy = energyBar(PURPLE);

        btnPlayerPowerup = actionButton("USE POWERUP", PURPLE, "#ffffff");
        btnPlayerPowerup.setOnAction(e -> handlePowerup());

        Region div = new Region(); div.setPrefHeight(1);
        div.setStyle("-fx-background-color:#7c3aed22;");

        btnCheatGate   = smallBtn("TELEPORT",   "#2d2860", "#c4b5fd");
        btnCheatEnergy = smallBtn("+500 ENERGY", "#2d2860", "#c4b5fd");        btnCheatGate.setOnAction(e -> handleCheatGate());
        btnCheatEnergy.setOnAction(e -> handleCheatEnergy());
        HBox cheats = new HBox(8, btnCheatGate, btnCheatEnergy);
        cheats.setAlignment(Pos.CENTER);

        playerCard.getChildren().addAll(
                avatarRow, lblPlayerType, pTags.box,
                sep(), lblPlayerEnergy, barPlayerEnergy,
                sep(), btnPlayerPowerup, div, cheats);

        panel.getChildren().add(playerCard);
        return panel;
    }

    private VBox buildCenterPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(18, 8, 18, 8));

        boardGrid = new GridPane();
        boardGrid.setHgap(3); boardGrid.setVgap(3);
        boardGrid.setPadding(new Insets(10));
        boardGrid.setStyle("-fx-background-color:" + BG_BOARD + ";"
                         + "-fx-border-color:#7c3aed44;-fx-border-width:2;"
                         + "-fx-border-radius:14;-fx-background-radius:14;");
        VBox.setVgrow(boardGrid, Priority.ALWAYS);

        for (int i = 0; i < 10; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(10); cc.setHgrow(Priority.ALWAYS);
            boardGrid.getColumnConstraints().add(cc);
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(10); rc.setVgrow(Priority.ALWAYS);
            boardGrid.getRowConstraints().add(rc);
        }

        for (int idx = 0; idx < 100; idx++) {
            int boardRow = idx / 10;
            int boardCol = (boardRow % 2 == 1) ? 9 - (idx % 10) : (idx % 10);
            StackPane cell = new StackPane();
            cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            Label num = new Label(String.valueOf(idx));
            num.setFont(Font.font("Arial", FontWeight.BOLD, 14)); 
            num.setTextFill(Color.web("#ffffff55"));
            StackPane.setAlignment(num, Pos.TOP_LEFT);
            StackPane.setMargin(num, new Insets(2, 0, 0, 3));
            cell.getChildren().add(num);

            boardGrid.add(cell, boardCol, 9 - boardRow);
            cellPanes[idx] = cell;
        }

        HBox controlsBar = buildControlsBar();
        panel.getChildren().addAll(boardGrid, controlsBar);
        return panel;
    }

    private HBox buildControlsBar() {
        HBox bar = new HBox(14);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 16, 10, 16));
        bar.setStyle("-fx-background-color:" + BG_CONTROLS + ";"
                   + "-fx-border-color:#7c3aed33;"
                   + "-fx-border-radius:14;-fx-background-radius:14;");

        diceView = new ImageView();
        diceView.setFitWidth(70); diceView.setFitHeight(70);
        Image di = loadImage("dice6.png");
        if (di != null) diceView.setImage(di);

        btnRoll = actionButton("ROLL DICE", GOLD, "#1a0a00");
        ImageView diceIcon = loadIcon("dice_small.png", 26);
        if (diceIcon != null) {
            btnRoll.setGraphic(diceIcon);
        }
        btnRoll.setPrefHeight(48);
        btnRoll.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 20));
        btnRoll.setOnAction(e -> performAnimatedRoll());

        logBox = new VBox(4);
        logBox.setPadding(new Insets(6, 10, 6, 10));
        ScrollPane logScroll = new ScrollPane(logBox);
        logScroll.setFitToWidth(true);
        logScroll.setMaxHeight(66);
        logScroll.setStyle("-fx-background:transparent;"
                         + "-fx-background-color:" + BG_INNER + ";"
                         + "-fx-border-color:transparent;"
                         + "-fx-border-radius:10;-fx-background-radius:10;");
        HBox.setHgrow(logScroll, Priority.ALWAYS);

        bar.getChildren().addAll(diceView, btnRoll, logScroll);
        return bar;
    }

    private VBox buildRightPanel() {
        VBox panel = new VBox(12);
        panel.setPrefWidth(320);
        panel.setPadding(new Insets(18));

        panel.getChildren().add(panelLabel("PLAYER 2"));

        oppCard = monsterCard();

        HBox avatarRow = new HBox(12);
        avatarRow.setAlignment(Pos.CENTER_LEFT);
        
        StackPane avatar = buildDynamicAvatar(game.getOpponent(), GREEN_DIM, 64);
        
        lblOppName = labelOf("", 22, TEXT_LIGHT, true);
        VBox nameCol = new VBox(2, lblOppName);
        avatarRow.getChildren().addAll(avatar, nameCol);

        lblOppType = labelOf("", 14, MUTED, false);

        tagOppRole      = makeTag("", GREEN_TEXT,  "#14532d33", "#14532d66");
        tagOppShield    = makeTag("[Shield]",  CYAN,        "#0e749022", "#0e749066");
        tagOppConfusion = makeTag("[Confused]",AMBER_TEXT,  "#d9770622", "#d9770666");
        tagOppFreeze    = makeTag("[Frozen]",  BLUE_TEXT,   "#0e749022", "#0e749066");
        
        tagOppMomentum  = makeTag("[Momentum]", ORANGE, "#d9770622", "#d9770666");
        tagOppFocus     = makeTag("[Focus]", PURPLE, "#7c3aed22", "#7c3aed66");
        tagOppMomentum.setVisible(false); tagOppMomentum.setManaged(false);
        tagOppFocus.setVisible(false); tagOppFocus.setManaged(false);
        
        FlowTags oTags = new FlowTags(tagOppRole, tagOppShield, tagOppConfusion, tagOppFreeze, tagOppMomentum, tagOppFocus);

        lblOppEnergy = labelOf("", 16, GOLD, true);
        barOppEnergy = energyBar(GREEN_DIM);

        btnOppPowerup = actionButton("USE POWERUP", GREEN_DIM, "#ffffff");
        btnOppPowerup.setOnAction(e -> handlePowerup());

        oppCard.getChildren().addAll(
                avatarRow, lblOppType, oTags.box,
                sep(), lblOppEnergy, barOppEnergy,
                sep(), btnOppPowerup);

        VBox legendCard = infoCard("BOARD LEGEND");
        legendCard.getChildren().addAll(
                legendRow("[D]", CELL_DOOR_S[1],   "Scarer Door"),
                legendRow("[L]", CELL_DOOR_L[1],   "Laugher Door"),
                legendRow("[C]", CELL_CARD[1],     "Card Cell"),
                legendRow("[^]", CELL_CONVEYOR[1], "Conveyor Belt"),
                legendRow("[S]", CELL_SOCK[1],     "Contam. Sock"),
                legendRow("[M]", CELL_MONSTER[1],  "Monster Cell"),
                legendRow("[_]", CELL_NORMAL[1],   "Normal Cell"));

        VBox cardInfoCard = infoCard("LAST CARD DRAWN");
        cardVisualBox = new VBox(6);
        cardVisualBox.setAlignment(Pos.CENTER);
        cardVisualBox.setPadding(new Insets(10));
        cardVisualBox.setStyle("-fx-background-color:" + BG_INNER + ";"
                + "-fx-border-color:#7c3aed55;-fx-border-style:dashed;"
                + "-fx-border-radius:10;-fx-background-radius:10;");

        lblLastCardName   = labelOf("Unknown", 18, VIOLET_TEXT, true);
        lblLastCardEffect = labelOf("Land on a Card Cell to reveal your fate.", 14, MUTED, false);
        lblLastCardEffect.setWrapText(true);

        VBox cardVisual = new VBox(4);
        cardVisual.setAlignment(Pos.CENTER);
        cardVisual.setStyle("-fx-background-color:linear-gradient(to bottom right,#4c1d95,#1e1b3a);"
                + "-fx-border-color:#7c3aed;-fx-border-width:1.5;"
                + "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:12;");
        cardVisual.setPrefWidth(70); cardVisual.setMaxWidth(70);
        
        lblCardIcon = new Label("");
        ImageView mysteryIcon = loadIcon("mystery.png", 50);
        if (mysteryIcon != null) {
            lblCardIcon.setGraphic(mysteryIcon);
        }
        lblCardIcon.setFont(Font.font(26));
        cardVisual.getChildren().add(lblCardIcon);

        cardVisualBox.getChildren().addAll(cardVisual, lblLastCardName, lblLastCardEffect);

        HBox pileRow = new HBox();
        pileRow.setAlignment(Pos.CENTER_LEFT);
        pileRow.setPadding(new Insets(6, 0, 0, 0));
        
        lblPileCount = labelOf("Deck: " + getDeckSize() + " cards", 14, VIOLET_TEXT, true);
        lblPileCount.setStyle("-fx-background-color:#7c3aed22;"
                + "-fx-border-color:#7c3aed55;-fx-border-radius:8;-fx-background-radius:8;-fx-padding:3 8;");
        pileRow.getChildren().add(lblPileCount);

        cardInfoCard.getChildren().addAll(cardVisualBox, pileRow);

        Button btnHelp = actionButton("HOW TO PLAY", "#2d2860", "#ffffff");
        btnHelp.setOnAction(e -> showInstructions());
        btnHelp.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnHelp, Priority.ALWAYS);

        Button btnSettings = actionButton("SETTINGS", "#2d2860", "#ffffff");
        btnSettings.setOnAction(e -> showSettings());
        btnSettings.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnSettings, Priority.ALWAYS);

        HBox settingsRow = new HBox(8, btnHelp, btnSettings);
        VBox.setMargin(settingsRow, new Insets(12, 0, 0, 0));

        Button btnExit = new Button("ABANDON GAME");
        btnExit.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 16));
        btnExit.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(btnExit, new Insets(6, 0, 0, 0));
        styleExitButton(btnExit);

        panel.getChildren().addAll(oppCard, legendCard, cardInfoCard, settingsRow, btnExit);
        return panel;
    }

    // ======================================================================
    // BOARD RENDERING
    // ======================================================================
    private void refreshBoard() {
        Monster player   = game.getPlayer();
        Monster opponent = game.getOpponent();
        Cell[][] engineCells = game.getBoard().getBoardCells();

        for (int idx = 0; idx < 100; idx++) {
            StackPane pane = cellPanes[idx];

            Node idxLabel = pane.getChildren().get(0);
            pane.getChildren().clear();
            pane.getChildren().add(idxLabel);

            int boardRow = idx / 10;
            int boardCol = (boardRow % 2 == 1) ? 9 - (idx % 10) : (idx % 10);
            Cell ec = engineCells[boardRow][boardCol];

            String[] colors = CELL_NORMAL;
            String   icon   = "";
            String   info   = "";
            String   tooltip = "Normal Cell " + idx;

            if (idx == 0) {
                colors = CELL_START; icon = "[START]"; tooltip = "Start";
            } else if (idx == 99) {
                colors = CELL_END; icon = "[END]"; info = "BOO'S DOOR"; tooltip = "Boo's Door";
            } else if (ec instanceof DoorCell) {
                DoorCell dc = (DoorCell) ec;
                boolean isScarer = dc.getRole() == Role.SCARER;
                colors  = isScarer ? CELL_DOOR_S : CELL_DOOR_L;
                icon    = isScarer ? "[SCARER]" : "[LAUGH]";
                info    = dc.getEnergy() + " E";
                tooltip = (isScarer ? "SCARER" : "LAUGHER") + " Door  |  Energy: " + dc.getEnergy()
                        + (dc.isActivated() ? "  [Exhausted]" : "  [Fresh]");
                if (dc.isActivated())
                    colors = new String[]{colors[0].replace("0.25", "0.12"), colors[1]};
            } else if (ec instanceof MonsterCell) {
                colors = CELL_MONSTER;
                Monster stationed = ((MonsterCell) ec).getCellMonster();
                String name = stationed != null ? stationed.getName().split(" ")[0] : "Monster";
                icon    = "[MONSTER]";
                info    = name;
                tooltip = "Monster Cell - " + (stationed != null ? stationed.getName() : "empty");
            } else if (ec instanceof CardCell) {
                colors  = CELL_CARD; icon = "[CARD]";
                tooltip = "Card Cell";
            } else if (ec instanceof ConveyorBelt) {
                colors  = CELL_CONVEYOR; icon = "[JUMP]";
                info    = "+" + ((ConveyorBelt) ec).getEffect();
                tooltip = "Conveyor Belt";
            } else if (ec instanceof ContaminationSock) {
                colors  = CELL_SOCK; icon = "[SOCK]";
                info    = "" + ((ContaminationSock) ec).getEffect();
                tooltip = "Contamination Sock";
            }

            pane.setStyle("-fx-background-color:" + colors[0] + ";"
                        + "-fx-border-color:" + colors[1] + ";"
                        + "-fx-border-width:1.5;"
                        + "-fx-border-radius:6;-fx-background-radius:6;");

            Tooltip.install(pane, styledTooltip(tooltip));

            VBox content = new VBox(1);
            content.setAlignment(Pos.TOP_CENTER);
            content.setPadding(new Insets(4, 0, 0, 0)); 

            ImageView iv = loadIcon(cellIconFile(ec, idx), 50);
            if (iv != null) content.getChildren().add(iv);
            else if (!icon.isEmpty()) {
                Label iconLbl = new Label(icon);
                iconLbl.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                content.getChildren().add(iconLbl);
            }

            if (!info.isEmpty()) {
                Label infoLbl = new Label(info);
                infoLbl.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 15));
                infoLbl.setTextFill(Color.web(GOLD));
                infoLbl.setEffect(new DropShadow(3, Color.BLACK));
                content.getChildren().add(infoLbl);
            }

            pane.getChildren().add(content);
            StackPane.setAlignment(content, Pos.TOP_CENTER);

            boolean hasPlayer = (idx == player.getPosition()) && !isAnimating;
            boolean hasOpp    = (idx == opponent.getPosition()) && !isAnimating;
            
            if (hasPlayer || hasOpp) {
                HBox tokens = new HBox(4);
                tokens.setAlignment(Pos.BOTTOM_CENTER);
                tokens.setPadding(new Insets(0, 0, 4, 0));

                if (hasPlayer) {
                    tokens.getChildren().add(buildDynamicAvatar(player, PURPLE, 36));
                }
                if (hasOpp) {
                    tokens.getChildren().add(buildDynamicAvatar(opponent, GREEN_DIM, 36));
                }
                pane.getChildren().add(tokens);
                StackPane.setAlignment(tokens, Pos.BOTTOM_CENTER);
            }
        }
    }

    private String cellIconFile(Cell ec, int idx) {
        if (idx == 0)  return "start.png";
        if (idx == 99) return "boo.png";
        if (ec instanceof DoorCell)
            return ((DoorCell) ec).getRole() == Role.SCARER ? "door_scarer.png" : "door_laugher.png";
        if (ec instanceof MonsterCell) {
            Monster m = ((MonsterCell) ec).getCellMonster();
            String imgF = MONSTER_IMG_MAP.get(m.getName()); return imgF != null ? imgF : "monster.png";
        }
        if (ec instanceof CardCell)    return "card.png";
        if (ec instanceof ConveyorBelt) return "belt.png";
        if (ec instanceof ContaminationSock) return "sock.png";
        return "";
    }

    // ======================================================================
    // STAT PANEL REFRESH 
    // ======================================================================
    private void refreshStats() {
        Monster p = game.getPlayer();
        Monster o = game.getOpponent();
        boolean isPlayer1Turn = (game.getCurrent() == p);

        lblPlayerName.setText(p.getName());
        lblPlayerType.setText(p.getClass().getSimpleName());
        lblPlayerEnergy.setText("Energy:  " + p.getEnergy() + "  /  1000");
        barPlayerEnergy.setProgress(Math.min(1.0, p.getEnergy() / 1000.0));

        if (p.isConfused()) {
            Role originalP = (p.getRole() == Role.SCARER) ? Role.LAUGHER : Role.SCARER;
            tagPlayerRole.setText("Role: " + originalP + " (Now " + p.getRole() + ")");
            tagPlayerRole.setTextFill(Color.web(ORANGE));
        } else {
            tagPlayerRole.setText("Role: " + p.getRole());
            tagPlayerRole.setTextFill(Color.web(BLUE_TEXT));
        }
        
        setTagVisible(tagPlayerShield,    p.isShielded());
        setTagVisible(tagPlayerConfusion, p.isConfused(), "[Confused] (" + p.getConfusionTurns() + ")");
        setTagVisible(tagPlayerFreeze, p.isFrozen());
        
        // Correctly assign Momentum for Dasher and Focus for MultiTasker for Player
        if (p.getClass().getSimpleName().equals("Dasher")) {
            setTagVisible(tagPlayerMomentum, ((Dasher) p).getMomentumTurns() > 0); 
        } else {
            setTagVisible(tagPlayerMomentum, false);
        }
        
        if (p.getClass().getSimpleName().equals("MultiTasker")) {
            setTagVisible(tagPlayerFocus, ((MultiTasker) p).getNormalSpeedTurns() > 0); 
        } else {
            setTagVisible(tagPlayerFocus, false);
        }

        lblOppName.setText(o.getName());
        lblOppType.setText(o.getClass().getSimpleName());
        lblOppEnergy.setText("Energy:  " + o.getEnergy() + "  /  1000");
        barOppEnergy.setProgress(Math.min(1.0, o.getEnergy() / 1000.0));

        if (o.isConfused()) {
            Role originalO = (o.getRole() == Role.SCARER) ? Role.LAUGHER : Role.SCARER;
            tagOppRole.setText("Role: " + originalO + " (Now " + o.getRole() + ")");
            tagOppRole.setTextFill(Color.web(ORANGE));
        } else {
            tagOppRole.setText("Role: " + o.getRole());
            tagOppRole.setTextFill(Color.web(GREEN_TEXT));
        }
        
        setTagVisible(tagOppShield,    o.isShielded());
        setTagVisible(tagOppConfusion, o.isConfused(), "[Confused] (" + o.getConfusionTurns() + ")");
        setTagVisible(tagOppFreeze, o.isFrozen());
        
        // Correctly assign Momentum for Dasher and Focus for MultiTasker for Opponent
        if (o.getClass().getSimpleName().equals("Dasher")) {
            setTagVisible(tagOppMomentum, ((Dasher) o).getMomentumTurns() > 0); 
        } else {
            setTagVisible(tagOppMomentum, false);
        }
        
        if (o.getClass().getSimpleName().equals("MultiTasker")) {
            setTagVisible(tagOppFocus, ((MultiTasker) o).getNormalSpeedTurns() > 0); 
        } else {
            setTagVisible(tagOppFocus, false);
        }

        lblPileCount.setText("Deck: " + getDeckSize() + " cards");

        if (isPlayer1Turn) {
            glowCard(playerCard, true);
            glowCard(oppCard, false);
            styleTurnBadge(lblTurnBadge, p.getName().toUpperCase() + "'S TURN", true);
        } else {
            glowCard(playerCard, false);
            glowCard(oppCard, true);
            styleTurnBadge(lblTurnBadge, o.getName().toUpperCase() + "'S TURN", false);
        }

        boolean p1CanPowerup = p.getEnergy() >= Constants.POWERUP_COST;
        btnPlayerPowerup.setDisable(!isPlayer1Turn || !p1CanPowerup);
        btnPlayerPowerup.setText(p1CanPowerup ? "USE POWERUP (500 E)" : "POWERUP (LOCKED)");

        boolean p2CanPowerup = o.getEnergy() >= Constants.POWERUP_COST;
        btnOppPowerup.setDisable(isPlayer1Turn || !p2CanPowerup);
        btnOppPowerup.setText(p2CanPowerup ? "USE POWERUP (500 E)" : "POWERUP (LOCKED)");

        btnRoll.setDisable(false);
        btnCheatGate.setDisable(false);
        btnCheatEnergy.setDisable(false);
    }

    private void refreshAll() {
        refreshBoard();
        refreshStats();
        checkAndSpawnFloats();
    }

    private void checkAndSpawnFloats() {
        Monster p = game.getPlayer();
        Monster o = game.getOpponent();
        spawnIfChanged(p.getPosition(), p.getEnergy(), lastPlayerEnergy);
        spawnIfChanged(o.getPosition(), o.getEnergy(), lastOppEnergy);
        lastPlayerEnergy = p.getEnergy();
        lastOppEnergy    = o.getEnergy();
    }

    private void spawnIfChanged(int cellIdx, int newVal, int oldVal) {
        if (oldVal == -1 || newVal == oldVal) return;
        int diff = newVal - oldVal;
        spawnFloat(cellPanes[cellIdx],
                   (diff > 0 ? "+" : "") + diff,
                   diff > 0 ? Color.web(GREEN) : Color.web(RED));
    }

    private void spawnFloat(StackPane parent, String text, Color color) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Impact", FontWeight.BOLD, 32));
        lbl.setTextFill(color);
        lbl.setEffect(new DropShadow(5, Color.BLACK));
        StackPane.setAlignment(lbl, Pos.CENTER);
        parent.getChildren().add(lbl);

        TranslateTransition tt = new TranslateTransition(Duration.millis(1300), lbl);
        tt.setByY(-55);
        FadeTransition ft = new FadeTransition(Duration.millis(1300), lbl);
        ft.setFromValue(1.0); ft.setToValue(0.0);
        tt.setOnFinished(e -> parent.getChildren().remove(lbl));
        tt.play(); ft.play();
    }

    private void shakeScreen() {
        Timeline shake = new Timeline(
            new KeyFrame(Duration.millis( 25), e -> mainLayout.setTranslateX(6)),
            new KeyFrame(Duration.millis( 55), e -> mainLayout.setTranslateX(-6)),
            new KeyFrame(Duration.millis( 85), e -> mainLayout.setTranslateY(4)),
            new KeyFrame(Duration.millis(115), e -> mainLayout.setTranslateY(-4)),
            new KeyFrame(Duration.millis(145), e -> mainLayout.setTranslateX(2)),
            new KeyFrame(Duration.millis(175), e -> mainLayout.setTranslateX(-2)),
            new KeyFrame(Duration.millis(200), e -> { mainLayout.setTranslateX(0); mainLayout.setTranslateY(0); })
        );
        shake.play();
    }

    // ======================================================================
    // STRICT ENGINE TURN EXECUTION & 3D DICE ANIMATION
    // ======================================================================
    private void performAnimatedRoll() {
        btnRoll.setDisable(true);
        btnPlayerPowerup.setDisable(true);
        btnOppPowerup.setDisable(true);
        btnCheatGate.setDisable(true);
        btnCheatEnergy.setDisable(true);
        playSound("roll.wav");

        int finalRoll = (int)(Math.random() * 6) + 1;

        ImageView floatingDice = new ImageView();
        floatingDice.setFitWidth(80);
        floatingDice.setFitHeight(80);
        floatingDice.setEffect(new DropShadow(15, Color.BLACK));
        animationLayer.getChildren().add(floatingDice);

        javafx.geometry.Bounds startBounds = diceView.localToScene(diceView.getBoundsInLocal());
        javafx.geometry.Bounds layerStart = animationLayer.sceneToLocal(startBounds);
        double startX = layerStart.getMinX();
        double startY = layerStart.getMinY();

        javafx.geometry.Bounds boardBounds = boardGrid.localToScene(boardGrid.getBoundsInLocal());
        javafx.geometry.Bounds layerBoard = animationLayer.sceneToLocal(boardBounds);
        
        double endX = layerBoard.getMinX() + (layerBoard.getWidth() / 2) - 40 + (Math.random() * 200 - 100);
        double endY = layerBoard.getMinY() + (layerBoard.getHeight() / 2) - 40 + (Math.random() * 200 - 100);

        floatingDice.setLayoutX(startX);
        floatingDice.setLayoutY(startY);

        TranslateTransition slide = new TranslateTransition(Duration.millis(800), floatingDice);
        slide.setToX(endX - startX);
        slide.setToY(endY - startY);
        slide.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

        ScaleTransition bounce = new ScaleTransition(Duration.millis(400), floatingDice);
        bounce.setFromX(1.0); bounce.setFromY(1.0);
        bounce.setToX(2.5); bounce.setToY(2.5);
        bounce.setAutoReverse(true);
        bounce.setCycleCount(2);

        javafx.animation.RotateTransition spin = new javafx.animation.RotateTransition(Duration.millis(800), floatingDice);
        spin.setByAngle(720 + (Math.random() * 360));

        Timeline faceChanger = new Timeline();
        for (int i = 0; i < 16; i++) {
            Duration d = Duration.millis(i * 50);
            faceChanger.getKeyFrames().add(new KeyFrame(d, e -> {
                int face = (int)(Math.random() * 6) + 1;
                Image img = loadImage("dice" + face + ".png");
                if (img != null) floatingDice.setImage(img);
            }));
        }

        faceChanger.getKeyFrames().add(new KeyFrame(Duration.millis(800), e -> {
            Image img = loadImage("dice" + finalRoll + ".png");
            if (img != null) floatingDice.setImage(img);
        }));

        javafx.animation.ParallelTransition throwAnimation = new javafx.animation.ParallelTransition(slide, bounce, spin, faceChanger);

        throwAnimation.setOnFinished(e -> {
            playSound("thud.wav"); 
            // Removed shakeScreen() here to remove vibration on dice roll
            
            Image finalImg = loadImage("dice" + finalRoll + ".png");
            if (finalImg != null) { diceView.setImage(finalImg); diceView.setRotate(0); }
            
            Timeline pause = new Timeline(new KeyFrame(Duration.millis(700), ev -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), floatingDice);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(ev2 -> {
                    animationLayer.getChildren().remove(floatingDice);
                    executeMove(finalRoll);
                });
                fadeOut.play();
            }));
            pause.play();
        });

        throwAnimation.play();
    }

    private void executeMove(int roll) {
        Monster current = game.getCurrent();
        int oldPos = current.getPosition();
        boolean hadShield = current.isShielded(); // TRACK SHIELD STATE BEFORE MOVE

        log(current.getName(), "rolled a " + roll + "!", "neutral");
        
        Card expectedCard = null;
        int deckSizeBefore = 0;
        try {
            if (Board.getCards() != null) {
                if (Board.getCards().isEmpty()) Board.reloadCards();
                if (!Board.getCards().isEmpty()) {
                    expectedCard = Board.getCards().get(0);
                    deckSizeBefore = Board.getCards().size();
                }
            }
        } catch (Exception ignored) {}

        try {
            game.playTurn(roll);
            int newPos = current.getPosition();
            
            int deckSizeAfter = Board.getCards() != null ? Board.getCards().size() : 0;
            // CORRECTED: Deck size logic now correctly detects card draw even if deck reshuffles
            if ((deckSizeAfter < deckSizeBefore || deckSizeAfter > deckSizeBefore + 20) && expectedCard != null) {
                playSound("whoosh.wav"); 
                notifyCardDrawn("[CARD]", expectedCard.getName(), expectedCard.getDescription(), deckSizeAfter);
            }

            // CORRECTED: Verify if shield was consumed to block a hit and display visual feedback
            if (hadShield && !current.isShielded()) {
                log(current.getName(), "blocked an effect with their Shield!", "good");
                spawnFloat(cellPanes[newPos], "BLOCKED!", Color.web(CYAN));
            }
            
            if (oldPos != newPos) {
                isAnimating = true;
                refreshBoard(); 
                
                boolean walkStepByStep = Math.abs(newPos - oldPos) <= 6;
                
                animateMovement(current, oldPos, newPos, walkStepByStep, () -> {
                    isAnimating = false;
                    refreshAll();
                    checkWin();
                });
            } else {
                refreshAll();
                checkWin();
            }
            
        } catch (InvalidMoveException ex) {
            showError("Move Blocked", current.getName() + " had its move reverted or blocked by an obstacle! Roll again.");
            refreshAll();
        } catch (Exception ex) {
            showError("Error", ex.getMessage());
            refreshAll();
        }
    }

    private void handlePowerup() {
        Monster current = game.getCurrent();
        try {
            game.usePowerup();
            playSound("powerup.wav"); shakeScreen();
            log(current.getName(), "activated their powerup!", "good");
            refreshAll();
        } catch (OutOfEnergyException ex) {
            showError("Powerup Failed", "Need at least 500 E to activate.");
        }
    }

    private void handleCheatGate() {
        playSound("whoosh.wav"); shakeScreen();
        Monster current = game.getCurrent();
        try {
            current.setPosition(99);
            log(current.getName(), "teleported to Boo's Door!", "good");
            } catch (Exception e) { showError("Cheat Failed", "Teleportation blocked."); }
        refreshAll(); checkWin();
    }

    private void handleCheatEnergy() {
        playSound("powerup.wav");
        Monster current = game.getCurrent();
        try {
            current.setEnergy(current.getEnergy() + 500);
            log(current.getName(), "used a cheat for +500 Energy!", "good");
            } catch (Exception ex) { showError("Cheat Failed", "Energy boost blocked."); }
        refreshAll();
    }

    private void checkWin() {
        Monster winner = game.getWinner();
        if (winner == null) return;
        playSound("win.wav");
        boolean playerWon = winner == game.getPlayer();
        showWinScreen(winner, playerWon);
    }
    
    // ======================================================================
    // 3D BOARD ANIMATION ENGINE
    // ======================================================================
    private javafx.geometry.Point2D getCellCenter(int idx) {
        StackPane pane = cellPanes[idx];
        javafx.geometry.Bounds bounds = pane.localToScene(pane.getBoundsInLocal());
        javafx.geometry.Bounds layerBounds = animationLayer.sceneToLocal(bounds);
        
        double x = layerBounds.getMinX() + (layerBounds.getWidth() / 2) - 20;
        double y = layerBounds.getMinY() + (layerBounds.getHeight() / 2) - 20;
        return new javafx.geometry.Point2D(x, y);
    }

    private void animateMovement(Monster m, int startIdx, int endIdx, boolean walkStepByStep, Runnable onFinished) {
        playSound("hover.mp3");

        String ringColor = (m == game.getPlayer()) ? PURPLE : GREEN_DIM;
        StackPane actualToken = buildDynamicAvatar(m, ringColor, 40); 
        
        Ellipse shadow = new Ellipse(18, 6);
        shadow.setFill(Color.rgb(0, 0, 0, 0.6));
        shadow.setTranslateY(24); 
        shadow.setEffect(new javafx.scene.effect.GaussianBlur(4));

        StackPane tokenWrapper = new StackPane(shadow, actualToken);

        javafx.geometry.Point2D startPos = getCellCenter(startIdx);
        tokenWrapper.setLayoutX(startPos.getX());
        tokenWrapper.setLayoutY(startPos.getY());
        animationLayer.getChildren().add(tokenWrapper);

        javafx.animation.SequentialTransition masterSequence = new javafx.animation.SequentialTransition();

        if (walkStepByStep) {
            int stepDir = startIdx < endIdx ? 1 : -1;
            int currentIdx = startIdx;

            while (currentIdx != endIdx) {
                currentIdx += stepDir;
                javafx.geometry.Point2D nextPos = getCellCenter(currentIdx);

                TranslateTransition moveGrid = new TranslateTransition(Duration.millis(350), tokenWrapper);
                moveGrid.setToX(nextPos.getX() - startPos.getX());
                moveGrid.setToY(nextPos.getY() - startPos.getY());

                TranslateTransition jumpUp = new TranslateTransition(Duration.millis(175), actualToken);
                jumpUp.setByY(-35); 
                jumpUp.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
                TranslateTransition jumpDown = new TranslateTransition(Duration.millis(175), actualToken);
                jumpDown.setByY(35);
                jumpDown.setInterpolator(javafx.animation.Interpolator.EASE_IN);
                javafx.animation.SequentialTransition jumpY = new javafx.animation.SequentialTransition(jumpUp, jumpDown);

                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(175), actualToken);
                scaleUp.setByX(0.3); scaleUp.setByY(0.3);
                ScaleTransition scaleDown = new ScaleTransition(Duration.millis(175), actualToken);
                scaleDown.setByX(-0.3); scaleDown.setByY(-0.3);
                javafx.animation.SequentialTransition scaleZ = new javafx.animation.SequentialTransition(scaleUp, scaleDown);

                ScaleTransition shadowShrink = new ScaleTransition(Duration.millis(175), shadow);
                shadowShrink.setToX(0.4); shadowShrink.setToY(0.4);
                ScaleTransition shadowGrow = new ScaleTransition(Duration.millis(175), shadow);
                shadowGrow.setToX(1.0); shadowGrow.setToY(1.0);
                javafx.animation.SequentialTransition shadowSeq = new javafx.animation.SequentialTransition(shadowShrink, shadowGrow);

                javafx.animation.ParallelTransition single3DHop = new javafx.animation.ParallelTransition(moveGrid, jumpY, scaleZ, shadowSeq);
                masterSequence.getChildren().add(single3DHop);
            }
            
        } else {
            javafx.geometry.Point2D nextPos = getCellCenter(endIdx);
            
            TranslateTransition moveGrid = new TranslateTransition(Duration.millis(1000), tokenWrapper);
            moveGrid.setToX(nextPos.getX() - startPos.getX());
            moveGrid.setToY(nextPos.getY() - startPos.getY());

            TranslateTransition jumpUp = new TranslateTransition(Duration.millis(500), actualToken);
            jumpUp.setByY(-90);
            jumpUp.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
            TranslateTransition jumpDown = new TranslateTransition(Duration.millis(500), actualToken);
            jumpDown.setByY(90);
            jumpDown.setInterpolator(javafx.animation.Interpolator.EASE_IN);
            javafx.animation.SequentialTransition jumpY = new javafx.animation.SequentialTransition(jumpUp, jumpDown);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(500), actualToken);
            scaleUp.setByX(0.7); scaleUp.setByY(0.7);
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(500), actualToken);
            scaleDown.setByX(-0.7); scaleDown.setByY(-0.7);
            javafx.animation.SequentialTransition scaleZ = new javafx.animation.SequentialTransition(scaleUp, scaleDown);

            ScaleTransition shadowShrink = new ScaleTransition(Duration.millis(500), shadow);
            shadowShrink.setToX(0.2); shadowShrink.setToY(0.2);
            ScaleTransition shadowGrow = new ScaleTransition(Duration.millis(500), shadow);
            shadowGrow.setToX(1.0); shadowGrow.setToY(1.0);
            javafx.animation.SequentialTransition shadowSeq = new javafx.animation.SequentialTransition(shadowShrink, shadowGrow);

            javafx.animation.ParallelTransition huge3DHop = new javafx.animation.ParallelTransition(moveGrid, jumpY, scaleZ, shadowSeq);
            masterSequence.getChildren().add(huge3DHop);
        }

        masterSequence.setOnFinished(e -> {
            animationLayer.getChildren().remove(tokenWrapper);
            onFinished.run();
        });
        
        masterSequence.play();
    }


    // ======================================================================
    // POPUPS
    // ======================================================================
    
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
            ft.setToValue(0); ft.setOnFinished(ev -> rootOverlay.getChildren().remove(overlay)); ft.play();
        });

        layout.getChildren().addAll(head, volLabel, volSlider, ok);
        overlay.getChildren().add(layout);
        
        layout.setOpacity(0); layout.setTranslateY(24);
        rootOverlay.getChildren().add(overlay);
        
        FadeTransition ft = new FadeTransition(Duration.millis(260), layout); ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(260), layout); tt.setToY(0);
        ft.play(); tt.play();
    }

    private void showInstructions() {
        playSound("hover.mp3");
        Stage pop = new Stage();
        pop.initOwner(stage);
        pop.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        pop.initStyle(StageStyle.UTILITY);

        VBox layout = new VBox(18);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(38, 46, 38, 46));
        layout.setStyle("-fx-background-color:linear-gradient(to bottom right,#1a0a2e,#0a0a0f);"
                      + "-fx-border-color:" + GOLD + ";-fx-border-width:2;"
                      + "-fx-border-radius:14;-fx-background-radius:14;"
                      + "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.9),25,0.3,0,8);");

        Label head = new Label("HOW TO PLAY");
        head.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 24));
        head.setTextFill(Color.web(GOLD));
        head.setEffect(new DropShadow(8, Color.web(GOLD, 0.5)));

        String instructions = "1. Roll the dice to move along the 100-cell floor.\n\n"
                            + "2. Collect energy from doors matching your role.\n\n"
                            + "3. Avoid Contamination Socks and rival doors.\n\n"
                            + "4. Reach Boo's Door (Cell 99) with ≥ 1000 Energy to win!";
        Label body = new Label(instructions);
        body.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        body.setTextFill(Color.web(TEXT_LIGHT));
        body.setWrapText(true); body.setMaxWidth(400);

        Button ok = actionButton("GOT IT", GOLD, "#1a0a00");
        ok.setPrefWidth(180);
        ok.setOnAction(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(180), layout);
            ft.setToValue(0); ft.setOnFinished(ev -> pop.close()); ft.play();
        });

        layout.getChildren().addAll(head, body, ok);
        StackPane errRoot = new StackPane(layout);
        errRoot.setStyle("-fx-background-color: #0a0a1a;");
        Scene sc = new Scene(errRoot);
        pop.setTitle("Game Rules");
        fadeInLayout(layout);
        pop.setScene(sc); pop.show();
    }

    private void showWinScreen(Monster winner, boolean playerWon) {
        Stage pop = new Stage();
        pop.initOwner(stage);
        pop.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        pop.initStyle(StageStyle.UTILITY);

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(44, 50, 44, 50));
        layout.setStyle("-fx-background-color:linear-gradient(to bottom right,#1a0a2e,#0a0a0f);"
                      + "-fx-border-color:" + GOLD + ";-fx-border-width:2;"
                      + "-fx-border-radius:16;-fx-background-radius:16;"
                      + "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.9),30,0.3,0,10);");

        Label trophy = new Label(playerWon ? "WINNER" : "GAME OVER");
        trophy.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 28));
        trophy.setTextFill(Color.web(playerWon ? GOLD : RED));

        Label header = new Label(playerWon ? "VICTORY!" : "DEFEAT");
        header.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 40));
        header.setTextFill(Color.web(playerWon ? GOLD : RED));
        header.setEffect(new DropShadow(10, Color.web(playerWon ? GOLD : RED, 0.6)));

        Label detail = new Label(winner.getName() + " (" + winner.getRole() + ") claimed Boo's Door!\n"
                + "Final Energy: " + winner.getEnergy() + " E");
        detail.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        detail.setTextFill(Color.web(TEXT_LIGHT));
        detail.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        detail.setWrapText(true);

        Monster p = game.getPlayer(); Monster o = game.getOpponent();
        Label scores = new Label(p.getName() + ": " + p.getEnergy() + " E    |    " + o.getName() + ": " + o.getEnergy() + " E");
        scores.setFont(Font.font("Arial", 18));
        scores.setTextFill(Color.web(MUTED));

        Button btnBack = actionButton("<-  RETURN TO MENU", GOLD, "#1a0a00");
        btnBack.setPrefWidth(240);
        
        btnBack.setOnAction(e -> {
            pop.close();
            try {
                new Main().start(stage);
            } catch (Exception ex) {
                stage.close();
            }
        });

        layout.getChildren().addAll(trophy, header, detail, scores, btnBack);
        StackPane winRoot = new StackPane(layout);
        winRoot.setStyle("-fx-background-color: #0a0a1a;");
        Scene sc = new Scene(winRoot);
        pop.setTitle("Game Over");
        fadeInLayout(layout);
        pop.setScene(sc); pop.show();
    }

    private void showError(String title, String msg) {
        playSound("error.mp3"); shakeScreen();
        Stage pop = new Stage();
        pop.initOwner(stage);
        pop.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        pop.initStyle(StageStyle.UTILITY);

        VBox layout = new VBox(18);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(38, 46, 38, 46));
        layout.setStyle("-fx-background-color:linear-gradient(to bottom right,#1a0a2e,#0a0a0f);"
                      + "-fx-border-color:" + RED + ";-fx-border-width:2;"
                      + "-fx-border-radius:14;-fx-background-radius:14;"
                      + "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.9),25,0.3,0,8);");

        Label head = new Label("WARNING: " + title.toUpperCase());
        head.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 24));
        head.setTextFill(Color.web(RED));
        head.setEffect(new DropShadow(8, Color.web(RED, 0.5)));

        Label body = new Label(msg);
        body.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        body.setTextFill(Color.web(TEXT_LIGHT));
        body.setWrapText(true); body.setMaxWidth(400);
        body.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button ok = actionButton("CONTINUE", GOLD, "#1a0a00");
        ok.setPrefWidth(180);
        ok.setOnAction(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(180), layout);
            ft.setToValue(0); ft.setOnFinished(ev -> pop.close()); ft.play();
        });

        layout.getChildren().addAll(head, body, ok);
        StackPane errRoot = new StackPane(layout);
        errRoot.setStyle("-fx-background-color: #0a0a1a;");
        Scene sc = new Scene(errRoot);
        pop.setTitle(title);
        fadeInLayout(layout);
        pop.setScene(sc); pop.show();
    }

    private void handleExitPrompt() {
        playSound("error.mp3"); 
        Stage pop = new Stage();
        pop.initOwner(stage);
        pop.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        pop.initStyle(StageStyle.UTILITY);

        VBox layout = new VBox(18);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(38, 46, 38, 46));
        layout.setStyle("-fx-background-color:linear-gradient(to bottom right,#2a0815,#12080a);"
                      + "-fx-border-color:" + RED + ";-fx-border-width:2;"
                      + "-fx-border-radius:14;-fx-background-radius:14;"
                      + "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.9),25,0.3,0,8);");

        Label head = new Label("ABANDON GAME?");
        head.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 26));
        head.setTextFill(Color.web(RED));
        head.setEffect(new DropShadow(8, Color.web(RED, 0.5)));

        Label body = new Label("Are you certain you wish to leave?\nAll progress will be permanently lost.");
        body.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        body.setTextFill(Color.web(TEXT_LIGHT));
        body.setWrapText(true); body.setMaxWidth(400);
        body.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button btnCancel = actionButton("CANCEL", "#374151", "#ffffff");
        btnCancel.setPrefWidth(130);
        btnCancel.setOnAction(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(180), layout);
            ft.setToValue(0); ft.setOnFinished(ev -> pop.close()); ft.play();
        });

        Button btnConfirm = actionButton("YES, QUIT", RED, "#ffffff");
        btnConfirm.setPrefWidth(130);
        btnConfirm.setStyle(btnConfirm.getStyle() + "-fx-effect:dropshadow(three-pass-box," + RED + "aa,10,0.4,0,0);");
        
        btnConfirm.setOnAction(e -> {
            pop.close();
            stage.close();
        });

        HBox btns = new HBox(15, btnCancel, btnConfirm);
        btns.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(head, body, btns);
        
        StackPane rootPane = new StackPane(layout);
        rootPane.setStyle("-fx-background-color: #0a0a1a;");
        Scene sc = new Scene(rootPane);
        pop.setTitle("Quit Game?");
        layout.setOpacity(0); layout.setTranslateY(24);
        FadeTransition ft = new FadeTransition(Duration.millis(260), layout); ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(260), layout); tt.setToY(0);
        ft.play(); tt.play();
        pop.setScene(sc); pop.show();
    }

    public void notifyCardDrawn(String icon, String cardName, String effect, int remainingPile) {
        lblCardIcon.setText(icon);
        lblLastCardName.setText(cardName);
        lblLastCardEffect.setText(effect);
        lblPileCount.setText("Deck: " + remainingPile + " cards");
        log(game.getCurrent().getName(), "drew a card: " + cardName, "neutral");
        
        ScaleTransition st = new ScaleTransition(Duration.millis(300), cardVisualBox);
        st.setFromX(0.85); st.setFromY(0.85);
        st.setToX(1.0); st.setToY(1.0);
        st.play();
    }

    // ======================================================================
    // WIDGET FACTORIES 
    // ======================================================================
    private VBox monsterCard() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color:" + BG_CARD + ";"
                   + "-fx-border-color:#7c3aed44;-fx-border-width:1.5;"
                   + "-fx-border-radius:14;-fx-background-radius:14;");
        return box;
    }

    private VBox infoCard(String title) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color:" + BG_CARD + ";"
                   + "-fx-border-color:#7c3aed33;-fx-border-width:1.5;"
                   + "-fx-border-radius:14;-fx-background-radius:14;");
        Label h = new Label(title);
        h.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 14));
        h.setTextFill(Color.web(PURPLE));
        h.setStyle("-fx-letter-spacing:1.5px;");
        box.getChildren().add(h);
        return box;
    }

    private Label makeTag(String text, String fg, String bg, String border) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        l.setTextFill(Color.web(fg));
        l.setStyle("-fx-background-color:" + bg + ";"
                 + "-fx-border-color:" + border + ";"
                 + "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:2 7;");
        return l;
    }

    private void setTagVisible(Label tag, boolean visible) {
        tag.setVisible(visible); tag.setManaged(visible);
    }
    private void setTagVisible(Label tag, boolean visible, String newText) {
        if (visible) tag.setText(newText);
        setTagVisible(tag, visible);
    }

    private static class FlowTags {
        final HBox box;
        FlowTags(Label... tags) {
            box = new HBox(5);
            box.setAlignment(Pos.CENTER_LEFT);
            box.getChildren().addAll(tags);
        }
    }

    private ProgressBar energyBar(String accent) {
        ProgressBar pb = new ProgressBar(0);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setPrefHeight(9);
        pb.setStyle("-fx-accent:" + accent + ";-fx-control-inner-background:#ffffff11;");
        return pb;
    }

    private Button actionButton(String text, String bg, String fg) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btn.setMaxWidth(Double.MAX_VALUE);
        String base = "-fx-background-color:" + bg + ";-fx-text-fill:" + fg + ";"
                    + "-fx-padding:9 14;-fx-background-radius:10;-fx-border-radius:10;-fx-cursor:hand;";
        String hover = base + "-fx-effect:dropshadow(three-pass-box," + bg + "aa,12,0.4,0,0);";
        
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> { 
            if (!btn.isDisabled()) {
                btn.setStyle(hover); 
            }
        });
        btn.setOnMouseExited(e  -> {
            btn.setStyle(base);
        });
        
        btn.disabledProperty().addListener((obs, oldVal, newVal) -> {
            btn.setStyle(base); 
        });

        return btn;
    }

    private Button smallBtn(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:" + fg + ";"
                 + "-fx-padding:5 10;-fx-background-radius:8;-fx-border-radius:8;-fx-cursor:hand;");
        return b;
    }

    private HBox legendRow(String icon, String dotColor, String label) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label dot = new Label("  ");
        dot.setStyle("-fx-background-color:" + dotColor + ";"
                   + "-fx-border-radius:3;-fx-background-radius:3;");
        dot.setPrefSize(10, 10); dot.setMaxSize(10, 10);
        Label ico = new Label(icon);
        Label txt = new Label(label);
        txt.setFont(Font.font("Arial", 14)); txt.setTextFill(Color.web(MUTED));
        row.getChildren().addAll(dot, ico, txt);
        return row;
    }

    private Label panelLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 16));
        l.setTextFill(Color.web(PURPLE));
        l.setStyle("-fx-letter-spacing:2px;");
        return l;
    }

    private Label labelOf(String text, int size, String hex, boolean bold) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", bold ? FontWeight.EXTRA_BOLD : FontWeight.NORMAL, size));
        l.setTextFill(Color.web(hex));
        l.setWrapText(true);
        return l;
    }

    private Region sep() {
        Region r = new Region(); r.setPrefHeight(1);
        r.setStyle("-fx-background-color:#7c3aed22;");
        return r;
    }

    private Label tokenDot(String color) {
        Label l = new Label(" ");
        l.setPrefSize(16, 16); l.setMaxSize(16, 16);
        l.setStyle("-fx-background-color:" + color + ";-fx-border-color:white;"
                 + "-fx-border-width:1.5;-fx-border-radius:8;-fx-background-radius:8;");
        return l;
    }

    private Tooltip styledTooltip(String text) {
        Tooltip t = new Tooltip(text);
        t.setFont(Font.font("Arial", 15));
        return t;
    }

    private void glowCard(VBox card, boolean active) {
        if (active) {
            card.setStyle("-fx-background-color:" + BG_CARD + ";"
                        + "-fx-border-color:" + GOLD + ";-fx-border-width:2;"
                        + "-fx-border-radius:14;-fx-background-radius:14;"
                        + "-fx-effect:dropshadow(three-pass-box," + GOLD + "55,18,0.4,0,0);");
        } else {
            card.setStyle("-fx-background-color:" + BG_CARD + ";"
                        + "-fx-border-color:#7c3aed44;-fx-border-width:1.5;"
                        + "-fx-border-radius:14;-fx-background-radius:14;-fx-effect:none;");
        }
    }

    private void styleTurnBadge(Label lbl, String text, boolean isP1) {
        lbl.setText("" + text);
        if (isP1) {
            lbl.setStyle("-fx-background-color:" + GOLD + ";-fx-text-fill:#1a0a00;"
                       + "-fx-border-radius:20;-fx-background-radius:20;-fx-padding:5 16;"
                       + "-fx-font-weight:bold;-fx-font-family:Arial;");
        } else {
            lbl.setStyle("-fx-background-color:" + GREEN + ";-fx-text-fill:#1a0a00;"
                       + "-fx-border-radius:20;-fx-background-radius:20;-fx-padding:5 16;"
                       + "-fx-font-weight:bold;-fx-font-family:Arial;");
        }
    }

    private void styleExitButton(Button btn) {
        String def = "-fx-background-color:linear-gradient(to right," + RED_DIM + "," + RED + ");"
                   + "-fx-text-fill:white;-fx-padding:11;-fx-border-radius:10;-fx-background-radius:10;"
                   + "-fx-border-color:#f87171;-fx-border-width:1.5;-fx-cursor:hand;";
        String hov = def + "-fx-effect:dropshadow(three-pass-box," + RED + "88,14,0.5,0,0);";
        btn.setStyle(def);
        btn.setOnMouseEntered(e -> { btn.setStyle(hov); });
        btn.setOnMouseExited(e  -> btn.setStyle(def));
        ImageView ic = loadIcon("exit.png", 20); 
        if (ic != null) btn.setGraphic(ic);
        
        btn.setOnAction(e -> handleExitPrompt());
    }

    private void fadeInLayout(VBox layout) {
        layout.setOpacity(0); layout.setTranslateY(24);
        FadeTransition ft = new FadeTransition(Duration.millis(260), layout); ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(260), layout); tt.setToY(0);
        ft.play(); tt.play();
    }

    private void log(String playerName, String action, String type) {
        String fullMsg = playerName.toUpperCase() + " " + action;
        
        Label l = new Label("> " + fullMsg);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        l.setWrapText(true);
        
        switch (type) {
            case "good": l.setTextFill(Color.web(GREEN)); break;
            case "bad":  l.setTextFill(Color.web(RED)); break;
            default:     l.setTextFill(Color.web(VIOLET_TEXT)); break;
        }
        
        logBox.getChildren().add(0, l);
        if (logBox.getChildren().size() > 60) logBox.getChildren().remove(60);

        showToast(fullMsg, type);
    }

    private void showToast(String message, String type) {
        Label toast = new Label(message);
        toast.setFont(Font.font("Arial Black", FontWeight.BOLD, 18));
        toast.setTextFill(Color.WHITE);
        toast.setPadding(new Insets(12, 25, 12, 25));

        String bgColor = type.equals("good") ? "#10b981ee" : 
                         type.equals("bad") ? "#ef4444ee" : "#7c3aedee";

        toast.setStyle("-fx-background-color: " + bgColor + ";"
                     + "-fx-background-radius: 30; -fx-border-radius: 30;"
                     + "-fx-border-color: #ffffff55; -fx-border-width: 2;"
                     + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 15, 0.2, 0, 5);");

        StackPane.setAlignment(toast, Pos.TOP_CENTER);
        StackPane.setMargin(toast, new Insets(50, 0, 0, 0));

        toast.setMouseTransparent(true); 
        rootOverlay.getChildren().add(toast);

        toast.setTranslateY(-50);
        toast.setOpacity(0);

        TranslateTransition ttIn = new TranslateTransition(Duration.millis(300), toast);
        ttIn.setToY(0);
        FadeTransition ftIn = new FadeTransition(Duration.millis(300), toast);
        ftIn.setToValue(1.0);

        TranslateTransition ttOut = new TranslateTransition(Duration.millis(300), toast);
        ttOut.setToY(-50);
        ttOut.setDelay(Duration.millis(2000)); 
        
        FadeTransition ftOut = new FadeTransition(Duration.millis(300), toast);
        ftOut.setToValue(0.0);
        ftOut.setDelay(Duration.millis(2000));

        ftOut.setOnFinished(e -> rootOverlay.getChildren().remove(toast));

        ttIn.play(); ftIn.play();
        ttOut.play(); ftOut.play();
    }
}