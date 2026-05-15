package game.gui;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.scene.media.AudioClip;
import javafx.scene.effect.DropShadow;

import java.io.File;

import game.engine.Board;
import game.engine.Game;
import game.engine.Role;
import game.engine.monsters.Monster;
import game.engine.cells.*;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;

public class GameWindow {

    // ── PALETTE ────────────────────────────────────────────────────────────
    private static final String BG_APP        = "#0a0a1a";
    private static final String BG_CARD       = "linear-gradient(to bottom right, #12082a, #1a1040)";
    private static final String BG_BOARD      = "#0d0d22";
    private static final String BG_CONTROLS   = "#12082a";
    private static final String BG_INNER      = "#ffffff08";

    private static final String PURPLE        = "#7c3aed";
    private static final String PURPLE_DIM    = "#7c3aed22";
    private static final String PURPLE_BORDER = "#7c3aed44";
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
    private static final String TEXT_DIM      = "#6b7280";

    // ── CELL COLOURS (bg / border) ─────────────────────────────────────────
    private static final String[] CELL_NORMAL   = {"#1e1b3a", "#2d2860"};
    private static final String[] CELL_DOOR_S   = {"rgba(30,58,138,0.25)", "#1e3a8a"};
    private static final String[] CELL_DOOR_L   = {"rgba(20,83,45,0.25)",  "#22c55e"};
    private static final String[] CELL_CARD     = {"rgba(220,38,38,0.25)", "#f87171"};
    private static final String[] CELL_CONVEYOR = {"rgba(5,150,105,0.25)", "#34d399"};
    private static final String[] CELL_SOCK     = {"rgba(217,119,6,0.25)", "#fbbf24"};
    private static final String[] CELL_MONSTER  = {"rgba(124,58,237,0.25)","#a78bfa"};
    private static final String[] CELL_START    = {"rgba(107,114,128,0.2)","#9ca3af"};
    private static final String[] CELL_END      = {"rgba(251,191,36,0.2)", "#fbbf24"};

    // ── STATE ──────────────────────────────────────────────────────────────
    private final Stage stage;
    private Game game;
    private BorderPane mainLayout;

    private final StackPane[] cellPanes = new StackPane[100];
    private GridPane boardGrid;

    private Label lblTurnBadge;
    private VBox playerCard, oppCard;
    private Label lblPlayerName, lblPlayerType, lblPlayerEnergy;
    private Label tagPlayerRole, tagPlayerShield, tagPlayerConfusion, tagPlayerFreeze;
    private ProgressBar barPlayerEnergy;
    private Button btnPlayerPowerup, btnRoll, btnCheatGate, btnCheatEnergy;
    
    private Label lblOppName, lblOppType, lblOppEnergy;
    private Label tagOppRole, tagOppShield, tagOppConfusion, tagOppFreeze;
    private ProgressBar barOppEnergy;
    private Button btnOppPowerup;

    private ImageView diceView;
    private VBox logBox, cardVisualBox;
    private Label lblCardIcon, lblLastCardName, lblLastCardEffect, lblPileCount;

    private int lastPlayerEnergy = -1, lastOppEnergy = -1;

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
            java.lang.reflect.Method getDeck = game.getClass().getMethod("getDeck");
            java.util.AbstractCollection<?> deck = (java.util.AbstractCollection<?>) getDeck.invoke(game);
            return deck.size();
        } catch (Exception e1) {
            try {
                Object board = game.getBoard();
                java.lang.reflect.Method getDeck = board.getClass().getMethod("getDeck");
                java.util.AbstractCollection<?> deck = (java.util.AbstractCollection<?>) getDeck.invoke(board);
                return deck.size();
            } catch (Exception e2) {
                return 24; 
            }
        }
    }

    private void playSound(String f) {
        try {
            File file = new File("assets/" + f);
            if (file.exists()) new AudioClip(file.toURI().toString()).play();
        } catch (Exception ignored) {}
    }

    private Image loadImage(String f) {
        try {
            File file = new File("assets/" + f);
            if (file.exists()) return new Image(file.toURI().toString());
        } catch (Exception ignored) {}
        return null;
    }

    private ImageView loadIcon(String f, int size) {
        Image img = loadImage(f);
        if (img == null) return null;
        ImageView iv = new ImageView(img);
        iv.setFitWidth(size); iv.setFitHeight(size);
        iv.setPreserveRatio(true);
        return iv;
    }

    // ── DYNAMIC AVATAR BUILDER ─────────────────────────────────────────────
    private StackPane buildDynamicAvatar(Monster m, String ringColor, int size) {
        StackPane sp = new StackPane();
        sp.setPrefSize(size + 14, size + 14); 
        sp.setMaxSize(size + 14, size + 14);
        sp.setStyle("-fx-background-color: #111111; " +
                    "-fx-border-color: " + ringColor + "; " +
                    "-fx-border-width: 2.5; " +
                    "-fx-border-radius: 50%; " +
                    "-fx-background-radius: 50%; " + 
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 3);");

        String dynamicImgName = m.getClass().getSimpleName().toLowerCase() + ".png";
        ImageView iv = loadIcon(dynamicImgName, size);
        
        if (iv != null) {
            Circle clip = new Circle(size/2.0, size/2.0, size/2.0);
            iv.setClip(clip);
            sp.getChildren().add(iv);
        } else {
            Label fallback = new Label(m.getName().substring(0, 1).toUpperCase());
            fallback.setFont(Font.font("Arial Black", FontWeight.BOLD, size/1.5));
            fallback.setTextFill(Color.web(ringColor));
            sp.getChildren().add(fallback);
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

        Scene scene = new Scene(root, 1380, 870);
        stage.setMinWidth(1100); stage.setMinHeight(720);
        stage.setScene(scene);
    }

    private HBox buildTopBar() {
        HBox bar = new HBox(14);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(9, 22, 9, 22));
        bar.setStyle("-fx-background-color:linear-gradient(to right,#1e0a3c,#0d1f3c,#1e0a3c);"
                   + "-fx-border-color:#7c3aed44;-fx-border-width:0 0 2 0;");

        Label title = new Label("🚪 DooR DasH");
        title.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 26));
        title.setTextFill(Color.web(GOLD));
        title.setEffect(new DropShadow(10, Color.web(ORANGE, 0.7)));

        Label sub = new Label("SCARE VS LAUGH TOUCHDOWN");
        sub.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        sub.setTextFill(Color.web(MUTED));

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        lblTurnBadge = new Label("TURN INFO");
        styleTurnBadge(lblTurnBadge, false);

        bar.getChildren().addAll(title, sub, spacer, lblTurnBadge);
        return bar;
    }

    private VBox buildLeftPanel() {
        VBox panel = new VBox(12);
        panel.setPrefWidth(260);
        panel.setPadding(new Insets(18));

        panel.getChildren().add(panelLabel("⚡ YOUR MONSTER"));

        playerCard = monsterCard();

        HBox avatarRow = new HBox(12);
        avatarRow.setAlignment(Pos.CENTER_LEFT);
        
        // --- INCREASED SIZE TO 48 ---
        StackPane avatar = buildDynamicAvatar(game.getPlayer(), PURPLE, 48);
        
        lblPlayerName = labelOf("", 17, TEXT_LIGHT, true);
        VBox nameCol = new VBox(2, lblPlayerName);
        avatarRow.getChildren().addAll(avatar, nameCol);

        lblPlayerType = labelOf("", 11, MUTED, false);

        tagPlayerRole      = makeTag("", BLUE_TEXT,  "#1e3a8a33", "#1e3a8a66");
        tagPlayerShield    = makeTag("🛡 Shield",  CYAN,        "#0e749022", "#0e749066");
        tagPlayerConfusion = makeTag("😵 Confused",AMBER_TEXT,  "#d9770622", "#d9770666");
        tagPlayerFreeze    = makeTag("❄ Frozen",  BLUE_TEXT,   "#0e749022", "#0e749066");
        
        FlowTags pTags = new FlowTags(tagPlayerRole, tagPlayerShield, tagPlayerConfusion, tagPlayerFreeze);

        lblPlayerEnergy = labelOf("", 12, GOLD, true);
        barPlayerEnergy = energyBar(PURPLE);

        btnPlayerPowerup = actionButton("⚡ USE POWERUP  (500 ⚡)", PURPLE, "#ffffff");
        btnPlayerPowerup.setOnAction(e -> handlePowerup());

        Region div = new Region(); div.setPrefHeight(1);
        div.setStyle("-fx-background-color:#7c3aed22;");

        btnCheatGate   = smallBtn("⚡ TELEPORT",   "#2d2860", "#c4b5fd");
        btnCheatEnergy = smallBtn("💠 +500 ENERGY", "#2d2860", "#c4b5fd");
        btnCheatGate.setOnAction(e -> handleCheatGate());
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
            num.setFont(Font.font("Arial", FontWeight.BOLD, 8));
            num.setTextFill(Color.web("#ffffff44"));
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
        diceView.setFitWidth(54); diceView.setFitHeight(54);
        Image di = loadImage("dice6.png");
        if (di != null) diceView.setImage(di);

        btnRoll = actionButton("🎲  ROLL DICE", GOLD, "#1a0a00");
        btnRoll.setPrefHeight(48);
        btnRoll.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 15));
        btnRoll.setOnAction(e -> handleRollDice());

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
        panel.setPrefWidth(260);
        panel.setPadding(new Insets(18));

        panel.getChildren().add(panelLabel("🎯 OPPONENT"));

        oppCard = monsterCard();

        HBox avatarRow = new HBox(12);
        avatarRow.setAlignment(Pos.CENTER_LEFT);
        
        // --- INCREASED SIZE TO 48 ---
        StackPane avatar = buildDynamicAvatar(game.getOpponent(), GREEN_DIM, 48);
        
        lblOppName = labelOf("", 17, TEXT_LIGHT, true);
        VBox nameCol = new VBox(2, lblOppName);
        avatarRow.getChildren().addAll(avatar, nameCol);

        lblOppType = labelOf("", 11, MUTED, false);

        tagOppRole      = makeTag("", GREEN_TEXT,  "#14532d33", "#14532d66");
        tagOppShield    = makeTag("🛡 Shield",  CYAN,        "#0e749022", "#0e749066");
        tagOppConfusion = makeTag("😵 Confused",AMBER_TEXT,  "#d9770622", "#d9770666");
        tagOppFreeze    = makeTag("❄ Frozen",  BLUE_TEXT,   "#0e749022", "#0e749066");
        
        FlowTags oTags = new FlowTags(tagOppRole, tagOppShield, tagOppConfusion, tagOppFreeze);

        lblOppEnergy = labelOf("", 12, GOLD, true);
        barOppEnergy = energyBar(GREEN_DIM);

        btnOppPowerup = actionButton("⚡ POWERUP  (LOCKED)", TEXT_DIM, "#6b7280");
        btnOppPowerup.setDisable(true);
        btnOppPowerup.setStyle(btnOppPowerup.getStyle()
                + "-fx-background-color:#1e1b3a;-fx-border-color:#2d2860;");

        oppCard.getChildren().addAll(
                avatarRow, lblOppType, oTags.box,
                sep(), lblOppEnergy, barOppEnergy,
                sep(), btnOppPowerup);

        VBox legendCard = infoCard("BOARD LEGEND");
        legendCard.getChildren().addAll(
                legendRow("🚪", CELL_DOOR_S[1],   "Scarer Door"),
                legendRow("🎭", CELL_DOOR_L[1],   "Laugher Door"),
                legendRow("🃏", CELL_CARD[1],     "Card Cell"),
                legendRow("⬆",  CELL_CONVEYOR[1], "Conveyor Belt"),
                legendRow("🧦", CELL_SOCK[1],     "Contamination Sock"),
                legendRow("👾", CELL_MONSTER[1],  "Monster Cell"),
                legendRow("⬜", CELL_NORMAL[1],   "Normal Cell"));

        VBox cardInfoCard = infoCard("LAST CARD DRAWN");
        cardVisualBox = new VBox(6);
        cardVisualBox.setAlignment(Pos.CENTER);
        cardVisualBox.setPadding(new Insets(10));
        cardVisualBox.setStyle("-fx-background-color:" + BG_INNER + ";"
                + "-fx-border-color:#7c3aed55;-fx-border-style:dashed;"
                + "-fx-border-radius:10;-fx-background-radius:10;");

        lblLastCardName   = labelOf("Unknown", 13, VIOLET_TEXT, true);
        lblLastCardEffect = labelOf("Land on a Card Cell to reveal your fate.", 10, MUTED, false);
        lblLastCardEffect.setWrapText(true);

        VBox cardVisual = new VBox(4);
        cardVisual.setAlignment(Pos.CENTER);
        cardVisual.setStyle("-fx-background-color:linear-gradient(to bottom right,#4c1d95,#1e1b3a);"
                + "-fx-border-color:#7c3aed;-fx-border-width:1.5;"
                + "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:12;");
        cardVisual.setPrefWidth(60); cardVisual.setMaxWidth(60);
        
        lblCardIcon = new Label("❔");
        lblCardIcon.setFont(Font.font(22));
        cardVisual.getChildren().add(lblCardIcon);

        cardVisualBox.getChildren().addAll(cardVisual, lblLastCardName, lblLastCardEffect);

        HBox pileRow = new HBox();
        pileRow.setAlignment(Pos.CENTER_LEFT);
        pileRow.setPadding(new Insets(6, 0, 0, 0));
        
        int currentDeckSize = 24;
        try { currentDeckSize = getDeckSize(); } catch (Exception ignored) {}
        
        lblPileCount = labelOf("📚 Pile: " + currentDeckSize + " cards", 10, VIOLET_TEXT, true);
        lblPileCount.setStyle("-fx-background-color:#7c3aed22;"
                + "-fx-border-color:#7c3aed55;-fx-border-radius:8;-fx-background-radius:8;-fx-padding:3 8;");
        pileRow.getChildren().add(lblPileCount);

        cardInfoCard.getChildren().addAll(cardVisualBox, pileRow);

        Button btnExit = new Button("⚠  ABANDON GAME");
        btnExit.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 13));
        btnExit.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(btnExit, new Insets(12, 0, 0, 0));
        styleExitButton(btnExit);

        panel.getChildren().addAll(oppCard, legendCard, cardInfoCard, btnExit);
        return panel;
    }

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
                colors = CELL_START; icon = "🚦"; tooltip = "Start";
            } else if (idx == 99) {
                colors = CELL_END; icon = "🏆"; info = "BOO'S DOOR"; tooltip = "Boo's Door — Victory!";
            } else if (ec instanceof DoorCell) {
                DoorCell dc = (DoorCell) ec;
                boolean isScarer = dc.getRole() == Role.SCARER;
                colors  = isScarer ? CELL_DOOR_S : CELL_DOOR_L;
                icon    = isScarer ? "🚪" : "🎭";
                info    = dc.getEnergy() + " ⚡";
                tooltip = (isScarer ? "SCARER" : "LAUGHER") + " Door  |  Energy: " + dc.getEnergy()
                        + (dc.isActivated() ? "  [Exhausted]" : "  [Fresh]");
                if (dc.isActivated())
                    colors = new String[]{colors[0].replace("0.25", "0.12"), colors[1]};
            } else if (ec instanceof MonsterCell) {
                colors = CELL_MONSTER;
                Monster stationed = ((MonsterCell) ec).getCellMonster();
                String name = stationed != null ? stationed.getName().split(" ")[0] : "Monster";
                icon    = "👾";
                info    = name;
                tooltip = "Monster Cell — " + (stationed != null ? stationed.getName() : "empty");
            } else if (ec instanceof CardCell) {
                colors  = CELL_CARD; icon = "🃏";
                tooltip = "Card Cell — draw a random card";
            } else if (ec instanceof ConveyorBelt) {
                colors  = CELL_CONVEYOR; icon = "⬆";
                info    = "+" + ((ConveyorBelt) ec).getEffect();
                tooltip = "Conveyor Belt — jump +" + ((ConveyorBelt) ec).getEffect() + " cells";
            } else if (ec instanceof ContaminationSock) {
                colors  = CELL_SOCK; icon = "🧦";
                info    = "" + ((ContaminationSock) ec).getEffect();
                tooltip = "Contamination Sock — " + ((ContaminationSock) ec).getEffect() + " cells, −100 ⚡";
            }

            pane.setStyle("-fx-background-color:" + colors[0] + ";"
                        + "-fx-border-color:" + colors[1] + ";"
                        + "-fx-border-width:1.5;"
                        + "-fx-border-radius:6;-fx-background-radius:6;");

            Tooltip.install(pane, styledTooltip(tooltip));

            VBox content = new VBox(1);
            content.setAlignment(Pos.TOP_CENTER);
            content.setPadding(new Insets(6, 0, 0, 0));

            ImageView iv = loadIcon(cellIconFile(ec, idx), 32);
            if (iv != null) content.getChildren().add(iv);
            else if (!icon.isEmpty()) {
                Label iconLbl = new Label(icon);
                iconLbl.setFont(Font.font(14));
                content.getChildren().add(iconLbl);
            }

            if (!info.isEmpty()) {
                Label infoLbl = new Label(info);
                infoLbl.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 9));
                infoLbl.setTextFill(Color.web(GOLD));
                infoLbl.setEffect(new DropShadow(3, Color.BLACK));
                content.getChildren().add(infoLbl);
            }

            pane.getChildren().add(content);
            StackPane.setAlignment(content, Pos.TOP_CENTER);

            // --- DYNAMIC TOKENS INCREASED TO 28 ---
            boolean hasPlayer = (idx == player.getPosition());
            boolean hasOpp    = (idx == opponent.getPosition());
            if (hasPlayer || hasOpp) {
                HBox tokens = new HBox(4);
                tokens.setAlignment(Pos.BOTTOM_CENTER);
                tokens.setPadding(new Insets(0, 0, 4, 0));

                if (hasPlayer) {
                    tokens.getChildren().add(buildDynamicAvatar(player, PURPLE, 26));
                }
                if (hasOpp) {
                    tokens.getChildren().add(buildDynamicAvatar(opponent, GREEN_DIM, 26));
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
            return m != null ? m.getClass().getSimpleName().toLowerCase() + ".png" : "monster.png";
        }
        if (ec instanceof CardCell)    return "card.png";
        if (ec instanceof ConveyorBelt) return "belt.png";
        if (ec instanceof ContaminationSock) return "sock.png";
        return "";
    }

    private void refreshStats() {
        Monster p = game.getPlayer();
        Monster o = game.getOpponent();
        boolean myTurn = (game.getCurrent() == p);

        lblPlayerName.setText(p.getName());
        lblPlayerType.setText(p.getClass().getSimpleName());
        lblPlayerEnergy.setText("Energy:  " + p.getEnergy() + "  /  1000");
        barPlayerEnergy.setProgress(Math.min(1.0, p.getEnergy() / 1000.0));

        tagPlayerRole.setText("Role: " + p.getRole());
        setTagVisible(tagPlayerShield,    p.isShielded());
        setTagVisible(tagPlayerConfusion, p.isConfused(), "😵 Confused");
        setTagVisible(tagPlayerFreeze, p.isFrozen());

        lblOppName.setText(o.getName());
        lblOppType.setText(o.getClass().getSimpleName());
        lblOppEnergy.setText("Energy:  " + o.getEnergy() + "  /  1000");
        barOppEnergy.setProgress(Math.min(1.0, o.getEnergy() / 1000.0));

        tagOppRole.setText("Role: " + o.getRole());
        setTagVisible(tagOppShield,    o.isShielded());
        setTagVisible(tagOppConfusion, o.isConfused(), "😵 Confused");
        setTagVisible(tagOppFreeze, o.isFrozen());

        int currentDeckSize = 0;
        try { currentDeckSize = getDeckSize(); } catch (Exception ignored) {}
        lblPileCount.setText("📚 Pile: " + currentDeckSize + " cards");

        if (myTurn) {
            glowCard(playerCard, true);
            glowCard(oppCard, false);
            styleTurnBadge(lblTurnBadge, true);
        } else {
            glowCard(playerCard, false);
            glowCard(oppCard, true);
            styleTurnBadge(lblTurnBadge, false);
        }

        btnRoll.setDisable(!myTurn);
        btnPlayerPowerup.setDisable(!myTurn || p.getEnergy() < 500);
        btnCheatGate.setDisable(!myTurn);
        btnCheatEnergy.setDisable(!myTurn);
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
        lbl.setFont(Font.font("Impact", FontWeight.BOLD, 20));
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

    private void handleRollDice() {
        performAnimatedRoll(true);
    }

    private void performAnimatedRoll(boolean isPlayer) {
        btnRoll.setDisable(true);
        btnPlayerPowerup.setDisable(true);
        btnCheatGate.setDisable(true);
        btnCheatEnergy.setDisable(true);
        playSound("roll.wav");

        int[] finalRoll = {(int)(Math.random() * 6) + 1};
        try {
            java.lang.reflect.Method rm = game.getClass().getDeclaredMethod("rollDice");
            rm.setAccessible(true);
            finalRoll[0] = (int) rm.invoke(game);
        } catch (Exception ignored) {}

        Timeline anim = new Timeline();
        for (int i = 0; i < 14; i++) {
            Duration d = Duration.millis(i * 45);
            anim.getKeyFrames().add(new KeyFrame(d, e -> {
                int face = (int)(Math.random() * 6) + 1;
                Image img = loadImage("dice" + face + ".png");
                if (img != null) { diceView.setImage(img); diceView.setRotate(Math.random() * 360); }
            }));
        }
        anim.getKeyFrames().add(new KeyFrame(Duration.millis(660), e -> {
            playSound("thud.wav"); shakeScreen();
            Image img = loadImage("dice" + finalRoll[0] + ".png");
            if (img != null) { diceView.setImage(img); diceView.setRotate(0); }
            executeMove(isPlayer, finalRoll[0]);
        }));
        anim.play();
    }

    private void executeMove(boolean isPlayer, int roll) {
        Monster current  = isPlayer ? game.getPlayer() : game.getOpponent();
        Monster other    = isPlayer ? game.getOpponent() : game.getPlayer();

        log((isPlayer ? "You" : "Opponent") + " rolled " + roll, "neutral");

        Object topCard = null;
        try {
            java.util.List<?> deck = Board.getCards();
            if (deck != null && !deck.isEmpty()) {
                topCard = deck.get(0);
            }
        } catch (Exception ignored) {}

        try {
            if (current.isFrozen()) {
                current.setFrozen(false);
                log(current.getName() + " was frozen — skipping turn.", "bad");
            } else {
                game.getBoard().moveMonster(current, roll, other);
                
                int newPos = current.getPosition();
                int boardRow = newPos / 10;
                int boardCol = (boardRow % 2 == 1) ? 9 - (newPos % 10) : (newPos % 10);
                Cell landedCell = game.getBoard().getBoardCells()[boardRow][boardCol];
                
                if (landedCell instanceof CardCell) {
                    playSound("whoosh.wav"); 
                    
                    String cName = "Unknown Card";
                    String cDesc = "A mysterious effect occurred.";
                    if (topCard != null) {
                        try {
                            java.lang.reflect.Method getName = topCard.getClass().getMethod("getName");
                            cName = (String) getName.invoke(topCard);
                        } catch (Exception ignored) {}
                        
                        try {
                            java.lang.reflect.Method getDesc = topCard.getClass().getMethod("getDescription");
                            cDesc = (String) getDesc.invoke(topCard);
                        } catch (Exception ignored) {
                            try {
                                java.lang.reflect.Method getEffect = topCard.getClass().getMethod("getEffect");
                                cDesc = (String) getEffect.invoke(topCard);
                            } catch (Exception ignored2) {}
                        }
                    }
                    
                    int remaining = 0;
                    try { remaining = getDeckSize(); } catch(Exception ignored){}
                    notifyCardDrawn("🃏", cName, cDesc, remaining);
                }
            }
            game.setCurrent(other);
        } catch (InvalidMoveException ex) {
            if (isPlayer) {
                showError("Move Blocked", "Your opponent occupies that cell. Roll again.");
                refreshAll(); return;
            } else {
                log("Opponent move blocked — retrying...", "bad");
                triggerOpponentTurn(); return;
            }
        } catch (Exception ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof InvalidMoveException) {
                if (isPlayer) {
                    showError("Move Blocked", "Your opponent occupies that cell. Roll again.");
                    refreshAll(); return;
                } else { triggerOpponentTurn(); return; }
            }
            if (isPlayer) {
                showError("Error", ex.getMessage());
                refreshAll();
                return;
            }
        }

        refreshAll();
        checkWin();

        if (game.getWinner() == null && game.getCurrent() == game.getOpponent()) {
            triggerOpponentTurn();
        }
    }

    private void handlePowerup() {
        try {
            game.usePowerup();
            playSound("powerup.wav"); shakeScreen();
            log("You activated your powerup!", "good");
            refreshAll();
            if (game.getCurrent() == game.getOpponent()) triggerOpponentTurn();
        } catch (OutOfEnergyException ex) {
            showError("Powerup Failed", "Need at least 500 ⚡ to activate.");
        }
    }

    private void handleCheatGate() {
        playSound("whoosh.wav"); shakeScreen();
        try {
            game.getPlayer().setPosition(99);
            log("Cheat: Teleported to Boo's Door!", "good");
        } catch (Exception e) { showError("Cheat Failed", "Teleportation blocked."); }
        refreshAll(); checkWin();
    }

    private void handleCheatEnergy() {
        playSound("powerup.wav");
        try {
            game.getPlayer().setEnergy(game.getPlayer().getEnergy() + 500);
            log("Cheat: +500 Energy!", "good");
        } catch (Exception ex) { showError("Cheat Failed", "Energy boost blocked."); }
        refreshAll();
    }

    private void triggerOpponentTurn() {
        log("Opponent thinking...", "neutral");
        PauseTransition pause = new PauseTransition(Duration.seconds(1.1));
        pause.setOnFinished(ev -> {
            try {
                if (game.getOpponent().getEnergy() >= 500) {
                    game.usePowerup();
                    playSound("powerup.wav"); shakeScreen();
                    log("Opponent used a powerup!", "bad");
                    refreshAll();
                }
            } catch (Exception ignored) {}
            performAnimatedRoll(false);
        });
        pause.play();
    }

    private void checkWin() {
        Monster winner = game.getWinner();
        if (winner == null) return;
        playSound("win.wav");
        boolean playerWon = winner == game.getPlayer();
        showWinScreen(winner, playerWon);
    }

    private void showWinScreen(Monster winner, boolean playerWon) {
        Stage pop = new Stage();
        pop.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        pop.initStyle(StageStyle.TRANSPARENT);

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(44, 50, 44, 50));
        layout.setStyle("-fx-background-color:linear-gradient(to bottom right,#1a0a2e,#0a0a0f);"
                      + "-fx-border-color:" + GOLD + ";-fx-border-width:2;"
                      + "-fx-border-radius:16;-fx-background-radius:16;"
                      + "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.9),30,0.3,0,10);");

        Label trophy = new Label(playerWon ? "🏆" : "💀");
        trophy.setFont(Font.font(54));

        Label header = new Label(playerWon ? "VICTORY!" : "DEFEAT");
        header.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 30));
        header.setTextFill(Color.web(playerWon ? GOLD : RED));
        header.setEffect(new DropShadow(10, Color.web(playerWon ? GOLD : RED, 0.6)));

        Label detail = new Label(winner.getName() + " claimed Boo's Door!\n"
                + "Final Energy: " + winner.getEnergy() + " ⚡");
        detail.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        detail.setTextFill(Color.web(TEXT_LIGHT));
        detail.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        detail.setWrapText(true);

        Monster p = game.getPlayer(); Monster o = game.getOpponent();
        Label scores = new Label(p.getName() + ": " + p.getEnergy() + " ⚡    |    " + o.getName() + ": " + o.getEnergy() + " ⚡");
        scores.setFont(Font.font("Arial", 13));
        scores.setTextFill(Color.web(MUTED));

        Button btnBack = actionButton("↩  RETURN TO MENU", GOLD, "#1a0a00");
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
        Scene sc = new Scene(new StackPane(layout)); sc.setFill(Color.TRANSPARENT);
        fadeInLayout(layout);
        
        pop.setScene(sc); pop.show();
    }

    private void showError(String title, String msg) {
        playSound("error.wav"); shakeScreen();
        Stage pop = new Stage();
        pop.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        pop.initStyle(StageStyle.TRANSPARENT);

        VBox layout = new VBox(18);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(38, 46, 38, 46));
        layout.setStyle("-fx-background-color:linear-gradient(to bottom right,#1a0a2e,#0a0a0f);"
                      + "-fx-border-color:" + RED + ";-fx-border-width:2;"
                      + "-fx-border-radius:14;-fx-background-radius:14;"
                      + "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.9),25,0.3,0,8);");

        Label head = new Label("⚠  " + title.toUpperCase());
        head.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 22));
        head.setTextFill(Color.web(RED));
        head.setEffect(new DropShadow(8, Color.web(RED, 0.5)));

        Label body = new Label(msg);
        body.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        body.setTextFill(Color.web(TEXT_LIGHT));
        body.setWrapText(true); body.setMaxWidth(320);
        body.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button ok = actionButton("CONTINUE", GOLD, "#1a0a00");
        ok.setPrefWidth(180);
        ok.setOnAction(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(180), layout);
            ft.setToValue(0); ft.setOnFinished(ev -> pop.close()); ft.play();
        });

        layout.getChildren().addAll(head, body, ok);
        Scene sc = new Scene(new StackPane(layout)); sc.setFill(Color.TRANSPARENT);
        fadeInLayout(layout);
        
        pop.setScene(sc); pop.show();
    }

    private void handleExitPrompt() {
        playSound("error.wav"); 
        Stage pop = new Stage();
        pop.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        pop.initStyle(StageStyle.TRANSPARENT);

        VBox layout = new VBox(18);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(38, 46, 38, 46));
        layout.setStyle("-fx-background-color:linear-gradient(to bottom right,#2a0815,#12080a);"
                      + "-fx-border-color:" + RED + ";-fx-border-width:2;"
                      + "-fx-border-radius:14;-fx-background-radius:14;"
                      + "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.9),25,0.3,0,8);");

        Label head = new Label("⚠  ABANDON GAME?");
        head.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 22));
        head.setTextFill(Color.web(RED));
        head.setEffect(new DropShadow(8, Color.web(RED, 0.5)));

        Label body = new Label("Are you certain you wish to leave?\nAll progress will be permanently lost.");
        body.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        body.setTextFill(Color.web(TEXT_LIGHT));
        body.setWrapText(true); body.setMaxWidth(320);
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
        rootPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-padding: 20;");
        
        Scene sc = new Scene(rootPane); 
        sc.setFill(Color.TRANSPARENT);
        
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
        lblPileCount.setText("📚 Pile: " + remainingPile + " cards");
        log("Card drawn: " + cardName, "neutral");
        
        ScaleTransition st = new ScaleTransition(Duration.millis(300), cardVisualBox);
        st.setFromX(0.85); st.setFromY(0.85);
        st.setToX(1.0); st.setToY(1.0);
        st.play();
    }

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
        h.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 10));
        h.setTextFill(Color.web(PURPLE));
        h.setStyle("-fx-letter-spacing:1.5px;");
        box.getChildren().add(h);
        return box;
    }

    private StackPane monsterAvatar(String iconFile, String bgHex) {
        StackPane sp = new StackPane();
        sp.setPrefSize(46, 46); sp.setMaxSize(46, 46);
        sp.setStyle("-fx-background-color:" + bgHex + "33;"
                  + "-fx-border-color:" + bgHex + "66;"
                  + "-fx-border-width:2;-fx-border-radius:23;-fx-background-radius:23;");
        ImageView iv = loadIcon(iconFile, 32);
        if (iv != null) sp.getChildren().add(iv);
        return sp;
    }

    private Label makeTag(String text, String fg, String bg, String border) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 9));
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
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        btn.setMaxWidth(Double.MAX_VALUE);
        String base = "-fx-background-color:" + bg + ";-fx-text-fill:" + fg + ";"
                    + "-fx-padding:9 14;-fx-background-radius:10;-fx-border-radius:10;-fx-cursor:hand;";
        String hover = base + "-fx-effect:dropshadow(three-pass-box," + bg + "aa,12,0.4,0,0);";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> { btn.setStyle(hover); playSound("flicker.wav"); });
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    private Button smallBtn(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setFont(Font.font("Arial", FontWeight.BOLD, 10));
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
        txt.setFont(Font.font("Arial", 10)); txt.setTextFill(Color.web(MUTED));
        row.getChildren().addAll(dot, ico, txt);
        return row;
    }

    private Label panelLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 11));
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
        t.setFont(Font.font("Arial", 11));
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

    private void styleTurnBadge(Label lbl, boolean myTurn) {
        if (myTurn) {
            lbl.setText("▶  YOUR TURN");
            lbl.setStyle("-fx-background-color:" + GOLD + ";-fx-text-fill:#1a0a00;"
                       + "-fx-border-radius:20;-fx-background-radius:20;-fx-padding:5 16;"
                       + "-fx-font-weight:bold;-fx-font-family:Arial;");
        } else {
            lbl.setText("OPPONENT'S TURN…");
            lbl.setStyle("-fx-background-color:#7c3aed22;-fx-text-fill:" + VIOLET_TEXT + ";"
                       + "-fx-border-color:#7c3aed66;-fx-border-width:1;"
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
        btn.setOnMouseEntered(e -> { btn.setStyle(hov); playSound("flicker.wav"); });
        btn.setOnMouseExited(e  -> btn.setStyle(def));
        ImageView ic = loadIcon("exit.png", 16);
        if (ic != null) btn.setGraphic(ic);
        
        btn.setOnAction(e -> handleExitPrompt());
    }

    private void fadeInLayout(VBox layout) {
        layout.setOpacity(0); layout.setTranslateY(24);
        FadeTransition ft = new FadeTransition(Duration.millis(260), layout); ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(260), layout); tt.setToY(0);
        ft.play(); tt.play();
    }

    private void log(String msg, String type) {
        String time = java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        Label l = new Label("[" + time + "]  " + msg);
        l.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        l.setWrapText(true);
        switch (type) {
            case "good":
                l.setTextFill(Color.web(GREEN)); 
                break;
            case "bad":
                l.setTextFill(Color.web(RED)); 
                break;
            default:
                l.setTextFill(Color.web(VIOLET_TEXT)); 
                break;
        }
        logBox.getChildren().add(0, l);
        if (logBox.getChildren().size() > 60) logBox.getChildren().remove(60);
    }
}