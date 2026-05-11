package game.gui;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

import game.engine.Game;
import game.engine.Role;
import game.engine.monsters.Monster;
import game.engine.cells.Cell;
import game.engine.cells.DoorCell;
import game.engine.cells.MonsterCell;
import game.engine.cells.CardCell;
import game.engine.cells.ConveyorBelt;
import game.engine.cells.ContaminationSock;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;

public class GameWindow {
    private Stage stage;
    private Game game;
    private GridPane boardGrid;
    
    private Label turnInfo;
    private VBox playerCardBox, oppCardBox;
    private Label playerName, playerRole, playerType, playerShield;
    private Label oppName, oppRole, oppType, oppShield;
    private ProgressBar playerEnergyBar, oppEnergyBar;
    private Label playerEnergyText, oppEnergyText;
    private Button rollButton, powerupButton, exitButton;
    private StackPane[] cellPanes = new StackPane[100];
    private VBox logBox;
    
    private ImageView diceView;

    public GameWindow(Stage stage, String side) {
        this.stage = stage;
        try {
            Role role = Role.valueOf(side);
            this.game = new Game(role);
        } catch (Exception e) {
            showError("Init Error", "Failed to initialize game engine: " + e.getMessage());
            return;
        }
        setupUI();
    }

    private void setupUI() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #2b4b7c, #1a2a42); -fx-padding: 20;");

        boardGrid = new GridPane();
        boardGrid.setStyle("-fx-background-color: #111; -fx-padding: 4; -fx-hgap: 2; -fx-vgap: 2; -fx-border-color: #444; -fx-border-width: 4; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        GridPane.setHgrow(boardGrid, Priority.ALWAYS);
        GridPane.setVgrow(boardGrid, Priority.ALWAYS);

        for (int i = 0; i < 10; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(10);
            col.setHgrow(Priority.ALWAYS);
            boardGrid.getColumnConstraints().add(col);

            RowConstraints row = new RowConstraints();
            row.setPercentHeight(10);
            row.setVgrow(Priority.ALWAYS);
            boardGrid.getRowConstraints().add(row);
        }
        
        for (int i = 0; i < 100; i++) {
            int row = i / 10;
            int col = i % 10;
            if (row % 2 == 1) col = 9 - col;

            StackPane cellPane = new StackPane();
            cellPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            
            Label idxLabel = new Label(String.valueOf(i));
            idxLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
            idxLabel.setTextFill(Color.DARKSLATEGRAY);
            StackPane.setAlignment(idxLabel, Pos.TOP_LEFT);
            StackPane.setMargin(idxLabel, new Insets(2, 0, 0, 4));
            
            cellPane.getChildren().add(idxLabel);
            boardGrid.add(cellPane, col, 9 - row);
            cellPanes[i] = cellPane;
        }
        
        HBox gridWrapper = new HBox(boardGrid);
        gridWrapper.setAlignment(Pos.CENTER);
        gridWrapper.setPadding(new Insets(0, 20, 0, 0));
        HBox.setHgrow(boardGrid, Priority.ALWAYS);
        mainLayout.setCenter(gridWrapper);

        VBox rightPanel = new VBox(15);
        // FLEXIBLE SIDEBAR: Can shrink down to 280px if needed, prefers 350px, won't grow past 400px
        rightPanel.setMinWidth(280);
        rightPanel.setPrefWidth(350);
        rightPanel.setMaxWidth(400); 
        rightPanel.setAlignment(Pos.TOP_CENTER);
        rightPanel.setStyle("-fx-background-color: #24344d; -fx-padding: 20; -fx-border-color: #3a5378; -fx-border-width: 3; -fx-border-radius: 10; -fx-background-radius: 10;");

        turnInfo = new Label("PLAYER 1 TURN\n(YOUR TURN)");
        turnInfo.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 24));
        turnInfo.setTextFill(Color.web("#2ecc71"));
        turnInfo.setAlignment(Pos.CENTER);
        turnInfo.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        playerCardBox = new VBox(8);
        playerCardBox.setStyle("-fx-background-color: #1a332a; -fx-border-color: #2ecc71; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 15;");
        playerName = createStyledLabel("Name: ", 18, "#ffffff", true);
        playerType = createStyledLabel("Type: ", 14, "#bdc3c7", false);
        playerRole = createStyledLabel("Role: ", 14, "#f39c12", true);
        playerShield = createStyledLabel("Shield: ", 14, "#2ecc71", true);
        
        playerEnergyText = createStyledLabel("Energy: 0 / 1000", 14, "#ffffff", false);
        playerEnergyBar = new ProgressBar(0);
        playerEnergyBar.setMaxWidth(Double.MAX_VALUE); // Let progress bar shrink
        playerEnergyBar.setPrefHeight(15);
        playerEnergyBar.setStyle("-fx-accent: #f39c12; -fx-control-inner-background: #2c3e50;");

        playerCardBox.getChildren().addAll(playerName, playerType, playerEnergyText, playerEnergyBar, playerRole, playerShield);

        diceView = new ImageView();
        diceView.setFitWidth(80);
        diceView.setFitHeight(80);
        diceView.setPreserveRatio(true);
        Image defaultDice = loadImage("dice6.png");
        if (defaultDice != null) diceView.setImage(defaultDice);
        
        HBox diceBox = new HBox(diceView);
        diceBox.setAlignment(Pos.CENTER);
        diceBox.setPadding(new Insets(5, 0, 5, 0));

        rollButton = new Button("ROLL DICE");
        rollButton.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        rollButton.setStyle("-fx-background-color: linear-gradient(#e0f7fa, #81d4fa); -fx-text-fill: #111; -fx-background-radius: 30; -fx-padding: 15 40; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 5, 0, 0, 3);");
        rollButton.setMaxWidth(Double.MAX_VALUE);
        rollButton.setOnAction(e -> handleRollDice());

        powerupButton = new Button("USE POWERUP\nCost: 500 Energy");
        powerupButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        powerupButton.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        powerupButton.setStyle("-fx-background-color: linear-gradient(#b2dfdb, #4db6ac); -fx-text-fill: #111; -fx-background-radius: 15; -fx-padding: 10 20;");
        powerupButton.setMaxWidth(Double.MAX_VALUE);
        powerupButton.setOnAction(e -> handlePowerup());

        oppCardBox = new VBox(8);
        oppCardBox.setStyle("-fx-background-color: #2d1b38; -fx-border-color: #9b59b6; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 15;");
        oppName = createStyledLabel("Name: ", 18, "#ffffff", true);
        oppType = createStyledLabel("Type: ", 14, "#bdc3c7", false);
        oppRole = createStyledLabel("Role: ", 14, "#f39c12", true);
        oppShield = createStyledLabel("Shield: ", 14, "#9b59b6", true);
        
        oppEnergyText = createStyledLabel("Energy: 0 / 1000", 14, "#ffffff", false);
        oppEnergyBar = new ProgressBar(0);
        oppEnergyBar.setMaxWidth(Double.MAX_VALUE); // Let progress bar shrink
        oppEnergyBar.setPrefHeight(15);
        oppEnergyBar.setStyle("-fx-accent: #9b59b6; -fx-control-inner-background: #2c3e50;");

        oppCardBox.getChildren().addAll(oppName, oppType, oppEnergyText, oppEnergyBar, oppRole, oppShield);

        logBox = new VBox(4);
        logBox.setStyle("-fx-background-color: #ffffff; -fx-padding: 10; -fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-background-radius: 5;");
        logBox.setMinHeight(100); // Ensures it doesn't vanish entirely
        VBox.setVgrow(logBox, Priority.ALWAYS);
        
        Label logTitle = new Label("ACTION LOG");
        logTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        logTitle.setTextFill(Color.WHITE);
        
        VBox bottomPanel = new VBox(5, logTitle, logBox);
        bottomPanel.setPadding(new Insets(10, 0, 0, 0));
        VBox.setVgrow(bottomPanel, Priority.ALWAYS);

        exitButton = new Button(" EXIT GAME");
        exitButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        exitButton.setMaxWidth(Double.MAX_VALUE);
        exitButton.setStyle("-fx-background-color: linear-gradient(#e74c3c, #c0392b); -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 10;");
        
        ImageView exitIcon = loadIcon("exit.png", 18);
        if (exitIcon != null) exitButton.setGraphic(exitIcon);
        exitButton.setOnAction(e -> handleExit());

        rightPanel.getChildren().addAll(turnInfo, playerCardBox, diceBox, rollButton, powerupButton, oppCardBox, bottomPanel, exitButton);
        mainLayout.setRight(rightPanel);

        updateBoard();

        // THE SAFETY NET: Wrap everything in a ScrollPane
        ScrollPane scrollRoot = new ScrollPane(mainLayout);
        scrollRoot.setFitToWidth(true);
        scrollRoot.setFitToHeight(true);
        // Matches the background so the scrolling looks seamless
        scrollRoot.setStyle("-fx-background: #1a2a42; -fx-border-color: #1a2a42;");

        Scene scene = new Scene(scrollRoot, 1280, 850);
        // Allow the user to shrink the window as much as they want safely
        stage.setMinWidth(600); 
        stage.setMinHeight(500);
        stage.setScene(scene);
    }

    private Image loadImage(String filename) {
        try {
            File file = new File("assets/" + filename);
            if (file.exists()) {
                return new Image(file.toURI().toString());
            }
        } catch (Exception e) {}
        return null;
    }

    private ImageView loadIcon(String filename, int size) {
        Image img = loadImage(filename);
        if (img != null) {
            ImageView view = new ImageView(img);
            view.setFitWidth(size);
            view.setFitHeight(size);
            view.setPreserveRatio(true);
            return view;
        }
        return null;
    }

    private Label createStyledLabel(String text, int size, String hexColor, boolean bold) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
        l.setTextFill(Color.web(hexColor));
        l.setWrapText(true); // Let labels wrap if they get too tight
        return l;
    }

    private void updateBoard() {
        Monster player = game.getPlayer();
        Monster opponent = game.getOpponent();
        Cell[][] engineCells = game.getBoard().getBoardCells();

        for (int i = 0; i < 100; i++) {
            StackPane pane = cellPanes[i];
            
            if (pane.getChildren().size() > 1) {
                javafx.scene.Node indexLabel = pane.getChildren().get(0);
                pane.getChildren().clear();
                pane.getChildren().add(indexLabel);
            }

            int row = i / 10;
            int col = (row % 2 == 1) ? 9 - (i % 10) : (i % 10);
            Cell engineCell = engineCells[row][col];

            String bgColor = "#ffe680"; 
            String cellText = "";
            ImageView cellImage = null;

            if (i == 0) {
                bgColor = "#2ecc71"; 
                cellText = "START";
                cellImage = loadIcon("start.png", 30);
            } else if (i == 99) {
                bgColor = "#f1c40f"; 
                cellText = "BOO'S\nDOOR";
                cellImage = loadIcon("boo.png", 30);
            } else if (engineCell instanceof DoorCell) {
                DoorCell dc = (DoorCell) engineCell;
                bgColor = dc.getRole() == Role.SCARER ? "#c39bd3" : "#f5b041"; 
                cellText = "Val: " + dc.getEnergy();
                cellImage = dc.getRole() == Role.SCARER ? loadIcon("door_scarer.png", 25) : loadIcon("door_laugher.png", 25);
            } else if (engineCell instanceof MonsterCell) {
                bgColor = "#85c1e9"; 
                Monster stationed = ((MonsterCell) engineCell).getCellMonster();
                cellText = stationed != null ? stationed.getName().split(" ")[0] : "Monster";
                cellImage = loadIcon("monster.png", 25);
            } else if (engineCell instanceof CardCell) {
                bgColor = "#ec7063"; 
                cellImage = loadIcon("card.png", 30);
            } else if (engineCell instanceof ConveyorBelt) {
                bgColor = "#82e0aa"; 
                cellText = "+" + ((ConveyorBelt)engineCell).getEffect();
                cellImage = loadIcon("belt.png", 25);
            } else if (engineCell instanceof ContaminationSock) {
                bgColor = "#eb984e"; 
                cellText = "-" + Math.abs(((ContaminationSock)engineCell).getEffect());
                cellImage = loadIcon("sock.png", 25);
            }

            pane.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: #333; -fx-border-width: 1;");
            
            VBox contentBox = new VBox(2);
            contentBox.setAlignment(Pos.TOP_CENTER);
            contentBox.setPadding(new Insets(6, 0, 0, 0));
            
            if (cellImage != null) {
                contentBox.getChildren().add(cellImage);
            }
            if (!cellText.isEmpty()) {
                Label textLabel = new Label(cellText);
                textLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                textLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                textLabel.setAlignment(Pos.CENTER);
                textLabel.setMaxWidth(Double.MAX_VALUE);
                // Allow label to shrink if cell gets tiny
                textLabel.setMinSize(0, 0); 
                contentBox.getChildren().add(textLabel);
            }
            
            pane.getChildren().add(contentBox);
            StackPane.setAlignment(contentBox, Pos.TOP_CENTER);

            if (i == player.getPosition() || i == opponent.getPosition()) {
                HBox monsterBox = new HBox(4); 
                monsterBox.setAlignment(Pos.BOTTOM_CENTER);
                monsterBox.setPadding(new Insets(0, 0, 6, 0)); 
                
                if (i == player.getPosition()) {
                    ImageView pImg = loadIcon("player.png", 20);
                    if (pImg != null) monsterBox.getChildren().add(pImg);
                    else {
                        javafx.scene.shape.Circle pToken = new javafx.scene.shape.Circle(8, Color.web("#2ecc71"));
                        pToken.setStroke(Color.WHITE); pToken.setStrokeWidth(2);
                        monsterBox.getChildren().add(pToken);
                    }
                }
                if (i == opponent.getPosition()) {
                    ImageView oImg = loadIcon("opponent.png", 20);
                    if (oImg != null) monsterBox.getChildren().add(oImg);
                    else {
                        javafx.scene.shape.Circle oToken = new javafx.scene.shape.Circle(8, Color.web("#9b59b6"));
                        oToken.setStroke(Color.WHITE); oToken.setStrokeWidth(2);
                        monsterBox.getChildren().add(oToken);
                    }
                }
                pane.getChildren().add(monsterBox);
                StackPane.setAlignment(monsterBox, Pos.BOTTOM_CENTER);
            }
        }

        playerName.setText("Name: " + player.getName());
        playerType.setText("Type: " + player.getClass().getSimpleName());
        playerRole.setText("Role: " + player.getRole() + (player.isConfused() ? " (CONFUSED)" : ""));
        playerShield.setText("Shield: " + (player.isShielded() ? "ACTIVE" : "None"));
        playerEnergyText.setText("Energy: " + player.getEnergy() + " / 1000");
        playerEnergyBar.setProgress(Math.min(1.0, player.getEnergy() / 1000.0));

        oppName.setText("Name: " + opponent.getName());
        oppType.setText("Type: " + opponent.getClass().getSimpleName());
        oppRole.setText("Role: " + opponent.getRole() + (opponent.isConfused() ? " (CONFUSED)" : ""));
        oppShield.setText("Shield: " + (opponent.isShielded() ? "ACTIVE" : "None"));
        oppEnergyText.setText("Energy: " + opponent.getEnergy() + " / 1000");
        oppEnergyBar.setProgress(Math.min(1.0, opponent.getEnergy() / 1000.0));
        
        boolean isPlayerTurn = (game.getCurrent() == player);
        if(isPlayerTurn) {
            turnInfo.setText("PLAYER 1 TURN\n(YOUR TURN)");
            turnInfo.setTextFill(Color.web("#2ecc71"));
        } else {
            turnInfo.setText("OPPONENT'S TURN\n(WAITING...)");
            turnInfo.setTextFill(Color.web("#9b59b6"));
        }
        
        rollButton.setDisable(!isPlayerTurn);
        powerupButton.setDisable(!isPlayerTurn || player.getEnergy() < 500);
        exitButton.setDisable(!isPlayerTurn);
    }

    private void performAnimatedRoll(boolean isPlayer) {
        rollButton.setDisable(true);
        powerupButton.setDisable(true);
        exitButton.setDisable(true);

        int finalRoll = 1;
        try {
            java.lang.reflect.Method rollMethod = game.getClass().getDeclaredMethod("rollDice");
            rollMethod.setAccessible(true);
            finalRoll = (int) rollMethod.invoke(game);
        } catch (Exception e) {
            finalRoll = (int) (Math.random() * 6) + 1;
        }

        final int actualRoll = finalRoll;
        Timeline timeline = new Timeline();

        for (int i = 0; i < 10; i++) {
            Duration duration = Duration.millis(i * 60); 
            KeyFrame frame = new KeyFrame(duration, e -> {
                int randomFace = (int) (Math.random() * 6) + 1;
                Image diceImg = loadImage("dice" + randomFace + ".png");
                if (diceImg != null) diceView.setImage(diceImg);
            });
            timeline.getKeyFrames().add(frame);
        }

        KeyFrame finalFrame = new KeyFrame(Duration.millis(650), e -> {
            Image actualImg = loadImage("dice" + actualRoll + ".png");
            if (actualImg != null) diceView.setImage(actualImg);
            
            exitButton.setDisable(false); 
            executeMove(isPlayer, actualRoll);
        });
        
        timeline.getKeyFrames().add(finalFrame);
        timeline.play();
    }

    private void executeMove(boolean isPlayer, int roll) {
        Monster current = isPlayer ? game.getPlayer() : game.getOpponent();
        Monster opponent = isPlayer ? game.getOpponent() : game.getPlayer();

        logAction((isPlayer ? "You" : "Opponent") + " rolled a " + roll + "!");

        try {
            if (current.isFrozen()) {
                current.setFrozen(false);
                logAction(current.getName() + " thawed out (Frozen)!");
            } else {
                game.getBoard().moveMonster(current, roll, opponent);
            }
            game.setCurrent(opponent); 
            
        } catch (InvalidMoveException e) {
            if (isPlayer) {
                showError("Invalid Move!", "The destination cell is occupied by the opponent.");
            } else {
                logAction("Opponent move blocked. Retrying...");
                triggerOpponentTurn();
                return; 
            }
        } catch (Exception e) {
            if (e.getCause() instanceof InvalidMoveException) {
                if (isPlayer) {
                    showError("Invalid Move!", "The destination cell is occupied.");
                } else {
                    logAction("Opponent move blocked. Retrying...");
                    triggerOpponentTurn();
                    return;
                }
            } else {
                if (isPlayer) showError("Error", e.getMessage());
            }
        }

        updateBoard();
        checkWinState();

        if (game.getWinner() == null && game.getCurrent() == game.getOpponent()) {
            triggerOpponentTurn();
        }
    }

    private void handleRollDice() {
        performAnimatedRoll(true);
    }

    private void handlePowerup() {
        try {
            game.usePowerup();
            logAction("You used your powerup!");
            updateBoard();
            
            if (game.getCurrent() == game.getOpponent()) {
                triggerOpponentTurn();
            }
        } catch (OutOfEnergyException e) {
            showError("Action Failed", "Not enough energy to use powerup! You need 500.");
        }
    }

    private void triggerOpponentTurn() {
        logAction("Opponent is thinking...");
        
        PauseTransition pause = new PauseTransition(Duration.seconds(1.0));
        pause.setOnFinished(event -> {
            try {
                if (game.getOpponent().getEnergy() >= 500) {
                    game.usePowerup();
                    logAction("Opponent used a powerup!");
                    updateBoard();
                }
            } catch (Exception e) {}
            
            performAnimatedRoll(false);
        });
        pause.play();
    }

    private void handleExit() {
        Stage popup = new Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setTitle("Exit Game");

        Label title = new Label("Abandon the Floor?");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Label text = new Label("Are you sure you want to exit?\nAll current game progress will be lost.");
        text.setFont(Font.font("Arial", 14));
        text.setStyle("-fx-text-alignment: center;");

        Button yesBtn = new Button("Yes, Exit");
        yesBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
        yesBtn.setOnAction(e -> {
            popup.close();
            stage.close(); 
        });

        Button noBtn = new Button("Cancel");
        noBtn.setStyle("-fx-padding: 8 15;");
        noBtn.setOnAction(e -> popup.close());

        HBox btnBox = new HBox(15, yesBtn, noBtn);
        btnBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(15, title, text, btnBox);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 25; -fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 3;");

        popup.setScene(new Scene(layout));
        popup.showAndWait();
    }

    private void checkWinState() {
        Monster winner = game.getWinner();
        if (winner != null) {
            String msg = winner.getName() + " (" + winner.getRole() + ") has won the game with " + winner.getEnergy() + " energy!";
            showCustomPopup("Game Over", "WE HAVE A WINNER!", msg, true);
        }
    }

    private void showError(String title, String msg) {
        showCustomPopup(title, "INVALID ACTION!", msg, false);
    }

    private void showCustomPopup(String windowTitle, String header, String content, boolean closeGameOnOk) {
        Stage popup = new Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setTitle(windowTitle);

        Label headLabel = new Label(header);
        headLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        headLabel.setTextFill(Color.web("#c0392b"));

        Label textLabel = new Label(content);
        textLabel.setFont(Font.font("Arial", 14));
        textLabel.setStyle("-fx-text-alignment: center;");

        Button okBtn = new Button("OK");
        okBtn.setStyle("-fx-font-weight: bold; -fx-padding: 8 25;");
        okBtn.setOnAction(e -> {
            popup.close();
            if (closeGameOnOk) {
                stage.close();
            }
        });

        VBox layout = new VBox(15, headLabel, textLabel, okBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 25; -fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 3;");

        popup.setScene(new Scene(layout));
        popup.showAndWait();
    }

    private void logAction(String msg) {
        Label l = new Label(msg);
        l.setFont(Font.font("Consolas", 12));
        logBox.getChildren().add(0, l);
    }
}